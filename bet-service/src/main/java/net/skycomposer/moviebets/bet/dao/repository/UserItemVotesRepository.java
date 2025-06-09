package net.skycomposer.moviebets.bet.dao.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.skycomposer.moviebets.bet.dao.entity.UserItemVotesEntity;

@Repository
public interface UserItemVotesRepository extends JpaRepository<UserItemVotesEntity, UUID> {

    @Modifying
    @Query(value = """
        WITH
        -- Get votes count for itemWon
        item_won_votes AS (
            SELECT item_id, COUNT(*) AS vote_count
            FROM bet
            WHERE market_id = :marketId
              AND ((:item1Won AND item1_id = :itemWon) OR (NOT :item1Won AND item2_id = :itemWon))
            GROUP BY item_id
        ),
        -- Get votes count for itemLost
        item_lost_votes AS (
            SELECT item_id, COUNT(*) AS vote_count
            FROM bet
            WHERE market_id = :marketId
              AND ((:item1Won AND item2_id = :itemLost) OR (NOT :item1Won AND item1_id = :itemLost))
            GROUP BY item_id
        ),
        -- Insert or update votes for itemWon
        upsert_item_won AS (
            INSERT INTO user_item_votes (id, user_id, item_id, votes)
            SELECT gen_random_uuid(), customer_id, :itemWon, 1
            FROM bet
            WHERE market_id = :marketId
              AND ((:item1Won AND item1_id = :itemWon) OR (NOT :item1Won AND item2_id = :itemWon))
            ON CONFLICT (user_id, item_id) DO UPDATE SET votes = user_item_votes.votes + 1
            RETURNING *
        ),
        -- Insert or update votes for itemLost
        upsert_item_lost AS (
            INSERT INTO user_item_votes (id, user_id, item_id, votes)
            SELECT gen_random_uuid(), customer_id, :itemLost, 1
            FROM bet
            WHERE market_id = :marketId
              AND ((:item1Won AND item2_id = :itemLost) OR (NOT :item1Won AND item1_id = :itemLost))
            ON CONFLICT (user_id, item_id) DO UPDATE SET votes = user_item_votes.votes + 1
            RETURNING *
        ),
        -- Get all users who won
        winning_users AS (
            SELECT customer_id
            FROM bet
            WHERE market_id = :marketId AND bet_won = TRUE
        ),
        -- Get all users who lost
        losing_users AS (
            SELECT customer_id
            FROM bet
            WHERE market_id = :marketId AND bet_won = FALSE
        ),
        -- Create user-user pairs for winners
        winner_pairs AS (
            SELECT wu1.customer_id AS user_id, wu2.customer_id AS friend_id
            FROM winning_users wu1, winning_users wu2
            WHERE wu1.customer_id <> wu2.customer_id
        ),
        -- Create user-user pairs for losers
        loser_pairs AS (
            SELECT lu1.customer_id AS user_id, lu2.customer_id AS friend_id
            FROM losing_users lu1, losing_users lu2
            WHERE lu1.customer_id <> lu2.customer_id
        ),
        -- Combine all user-user weight updates
        all_pairs AS (
            SELECT * FROM winner_pairs
            UNION ALL
            SELECT * FROM loser_pairs
        )
        -- Insert or update weights
        INSERT INTO user_friend_weight (id, user_id, friend_id, weight)
        SELECT gen_random_uuid(), user_id, friend_id, 1
        FROM all_pairs
        ON CONFLICT (user_id, friend_id) DO UPDATE SET weight = user_friend_weight.weight + 1;
        """, nativeQuery = true)
    void updateUserVotesAndFriendWeights(@Param("marketId") UUID marketId,
                                         @Param("itemWon") String itemWon,
                                         @Param("itemLost") String itemLost,
                                         @Param("item1Won") boolean item1Won);
}
