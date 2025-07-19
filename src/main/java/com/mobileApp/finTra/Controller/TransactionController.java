package com.mobileApp.finTra.Controller;

import com.mobileApp.finTra.Entity.TransactionModel;
import com.mobileApp.finTra.Repository.TransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;

    public TransactionController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    // ✅ GET transactions by userId
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserTransactions(@PathVariable Long userId) {
        List<TransactionModel> transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);

        if (transactions.isEmpty()) {
            return ResponseEntity.ok("No transactions found");
        }

        return ResponseEntity.ok(transactions);
    }
}

