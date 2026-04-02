package com.finance.backend.service;

import com.finance.backend.dto.request.CreateTransactionRequest;
import com.finance.backend.dto.request.UpdateTransactionRequest;
import com.finance.backend.dto.response.TransactionResponse;
import com.finance.backend.entity.Transaction;
import com.finance.backend.entity.User;
import com.finance.backend.enums.Role;
import com.finance.backend.enums.TransactionType;
import com.finance.backend.exception.ResourceNotFoundException;
import com.finance.backend.exception.UnauthorizedAccessException;
import com.finance.backend.repository.TransactionRepository;
import com.finance.backend.repository.UserRepository;
import com.finance.backend.specification.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        User currentUser = getCurrentUser();

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .date(request.getDate())
                .notes(request.getNotes())
                .isDeleted(false)
                .createdBy(currentUser)
                .build();

        transaction = transactionRepository.save(transaction);
        return mapToResponse(transaction);
    }

    public Page<TransactionResponse> getAllTransactions(
            TransactionType type, String category,
            LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {

        Specification<Transaction> spec = Specification.where(null);

        if (type != null) {
            spec = spec.and(TransactionSpecification.hasType(type));
        }
        if (category != null && !category.isBlank()) {
            spec = spec.and(TransactionSpecification.hasCategory(category));
        }
        if (dateFrom != null) {
            spec = spec.and(TransactionSpecification.dateAfter(dateFrom));
        }
        if (dateTo != null) {
            spec = spec.and(TransactionSpecification.dateBefore(dateTo));
        }

        return transactionRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    public TransactionResponse getTransactionById(Long id) {
        Transaction transaction = findTransactionById(id);
        return mapToResponse(transaction);
    }

    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @Transactional
    public TransactionResponse updateTransaction(Long id, UpdateTransactionRequest request) {
        Transaction transaction = findTransactionById(id);
        User currentUser = getCurrentUser();

        checkOwnership(transaction, currentUser);

        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory());
        transaction.setDate(request.getDate());
        transaction.setNotes(request.getNotes());

        transaction = transactionRepository.save(transaction);
        return mapToResponse(transaction);
    }

    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = findTransactionById(id);
        User currentUser = getCurrentUser();

        checkOwnership(transaction, currentUser);

        transaction.setIsDeleted(true);
        transactionRepository.save(transaction);
    }

    private void checkOwnership(Transaction transaction, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return; // ADMIN can modify any transaction
        }
        if (!transaction.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException(
                    "You can only modify your own transactions");
        }
    }

    private Transaction findTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .category(transaction.getCategory())
                .date(transaction.getDate())
                .notes(transaction.getNotes())
                .createdByName(transaction.getCreatedBy().getName())
                .createdByEmail(transaction.getCreatedBy().getEmail())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
