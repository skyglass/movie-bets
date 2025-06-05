package net.skycomposer.moviebets.bet.exception;

import net.skycomposer.moviebets.common.dto.item.ItemType;

public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(String itemId, ItemType itemType) {
        super(String.format("Couldn't find item %s, type = %s", itemId, itemType));
    }

}