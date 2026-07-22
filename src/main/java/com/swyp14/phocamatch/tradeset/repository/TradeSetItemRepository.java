package com.swyp14.phocamatch.tradeset.repository;

import com.swyp14.phocamatch.tradeset.domain.TradeSetItem;
import com.swyp14.phocamatch.tradeset.dto.MatchedCardProjection;
import com.swyp14.phocamatch.tradeset.dto.TradeSetCardQueryResult;
import com.swyp14.phocamatch.tradeset.dto.TradeSetRepresentativeQueryResult;
import com.swyp14.phocamatch.tradeset.dto.TradeSetTypeCountQueryResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TradeSetItemRepository extends JpaRepository<TradeSetItem,Long> {

    @Query("""
        SELECT new com.swyp14.phocamatch.tradeset.dto.TradeSetTypeCountQueryResult(
            item.tradeSet.id,
            item.tradeType,
            COUNT(item.id)
        )
        FROM TradeSetItem item
        WHERE item.tradeSet.id IN :tradeSetIds
        GROUP BY
            item.tradeSet.id,
            item.tradeType
        """)
    List<TradeSetTypeCountQueryResult> findTypeCounts(
            @Param("tradeSetIds") List<Long> tradeSetIds
    );

    @Query("""
        SELECT new com.swyp14.phocamatch.tradeset.dto.TradeSetRepresentativeQueryResult(
            item.id,
            item.tradeSet.id,
            item.tradeType,
            item.card.imageUrl,
            item.card.version.album.name,
            item.card.version.name
        )
        FROM TradeSetItem item
        WHERE item.tradeSet.id IN :tradeSetIds
        ORDER BY
            item.tradeSet.id ASC,
            item.tradeType ASC,
            item.id ASC
        """)
    List<TradeSetRepresentativeQueryResult>
    findRepresentativeCandidates(
            @Param("tradeSetIds") List<Long> tradeSetIds
    );

    @Query("""
            SELECT new com.swyp14.phocamatch.tradeset.dto.TradeSetCardQueryResult(
                item.card.id,
                item.card.version.album.name,
                item.card.version.name,
                item.card.name,
                item.card.imageUrl,
                item.tradeType
            )
            FROM TradeSetItem item
            WHERE item.tradeSet.id = :tradeSetId
            ORDER BY
                item.tradeType ASC,
                item.id ASC
            """)
    List<TradeSetCardQueryResult> findCardsByTradeSetId(
            @Param("tradeSetId") Long tradeSetId
    );

    List<TradeSetItem> findAllByTradeSet_Id(
            Long tradeSetId
    );

    @Query(
            value = """
                    SELECT
                        candidate_item.trade_set_id AS tradeSetId,
                        candidate_item.trade_type AS tradeType,
                        pc.card_id AS photoCardId,
                        pc.card_image_url AS imageUrl
                    FROM trade_set_items candidate_item
                    
                    JOIN photo_cards pc
                      ON pc.card_id = candidate_item.card_id
                    
                    WHERE candidate_item.trade_set_id IN (:candidateIds)
                      AND (
                            (
                                candidate_item.trade_type = 'HAVE'
                                AND EXISTS (
                                    SELECT 1
                                    FROM trade_set_items my_item
                                    WHERE my_item.trade_set_id = :myTradeSetId
                                      AND my_item.trade_type = 'WANT'
                                      AND my_item.card_id
                                        = candidate_item.card_id
                                )
                            )
                            OR
                            (
                                candidate_item.trade_type = 'WANT'
                                AND EXISTS (
                                    SELECT 1
                                    FROM trade_set_items my_item
                                    WHERE my_item.trade_set_id = :myTradeSetId
                                      AND my_item.trade_type = 'HAVE'
                                      AND my_item.card_id
                                        = candidate_item.card_id
                                )
                            )
                      )
                    ORDER BY
                        candidate_item.trade_set_id DESC,
                        candidate_item.trade_type ASC,
                        candidate_item.trade_set_item_id ASC
                    """,
            nativeQuery = true
    )
    List<MatchedCardProjection> findMatchedCards(
            @Param("myTradeSetId") Long myTradeSetId,
            @Param("candidateIds") List<Long> candidateIds
    );
}
