package com.finance.backend.repository;

import com.finance.backend.entity.Transaction;
import com.finance.backend.enums.TransactionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type " +
            "AND t.date BETWEEN :dateFrom AND :dateTo")
    BigDecimal sumByTypeAndDateBetween(@Param("type") TransactionType type,
                                       @Param("dateFrom") LocalDate dateFrom,
                                       @Param("dateTo") LocalDate dateTo);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.date BETWEEN :dateFrom AND :dateTo")
    long countByDateBetween(@Param("dateFrom") LocalDate dateFrom,
                            @Param("dateTo") LocalDate dateTo);

    @Query("SELECT t.category, SUM(t.amount), COUNT(t) FROM Transaction t " +
            "WHERE (:type IS NULL OR t.type = :type) " +
            "AND t.date BETWEEN :dateFrom AND :dateTo " +
            "GROUP BY t.category ORDER BY SUM(t.amount) DESC")
    List<Object[]> getCategorySummary(@Param("type") TransactionType type,
                                      @Param("dateFrom") LocalDate dateFrom,
                                      @Param("dateTo") LocalDate dateTo);

    @Query(value = """
            SELECT
                EXTRACT(MONTH FROM date) AS month,
                SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) AS totalIncome,
                SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) AS totalExpenses
            FROM transactions
            WHERE EXTRACT(YEAR FROM date) = :year
              AND is_deleted = false
            GROUP BY EXTRACT(MONTH FROM date)
            ORDER BY month
            """, nativeQuery = true)
    List<Object[]> getMonthlyTrend(@Param("year") int year);

    List<Transaction> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
