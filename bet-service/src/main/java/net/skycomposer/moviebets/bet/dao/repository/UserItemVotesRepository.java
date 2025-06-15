package net.skycomposer.moviebets.bet.dao.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.skycomposer.moviebets.bet.dao.entity.UserItemVotesEntity;
import net.skycomposer.moviebets.common.dto.item.ItemType;
import net.skycomposer.moviebets.common.dto.votes.UserItemVotes;

@Repository
public interface UserItemVotesRepository extends JpaRepository<UserItemVotesEntity, UUID> {

        @Query("""
            SELECT new net.skycomposer.moviebets.common.dto.votes.UserItemVotes(
                :userId,
                utv.itemId,
                utv.itemType,
                true,
                SUM(utv.votes)
            )
            FROM UserItemVotesEntity utv
            WHERE utv.itemType = :itemType
              AND NOT EXISTS (
                  SELECT 1
                  FROM UserItemStatusEntity uis
                  WHERE uis.userId = :userId
                    AND uis.itemId = utv.itemId
                    AND uis.itemType = :itemType
              )
            GROUP BY utv.itemId, utv.itemType
            ORDER BY SUM(utv.votes) DESC
        """)
        List<UserItemVotes> findTopVotedExcludingUserVoted(
                @Param("userId")   String   userId,
                @Param("itemType") ItemType itemType
        );

        @Query("""
            SELECT new net.skycomposer.moviebets.common.dto.votes.UserItemVotes(
                :userId,
                utv.itemId,
                utv.itemType,
                CASE WHEN COUNT(uis) = 0 THEN true ELSE false END,
                SUM(utv.votes)
            )
            FROM UserItemVotesEntity utv
            LEFT JOIN UserItemStatusEntity uis
              ON uis.userId = :userId
              AND uis.itemId = utv.itemId
              AND uis.itemType = :itemType
            WHERE utv.itemType = :itemType
            GROUP BY utv.itemId, utv.itemType
            ORDER BY SUM(utv.votes) DESC
        """)
        List<UserItemVotes> findTopVotedWithUserCanVoteFlag(
                @Param("userId")   String   userId,
                @Param("itemType") ItemType itemType
        );

        @Query("""
            SELECT new net.skycomposer.moviebets.common.dto.votes.UserItemVotes(
                :userId,
                utv.itemId,
                utv.itemType,
                true,
                SUM(utv.votes * ufw.weight)
            )
            FROM UserItemVotesEntity utv
            JOIN UserFriendWeightEntity ufw
              ON ufw.userId = :userId
              AND ufw.friendId = utv.userId
            WHERE utv.itemType = :itemType
              AND NOT EXISTS (
                  SELECT 1
                  FROM UserItemStatusEntity uis
                  WHERE uis.userId = :userId
                    AND uis.itemId = utv.itemId
                    AND uis.itemType = :itemType
              )
            GROUP BY utv.itemId, utv.itemType
            ORDER BY SUM(utv.votes * ufw.weight) DESC
        """)
        List<UserItemVotes> findFriendWeightedVotesExcludingUserVoted(
                @Param("userId")   String   userId,
                @Param("itemType") ItemType itemType
        );

        @Query("""
            SELECT new net.skycomposer.moviebets.common.dto.votes.UserItemVotes(
                :userId,
                utv.itemId,
                utv.itemType,
                CASE WHEN COUNT(uis) = 0 THEN true ELSE false END,
                SUM(utv.votes * ufw.weight)
            )
            FROM UserItemVotesEntity utv
            JOIN UserFriendWeightEntity ufw
              ON ufw.userId = :userId
              AND ufw.friendId = utv.userId
            LEFT JOIN UserItemStatusEntity uis
              ON uis.userId = :userId
              AND uis.itemId = utv.itemId
              AND uis.itemType = :itemType
            WHERE utv.itemType = :itemType
            GROUP BY utv.itemId, utv.itemType
            ORDER BY SUM(utv.votes * ufw.weight) DESC
        """)
        List<UserItemVotes> findFriendWeightedVotesWithUserCanVoteFlag(
                @Param("userId")   String   userId,
                @Param("itemType") ItemType itemType
        );


        @Modifying
        @Query(value = """
            
                WITH
            -- Insert or update votes for itemWon
            upsert_item_won AS (
                INSERT INTO user_item_votes (id, user_id, item_id, item_type, votes)
                SELECT gen_random_uuid(), customer_id, :itemWon, :itemType, 1
                FROM bet
                WHERE market_id = :marketId
                  AND status = 'SETTLED'
                  AND bet_won = TRUE
                ON CONFLICT (user_id, item_id, item_type) DO UPDATE SET votes = user_item_votes.votes + 1
            ),
            -- Insert or update votes for itemLost
            upsert_item_lost AS (
                INSERT INTO user_item_votes (id, user_id, item_id, item_type, votes)
                SELECT gen_random_uuid(), customer_id, :itemLost, :itemType, 1
                FROM bet
                WHERE market_id = :marketId
                  AND status = 'SETTLED'
                  AND bet_won = FALSE
                ON CONFLICT (user_id, item_id, item_type) DO UPDATE SET votes = user_item_votes.votes + 1
            ),
            -- Get all users who won
            winning_users AS (
                SELECT DISTINCT customer_id
                FROM bet
^                WHERE market_id = :marketId AND bet_won = TRUE AND status = 'SETTLED'
            ),
            -- Get all users who lost
            losing_users AS (
                SELECT DISTINCT customer_id
                FROM bet
                WHERE market_id = :marketId AND bet_won = FALSE AND status = 'SETTLED'
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
                                             @Param("itemType") Integer itemType);

}
