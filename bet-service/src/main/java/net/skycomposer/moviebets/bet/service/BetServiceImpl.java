package net.skycomposer.moviebets.bet.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.skycomposer.moviebets.bet.dao.entity.BetEntity;
import net.skycomposer.moviebets.bet.dao.entity.BetSettleRequestEntity;
import net.skycomposer.moviebets.bet.dao.entity.ItemEntity;
import net.skycomposer.moviebets.bet.dao.entity.MarketSettleStatusEntity;
import net.skycomposer.moviebets.bet.dao.repository.BetRepository;
import net.skycomposer.moviebets.bet.dao.repository.BetSettleRequestRepository;
import net.skycomposer.moviebets.bet.dao.repository.ItemRepository;
import net.skycomposer.moviebets.bet.dao.repository.MarketSettleStatusRepository;
import net.skycomposer.moviebets.bet.exception.*;
import net.skycomposer.moviebets.common.dto.bet.*;
import net.skycomposer.moviebets.common.dto.bet.events.BetCreatedEvent;
import net.skycomposer.moviebets.common.dto.item.ItemType;
import net.skycomposer.moviebets.common.dto.market.MarketResult;

@Service
public class BetServiceImpl implements BetService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BetRepository betRepository;

    private final ItemRepository itemRepository;

    private final MarketSettleStatusRepository marketSettleStatusRepository;

    private final BetSettleRequestRepository betSettleRequestRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String betCommandsTopicName;


    public BetServiceImpl(
            BetRepository betRepository,
            ItemRepository itemRepository,
            MarketSettleStatusRepository marketSettleStatusRepository,
            BetSettleRequestRepository betSettleRequestRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${bet.commands.topic.name}") String betCommandsTopicName
    ) {
        this.betRepository = betRepository;
        this.itemRepository = itemRepository;
        this.marketSettleStatusRepository = marketSettleStatusRepository;
        this.betSettleRequestRepository = betSettleRequestRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.betCommandsTopicName = betCommandsTopicName;
    }


    @Override
    @Transactional(readOnly = true)
    public BetData findBetById(UUID betId) {
        BetEntity betEntity = betRepository.findById(betId).get();
        if (betEntity == null) {
            throw new BetNotFoundException(betId);
        }
        return createBetData(betEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BetData> findAll() {
        return betRepository.findAll().stream()
                .map(entity -> createBetData(entity))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BetResponse place(BetData betData, String authenticatedCustomerId) {
        if (!Objects.equals(betData.getCustomerId(), authenticatedCustomerId)) {
            throw new BetOpenDeniedException(authenticatedCustomerId, betData.getCustomerId());
        }
        if (isMarketClosed(betData.getMarketId())) {
            throw new MarketIsClosedException(getMarketName(betData));
        }
        if (betRepository.existsByCustomerIdAndMarketId(betData.getCustomerId(), betData.getMarketId())) {
            throw new BetAlreadyExistsException(betData.getCustomerId(), getMarketName(betData));
        }
        getOrCreateItem1Entity(betData);
        getOrCreateItem2Entity(betData);
        BetEntity betEntity = createBetEntity(betData);
        betEntity.setStatus(BetStatus.PlACED);
        betEntity = betRepository.save(betEntity);
        BetCreatedEvent betCreatedEvent = createBetCreatedEvent(betEntity, betData, betData.getRequestId(), betData.getCancelRequestId());
        kafkaTemplate.send(betCommandsTopicName, betEntity.getId().toString(), betCreatedEvent);
        return new BetResponse(betEntity.getId(),
                "Bet %s created successfully".formatted(betEntity.getId()));
    }

    @Override
    @Transactional
    public BetResponse cancel(CancelBetRequest cancelBetRequest, String authenticatedCustomerId, boolean isAdmin) {
        BetEntity betEntity = betRepository.findById(cancelBetRequest.getBetId()).get();
        if (betEntity == null) {
            throw new BetNotFoundException(cancelBetRequest.getBetId());
        }
        if (!isAdmin && !Objects.equals(betEntity.getCustomerId(), authenticatedCustomerId)) {
            throw new BetCloseDeniedException(authenticatedCustomerId, betEntity.getCustomerId());
        }
        if (isMarketClosed(betEntity.getMarketId())) {
            throw new MarketIsClosedException(cancelBetRequest.getMarketName());
        }
        betEntity.setStatus(BetStatus.CANCELLED);
        betEntity = betRepository.save(betEntity);
        return new BetResponse(betEntity.getId(),
                "Bet %s cancelled successfully".formatted(betEntity.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public BetData getState(UUID betId) {
        return findBetById(betId);
    }

    @Override
    @Transactional(readOnly = true)
    public SumStakesData getBetsByMarket(UUID marketId) {
        List<SumStakeData> groupedStakes = betRepository.findStakeSumGroupedByResult(marketId);
        return SumStakesData.builder()
                .sumStakes(groupedStakes)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BetDataList getBetsForMarket(UUID marketId, boolean skipMarketOpenCheck) {
        if (!skipMarketOpenCheck && !isMarketClosed(marketId)) {
            throw new MarketIsOpenException(marketId);
        }
        List<BetEntity> betEntityList = betRepository.findByMarketId(marketId);
        return BetDataList.builder()
                .betDataList(createBetDataList(betEntityList))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BetDataList getBetsForPlayer(String customerId) {
        List<BetEntity> betEntityList = betRepository.findByCustomerId(customerId);
        return BetDataList.builder()
                .betDataList(createBetDataList(betEntityList))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BetData> findByMarketAndStatus(UUID marketId, BetStatus betStatus, Integer limit) {
        return betRepository.findByMarketIdAndStatus(marketId, betStatus, PageRequest.of(0, limit))
                .stream()
                .map(e -> createBetData(e))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateStatus(List<UUID> betUuids, BetStatus betStatus) {
        if (betUuids == null || betUuids.isEmpty()) {
            return;
        }
        betRepository.updateStatus(betUuids, betStatus);
    }

    @Override
    @Transactional
    public void setBetValidated(UUID betId) {
        updateStatus(List.of(betId), BetStatus.VALIDATED);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMarketClosed(UUID marketId) {
        return marketSettleStatusRepository.existsByMarketId(marketId);
    }

    @Override
    @Transactional(readOnly = true)
    public int countByMarketIdAndStatus(UUID marketId, BetStatus betStatus) {
        return betRepository.countByMarketIdAndStatus(marketId, betStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public int countSettledBets(UUID marketId) {
        MarketSettleStatusEntity marketSettleStatusEntity = marketSettleStatusRepository.findByMarketId(marketId)
                .orElseThrow(() -> new IllegalArgumentException("Wrong usage of countSettledBets method"));
        return marketSettleStatusEntity.getFinishedCount();
    }

    @Override
    @Transactional(readOnly = true)
    public MarketStatusData getMarketStatus(UUID marketId, String customerId) {
        boolean marketClosed = isMarketClosed(marketId);
        boolean customerBetExists = betRepository.existsByCustomerIdAndMarketId(customerId, marketId);
        Integer votes = marketClosed
                ? betRepository.countByMarketIdAndStatusNotIn(marketId, BetStatus.getClosedMarketInvalidStatuses())
                : betRepository.countByMarketIdAndStatus(marketId, BetStatus.VALIDATED);
        boolean canPlaceBet = !marketClosed && !customerBetExists;
        return MarketStatusData.builder()
                .canPlaceBet(canPlaceBet)
                .marketClosed(marketClosed)
                .votes(votes)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BetStatusResponse getBetStatus(String customerId, UUID marketId) {
        boolean betExists = betRepository.existsByCustomerIdAndMarketId(customerId, marketId);
        UUID betId = betExists ? betRepository.findByCustomerIdAndMarketId(customerId, marketId).get().getId() : null;
        return new BetStatusResponse(betId, betExists);
    }

    @Override
    @Transactional
    public void marketSettleStart(UUID marketId, int expectedCount) {
        if (!marketSettleStatusRepository.existsByMarketId(marketId)) {
            MarketSettleStatusEntity marketSettleStatusEntity = MarketSettleStatusEntity.builder()
                .marketId(marketId)
                .expectedCount(expectedCount)
                .finishedCount(0)
                .build();
            marketSettleStatusRepository.save(marketSettleStatusEntity);
        }
    }

    @Override
    @Transactional
    public void updateMarketSettleCount(UUID betId, UUID marketId) {
        if (betSettleRequestRepository.existsByRequestId(betId)) {
            String message = String.format("Duplicate bet settle request for bet %s, market = %s", betId, marketId);
            logger.warn(message);
        } else {
            MarketSettleStatusEntity marketSettleStatusEntity = marketSettleStatusRepository.findByMarketId(marketId).get();
            marketSettleStatusEntity.setFinishedCount(marketSettleStatusEntity.getFinishedCount() + 1);
            marketSettleStatusRepository.save(marketSettleStatusEntity);
            betSettleRequestRepository.save(
                    BetSettleRequestEntity.builder()
                    .requestId(betId)
                    .marketId(marketId)
                    .build());
        }
    }

    @Override
    @Transactional
    public void marketSettleDone(UUID marketId, MarketResult winResult) {
        betSettleRequestRepository.deleteByMarketId(marketId);
        betRepository.settleBets(marketId, BetStatus.SETTLE_STARTED, BetStatus.SETTLED, winResult);
    }

    private BetData createBetData(BetEntity betEntity) {
        ItemEntity item1Entity = findItemEntity(betEntity.getItem1Id(), betEntity.getItemType());
        ItemEntity item2Entity = findItemEntity(betEntity.getItem2Id(), betEntity.getItemType());
        return BetData.builder()
                .betId(betEntity.getId())
                .customerId(betEntity.getCustomerId())
                .marketId(betEntity.getMarketId())
                .item1Id(item1Entity.getItemId())
                .item2Id(item2Entity.getItemId())
                .item1Name(item1Entity.getName())
                .item2Name(item2Entity.getName())
                .itemType(betEntity.getItemType())
                .stake(betEntity.getStake())
                .result(betEntity.getResult())
                .status(betEntity.getStatus())
                .betWon(betEntity.getBetWon())
                .build();
    }

    private List<BetData> createBetDataList(List<BetEntity> betEntityList) {
        return betEntityList.stream().map(entity -> createBetData(entity)).toList();
    }

    private BetEntity createBetEntity(BetData betData) {
        return BetEntity.builder()
                .customerId(betData.getCustomerId())
                .marketId(betData.getMarketId())
                .item1Id(betData.getItem1Id())
                .item2Id(betData.getItem2Id())
                .itemType(betData.getItemType())
                .stake(betData.getStake() == null ? 1 : betData.getStake())
                .result(betData.getResult())
                .build();
    }

    private BetCreatedEvent createBetCreatedEvent(BetEntity betEntity, BetData betData, UUID requestId, UUID cancelRequestId) {
        return BetCreatedEvent.builder()
                .betId(betEntity.getId())
                .customerId(betEntity.getCustomerId())
                .requestId(requestId)
                .cancelRequestId(cancelRequestId)
                .marketId(betEntity.getMarketId())
                .marketName(getMarketName(betData))
                .stake(betEntity.getStake())
                .result(betEntity.getResult())
                .build();
    }

    private ItemEntity getOrCreateItem1Entity(BetData betData) {
        return getOrCreateItemEntity(betData.getItem1Id(), betData.getItem1Name(), betData.getItemType());
    }

    private ItemEntity getOrCreateItem2Entity(BetData betData) {
        return getOrCreateItemEntity(betData.getItem2Id(), betData.getItem2Name(), betData.getItemType());
    }

    private ItemEntity findItemEntity(String itemId, ItemType itemType) {
        return itemRepository.findByItemIdAndItemType(itemId, itemType)
                .orElseThrow(() -> new ItemNotFoundException(itemId, itemType));
    }

    private ItemEntity getOrCreateItemEntity(String itemId, String name, ItemType itemType) {
        ItemEntity item = itemRepository.findByItemIdAndItemType(itemId, itemType).orElse(null);
        if (item == null) {
            item = itemRepository.save(ItemEntity.builder()
                    .itemId(itemId)
                    .itemType(itemType)
                    .name(name)
                    .build());
        }
        return item;
    }

    private String getMarketName(BetData betData) {
        return getMarketName(betData.getItem1Name(), betData.getItem2Name());
    }

    private String getMarketName(String item1Name, String item2Name) {
        return "['%s'] vs ['%s']".formatted(item1Name, item2Name);
    }


}
