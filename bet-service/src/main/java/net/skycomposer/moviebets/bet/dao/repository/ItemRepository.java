package net.skycomposer.moviebets.bet.dao.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.skycomposer.moviebets.bet.dao.entity.ItemEntity;
import net.skycomposer.moviebets.common.dto.item.ItemType;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, UUID> {

    public Optional<ItemEntity> findByItemIdAndItemType(String itemId, ItemType itemType);
}
