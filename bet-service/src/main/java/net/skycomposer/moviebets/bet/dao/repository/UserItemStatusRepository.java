package net.skycomposer.moviebets.bet.dao.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.skycomposer.moviebets.bet.dao.entity.UserItemStatusEntity;
import net.skycomposer.moviebets.common.dto.item.ItemType;
import net.skycomposer.moviebets.common.dto.item.UserItemStatus;

@Repository
public interface UserItemStatusRepository extends JpaRepository<UserItemStatusEntity, UUID> {

    @Query("""
        SELECT i.itemId
        FROM UserItemStatusEntity i
        WHERE i.userId = :userId
          AND i.itemType = :itemType
          AND i.itemId IN :itemIds
    """)
    List<String> findExistingItemIds(
            @Param("userId") String userId,
            @Param("itemType") ItemType itemType,
            @Param("itemIds") List<String> itemIds
    );

    boolean existsByUserIdAndItemIdAndItemType(String userId, String itemId, ItemType itemType);

    @Query("""
        SELECT u FROM UserItemStatusEntity u
        WHERE u.status = :status
          AND NOT EXISTS (
              SELECT 1 FROM BetEntity b
              WHERE b.status = 'VALIDATED'
                AND (b.item1Id = u.itemId OR b.item2Id = u.itemId)
                AND b.itemType = u.itemType
          )
    """)
    Optional<UserItemStatusEntity> findFirstByStatusAndNoMarketExists(@Param("status") UserItemStatus status);

    @Query("""
        SELECT u FROM UserItemStatusEntity u
        WHERE u.status = :status
          AND u.itemType = :itemType
          AND u.userId <> :excludedUserId
          AND u.itemId <> :excludedItemId
          AND NOT EXISTS (
              SELECT 1 FROM BetEntity b
              WHERE b.status = 'VALIDATED'
                AND (b.item1Id = u.itemId OR b.item2Id = u.itemId)
                AND b.itemType = u.itemType
          )
    """)
    Optional<UserItemStatusEntity> findFirstByStatusAndItemTypeAndUserIdNotAndItemIdNotAndNoMarketExists(
            @Param("status") UserItemStatus status,
            @Param("itemType") ItemType itemType,
            @Param("excludedUserId") String excludedUserId,
            @Param("excludedItemId") String excludedItemId
    );

}
