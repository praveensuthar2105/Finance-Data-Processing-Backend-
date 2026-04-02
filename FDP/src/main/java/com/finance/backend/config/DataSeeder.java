package com.finance.backend.config;

import com.finance.backend.entity.Transaction;
import com.finance.backend.entity.User;
import com.finance.backend.enums.Role;
import com.finance.backend.enums.TransactionType;
import com.finance.backend.repository.TransactionRepository;
import com.finance.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded. Skipping...");
            return;
        }

        log.info("Seeding database with sample data...");

        // Create users
        User admin = userRepository.save(User.builder()
                .name("Admin User")
                .email("admin@finance.com")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .role(Role.ADMIN)
                .isActive(true)
                .build());

        User analyst = userRepository.save(User.builder()
                .name("Analyst User")
                .email("analyst@finance.com")
                .passwordHash(passwordEncoder.encode("Analyst@123"))
                .role(Role.ANALYST)
                .isActive(true)
                .build());

        User viewer = userRepository.save(User.builder()
                .name("Viewer User")
                .email("viewer@finance.com")
                .passwordHash(passwordEncoder.encode("Viewer@123"))
                .role(Role.VIEWER)
                .isActive(true)
                .build());

        log.info("Created users: admin, analyst, viewer");

        // Create 20 sample transactions across last 6 months
        LocalDate now = LocalDate.now();

        List<Transaction> transactions = List.of(
                buildTransaction(new BigDecimal("5000.00"), TransactionType.INCOME, "Salary",
                        now.minusMonths(5).withDayOfMonth(1), "Monthly salary", analyst),
                buildTransaction(new BigDecimal("1200.00"), TransactionType.EXPENSE, "Rent",
                        now.minusMonths(5).withDayOfMonth(3), "Monthly rent payment", analyst),
                buildTransaction(new BigDecimal("150.00"), TransactionType.EXPENSE, "Utilities",
                        now.minusMonths(5).withDayOfMonth(10), "Electric bill", analyst),
                buildTransaction(new BigDecimal("800.00"), TransactionType.INCOME, "Freelance",
                        now.minusMonths(4).withDayOfMonth(5), "Web design project", analyst),
                buildTransaction(new BigDecimal("5000.00"), TransactionType.INCOME, "Salary",
                        now.minusMonths(4).withDayOfMonth(1), "Monthly salary", analyst),
                buildTransaction(new BigDecimal("200.00"), TransactionType.EXPENSE, "Food",
                        now.minusMonths(4).withDayOfMonth(8), "Grocery shopping", analyst),
                buildTransaction(new BigDecimal("50.00"), TransactionType.EXPENSE, "Transport",
                        now.minusMonths(4).withDayOfMonth(12), "Bus pass", analyst),
                buildTransaction(new BigDecimal("5000.00"), TransactionType.INCOME, "Salary",
                        now.minusMonths(3).withDayOfMonth(1), "Monthly salary", admin),
                buildTransaction(new BigDecimal("1200.00"), TransactionType.EXPENSE, "Rent",
                        now.minusMonths(3).withDayOfMonth(3), "Monthly rent payment", admin),
                buildTransaction(new BigDecimal("100.00"), TransactionType.EXPENSE, "Entertainment",
                        now.minusMonths(3).withDayOfMonth(15), "Movie tickets", admin),
                buildTransaction(new BigDecimal("300.00"), TransactionType.EXPENSE, "Food",
                        now.minusMonths(3).withDayOfMonth(20), "Restaurant dining", admin),
                buildTransaction(new BigDecimal("1500.00"), TransactionType.INCOME, "Freelance",
                        now.minusMonths(2).withDayOfMonth(10), "App development", analyst),
                buildTransaction(new BigDecimal("5000.00"), TransactionType.INCOME, "Salary",
                        now.minusMonths(2).withDayOfMonth(1), "Monthly salary", analyst),
                buildTransaction(new BigDecimal("180.00"), TransactionType.EXPENSE, "Utilities",
                        now.minusMonths(2).withDayOfMonth(12), "Water and gas", analyst),
                buildTransaction(new BigDecimal("75.00"), TransactionType.EXPENSE, "Transport",
                        now.minusMonths(2).withDayOfMonth(18), "Fuel", analyst),
                buildTransaction(new BigDecimal("5000.00"), TransactionType.INCOME, "Salary",
                        now.minusMonths(1).withDayOfMonth(1), "Monthly salary", admin),
                buildTransaction(new BigDecimal("1200.00"), TransactionType.EXPENSE, "Rent",
                        now.minusMonths(1).withDayOfMonth(3), "Monthly rent payment", admin),
                buildTransaction(new BigDecimal("250.00"), TransactionType.EXPENSE, "Food",
                        now.minusMonths(1).withDayOfMonth(7), "Weekly groceries", admin),
                buildTransaction(new BigDecimal("120.00"), TransactionType.EXPENSE, "Entertainment",
                        now.minusMonths(1).withDayOfMonth(22), "Concert tickets", admin),
                buildTransaction(new BigDecimal("2000.00"), TransactionType.INCOME, "Freelance",
                        now.minusDays(5), "Consulting fee", analyst)
        );

        transactionRepository.saveAll(transactions);
        log.info("Created {} sample transactions", transactions.size());
        log.info("Database seeding complete!");
    }

    private Transaction buildTransaction(BigDecimal amount, TransactionType type,
                                         String category, LocalDate date, String notes, User createdBy) {
        return Transaction.builder()
                .amount(amount)
                .type(type)
                .category(category)
                .date(date)
                .notes(notes)
                .isDeleted(false)
                .createdBy(createdBy)
                .build();
    }
}
