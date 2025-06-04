package net.skycomposer.moviebets.common.dto.item;

public enum ItemType {
    MOVIE(0),
    MUSIC(1);

    private int value;

    private ItemType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ItemType fromValue(int value) {
        for (ItemType itemType: values()) {
            if (itemType.getValue() == value) {
                return itemType;
            }
        }
        throw new IllegalArgumentException(String.format("Unknown value for ItemType enum: %d", value));
    }
}
