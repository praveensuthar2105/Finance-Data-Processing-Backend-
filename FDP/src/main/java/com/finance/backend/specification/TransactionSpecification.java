package com.finance.backend.specification;

import com.finance.backend.entity.Transaction;
import com.finance.backend.enums.TransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class TransactionSpecification {

    private TransactionSpecification() {
    }

    public static Specification<Transaction> hasType(TransactionType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }

    public static Specification<Transaction> hasCategory(String category) {
        return (root, query, cb) -> category == null || category.isBlank()
                ? null
                : cb.like(cb.lower(root.get("category")), "%" + category.toLowerCase() + "%");
    }

    public static Specification<Transaction> dateAfter(LocalDate from) {
        return (root, query, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("date"), from);
    }

    public static Specification<Transaction> dateBefore(LocalDate to) {
        return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("date"), to);
    }

    public static Specification<Transaction> createdBy(Long userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("createdBy").get("id"), userId);
    }
}
