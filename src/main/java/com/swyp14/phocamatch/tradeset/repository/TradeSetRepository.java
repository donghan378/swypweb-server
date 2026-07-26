package com.swyp14.phocamatch.tradeset.repository;

import com.swyp14.phocamatch.tradeset.domain.TradeSet;
import com.swyp14.phocamatch.tradeset.domain.TradeSetStatus;
import com.swyp14.phocamatch.tradeset.dto.TradeSetMatchCandidateProjection;
import com.swyp14.phocamatch.tradeset.dto.TradeSetTypeCountQueryResult;
import com.swyp14.phocamatch.user.domain.UserStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TradeSetRepository extends JpaRepository<TradeSet,Long> {

    List<TradeSet>
    findAllByUser_IdAndGroup_IdAndStatusOrderByCreatedAtDescIdDesc(
            Long userId,
            Long groupId,
            TradeSetStatus status
    );

    @Query("""
            SELECT ts
            FROM TradeSet ts
            JOIN FETCH ts.group
            JOIN FETCH ts.user
            WHERE ts.id = :tradeSetId
                AND ts.status = :status
            """)
    Optional<TradeSet> findDetailByIdAndStatus(
            @Param("tradeSetId") Long tradeSetId,
            @Param("status") TradeSetStatus status
    );

    Optional<TradeSet> findByIdAndUser_Id(
            Long tradeSetId,
            Long userId
    );

    @Query(
            value = """
                    WITH my_have AS (
                        SELECT tsi.card_id
                        FROM trade_set_items tsi
                        WHERE tsi.trade_set_id = :tradeSetId
                          AND tsi.trade_type = 'HAVE'
                    ),
                    my_want AS (
                        SELECT tsi.card_id
                        FROM trade_set_items tsi
                        WHERE tsi.trade_set_id = :tradeSetId
                          AND tsi.trade_type = 'WANT'
                    ),
                    scored_matches AS (
                        SELECT
                            candidate.trade_set_id AS tradeSetId,
                            candidate.user_id AS userId,
                            u.nickname AS nickname,
                            u.profile_image_url AS profileImageUrl,
                            candidate.created_at AS createdAt,
                            
                            COUNT(
                                DISTINCT CASE
                                    WHEN candidate_item.trade_type = 'HAVE'
                                     AND my_want.card_id IS NOT NULL
                                    THEN candidate_item.card_id
                                END
                            )
                            +
                            COUNT(
                                DISTINCT CASE
                                    WHEN candidate_item.trade_type = 'WANT'
                                     AND my_have.card_id IS NOT NULL
                                    THEN candidate_item.card_id
                                END
                            ) AS matchScore,
                            
                            COUNT(
                                DISTINCT CASE
                                    WHEN candidate_item.trade_type = 'HAVE'
                                     AND my_want.card_id IS NOT NULL
                                    THEN candidate_item.card_id
                                END
                            ) AS matchedHaveCount,
                            
                            COUNT(
                                DISTINCT CASE
                                    WHEN candidate_item.trade_type = 'WANT'
                                     AND my_have.card_id IS NOT NULL
                                    THEN candidate_item.card_id
                                END
                            ) AS matchedWantCount
                            
                        FROM trade_sets candidate
                        
                        JOIN users u
                          ON u.user_id = candidate.user_id
                        
                        JOIN trade_set_items candidate_item
                          ON candidate_item.trade_set_id
                           = candidate.trade_set_id
                        
                        LEFT JOIN my_want
                          ON candidate_item.trade_type = 'HAVE'
                         AND my_want.card_id
                           = candidate_item.card_id
                        
                        LEFT JOIN my_have
                          ON candidate_item.trade_type = 'WANT'
                         AND my_have.card_id
                           = candidate_item.card_id
                        
                        WHERE candidate.group_id = :groupId
                          AND candidate.status = 'ACTIVE'
                          AND candidate.user_id <> :userId
                          AND candidate.trade_set_id <> :tradeSetId
                        
                        GROUP BY
                            candidate.trade_set_id,
                            candidate.user_id,
                            u.nickname,
                            u.profile_image_url,
                            candidate.created_at
                            
                        HAVING matchedHaveCount > 0
                           AND matchedWantCount > 0
                    )
                    SELECT
                        tradeSetId,
                        userId,
                        nickname,
                        profileImageUrl,
                        matchScore,
                        createdAt
                    FROM scored_matches
                    WHERE
                        :cursorScore IS NULL
                        OR matchScore < :cursorScore
                        OR (
                            matchScore = :cursorScore
                            AND createdAt < :cursorCreatedAt
                        )
                        OR (
                            matchScore = :cursorScore
                            AND createdAt = :cursorCreatedAt
                            AND tradeSetId < :cursorTradeSetId
                        )
                    ORDER BY
                        matchScore DESC,
                        createdAt DESC,
                        tradeSetId DESC
                    LIMIT :limit
                    """,
            nativeQuery = true
    )
    List<TradeSetMatchCandidateProjection> findMatchCandidates(
            @Param("tradeSetId") Long tradeSetId,
            @Param("groupId") Long groupId,
            @Param("userId") Long userId,
            @Param("cursorScore") Long cursorScore,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorTradeSetId") Long cursorTradeSetId,
            @Param("limit") int limit
    );

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE TradeSet tradeSet
            SET tradeSet.status = :deletedStatus
            WHERE tradeSet.user.id = :userId
              AND tradeSet.status = :activeStatus
            """)
    int deleteActiveTradeSetsByUserId(
            @Param("userId") Long userId,
            @Param("activeStatus") TradeSetStatus activeStatus,
            @Param("deletedStatus") TradeSetStatus deletedStatus
    );

    @Query("""
            SELECT tradeSet.id AS tradeSetId,
                   tradeSet.group.id AS groupId,
                   tradeSet.group.name AS groupName,
                   tradeSet.user.id AS userId,
                   tradeSet.user.nickname AS nickname,
                   tradeSet.user.profileImageUrl AS profileImageUrl,
                   tradeSet.createdAt AS createdAt
            FROM TradeSet tradeSet
            WHERE tradeSet.status = :tradeSetStatus
              AND tradeSet.user.status = :userStatus
              AND (:groupId IS NULL OR tradeSet.group.id = :groupId)
              AND (:cursor IS NULL OR tradeSet.id < :cursor)
            ORDER BY tradeSet.id DESC
            """)
    List<TradeFeedSetProjection> findFeed(
            @Param("groupId") Long groupId,
            @Param("cursor") Long cursor,
            @Param("tradeSetStatus")
            TradeSetStatus tradeSetStatus,
            @Param("userStatus")
            UserStatus userStatus,
            Pageable pageable
    );
}
