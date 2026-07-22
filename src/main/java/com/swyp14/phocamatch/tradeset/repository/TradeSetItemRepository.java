package com.swyp14.phocamatch.tradeset.repository;

import com.swyp14.phocamatch.tradeset.domain.TradeSetItem;
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
}
