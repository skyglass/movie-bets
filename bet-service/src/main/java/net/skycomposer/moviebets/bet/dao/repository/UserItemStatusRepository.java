package net.skycomposer.moviebets.bet.dao.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.skycomposer.moviebets.bet.dao.entity.UserItemStatusEntity;
import net.skycomposer.moviebets.common.dto.item.ItemType;
import net.skycomposer.moviebets.common.dto.item.UserItemStatus;

@Repository
public interface UserItemStatusRepository extends JpaRepository<UserItemStatusEntity, UUID> {

    boolean existsByUserIdAndItemIdAndItemType(String userId, String itemId, ItemType itemType);

    Optional<UserItemStatusEntity> findFirstByStatus(UserItemStatus status);

    Optional<UserItemStatusEntity> findFirstByStatusAndItemTypeAndUserIdNot(UserItemStatus status, ItemType itemType, String userId);

}
