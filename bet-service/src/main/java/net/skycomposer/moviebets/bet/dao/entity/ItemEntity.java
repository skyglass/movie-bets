package net.skycomposer.moviebets.bet.dao.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.skycomposer.moviebets.bet.dao.converter.ItemTypeConverter;
import net.skycomposer.moviebets.common.dto.item.ItemType;

@Table(name = "item")
@Entity
@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "item_id", nullable = false)
    private String itemId;

    @Column(name = "name", nullable = false)
    private String name;

    @Convert(converter = ItemTypeConverter.class)
    @Column(name = "item_type")
    private ItemType itemType;
}
