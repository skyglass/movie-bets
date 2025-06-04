package net.skycomposer.moviebets.bet.dao.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.skycomposer.moviebets.common.dto.item.UserItemStatus;

import java.util.UUID;

@Table(name = "user_item_status")
@Entity
@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserItemStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "item_id", nullable = false)
    private String itemId;

    @Column(name = "status", nullable = false)
    private UserItemStatus status;
}