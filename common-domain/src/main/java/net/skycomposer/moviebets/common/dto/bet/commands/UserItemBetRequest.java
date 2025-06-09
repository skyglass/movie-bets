package net.skycomposer.moviebets.common.dto.bet.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.skycomposer.moviebets.common.dto.item.ItemType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserItemBetRequest {
    private String userId;
    private String itemId;
    private ItemType itemType;
    private String itemName;
}
