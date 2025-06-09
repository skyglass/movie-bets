package net.skycomposer.moviebets.bet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import net.skycomposer.moviebets.bet.dao.entity.UserItemStatusEntity;
import net.skycomposer.moviebets.bet.dao.repository.UserItemStatusRepository;
import net.skycomposer.moviebets.common.dto.bet.commands.UserItemBetRequest;
import net.skycomposer.moviebets.common.dto.item.ItemType;
import net.skycomposer.moviebets.common.dto.item.UserItemStatus;

@Service
@RequiredArgsConstructor
public class UserItemStatusService {

    private final UserItemStatusRepository userItemStatusRepository;

    @Transactional
    public boolean placeVote(UserItemBetRequest userItemBetRequest) {
        boolean userItemExists = userItemExists(
                userItemBetRequest.getUserId(), userItemBetRequest.getItemId(), userItemBetRequest.getItemType());
        if (!userItemExists) {
            createUserItemStatusEntity(userItemBetRequest.getUserId(), userItemBetRequest.getItemId(),
                    userItemBetRequest.getItemName(), userItemBetRequest.getItemType());
            return false;
        }
        return true;
    }

    private boolean userItemExists(String userId, String itemId, ItemType itemType){
        return userItemStatusRepository.existsByUserIdAndItemIdAndItemType(userId, itemId, itemType);
    }

    private UserItemStatusEntity createUserItemStatusEntity (String userId, String itemId, String itemName, ItemType itemType){
        UserItemStatusEntity userItemStatusEntity = userItemStatusRepository.save(UserItemStatusEntity.builder()
                 .userId(userId)
                 .itemId(itemId)
                 .itemName(itemName)
                 .itemType(itemType)
                 .status(UserItemStatus.VOTED)
                 .build());
        return userItemStatusEntity;
    }

}
