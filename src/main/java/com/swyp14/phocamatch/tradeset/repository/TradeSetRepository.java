package com.swyp14.phocamatch.tradeset.repository;

import com.swyp14.phocamatch.tradeset.domain.TradeSet;
import com.swyp14.phocamatch.tradeset.domain.TradeSetStatus;
import com.swyp14.phocamatch.tradeset.dto.TradeSetTypeCountQueryResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
            WHERE ts.id = :tradeSetId
            """)
    Optional<TradeSet> findDetailById(
            @Param("tradeSetId") Long tradeSetId
    );

    Optional<TradeSet> findByIdAndUser_Id(
            Long tradeSetId,
            Long userId
    );
}
