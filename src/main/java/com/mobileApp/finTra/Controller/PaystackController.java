package com.mobileApp.finTra.Controller;

import com.mobileApp.finTra.DTO.TransferRequest;
import com.mobileApp.finTra.Entity.TransactionModel;
import com.mobileApp.finTra.Entity.UserModel;
import com.mobileApp.finTra.Repository.TransactionRepository;
import com.mobileApp.finTra.Repository.UserRepository;
import com.mobileApp.finTra.Service.PaystackService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
public class PaystackController {

    private final TransactionRepository transactionRepository;
    private final PaystackService paystackService;
    private final UserRepository userRepository;

    public PaystackController(TransactionRepository transactionRepository, PaystackService paystackService, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.paystackService = paystackService;
        this.userRepository = userRepository;
    }

    @PostMapping("/topup")
    public ResponseEntity<?> topUp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        int amount = Integer.parseInt(body.get("amount"));
        Long userId = Long.parseLong(body.get("userId"));
        String paymentType = body.get("type");
        String callbackUrl = body.get("callback_url");

        String reference = UUID.randomUUID().toString();

        transactionRepository.save(new TransactionModel(reference, userId, "TOPUP", amount, "PENDING"));

        String authorizationUrl = paystackService.initializeTransaction(email, amount, reference, paymentType, callbackUrl);

        return ResponseEntity.ok(Map.of(
                "reference", reference,
                "authorization_url", authorizationUrl
        ));
    }

    @Transactional
    @GetMapping("/verify/{reference}")
    public ResponseEntity<?> verify(@PathVariable String reference) {
        Map<String, Object> result = paystackService.verifyTransaction(reference);

        String status = (String) ((Map<?, ?>) result.get("data")).get("status");
        String email = (String) ((Map<?, ?>) result.get("data")).get("customer.email");
        int amount = (int) ((Map<?, ?>) result.get("data")).get("amount");

        TransactionModel trx = transactionRepository.findByReference(reference);
        if (trx == null || !"PENDING".equals(trx.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or already verified");
        }

        if ("success".equals(status)) {
            trx.setStatus("SUCCESS");
            transactionRepository.save(trx);

            UserModel user = userRepository.findById(trx.getUserId()).orElseThrow();
            user.setBalance(user.getBalance() + amount);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "Top-up successful",
                    "balance", user.getBalance()
            ));
        } else {
            trx.setStatus("FAILED");
            transactionRepository.save(trx);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Top-up failed");
        }
    }

    @Transactional
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody Map<String, String> body) {
        Long userId = Long.parseLong(body.get("userId"));
        int amount = Integer.parseInt(body.get("amount")); // in pesewas
        String accountNumber = body.get("accountNumber");
        String bankCode = body.get("bankCode");
        String name = body.get("name");

        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.getBalance() < amount) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient balance");
        }

        // Deduct balance
        user.setBalance(user.getBalance() - amount);
        userRepository.save(user);

        // Save withdrawal transaction
        String reference = UUID.randomUUID().toString();
        transactionRepository.save(new TransactionModel(reference, userId, "WITHDRAWAL", amount, "SUCCESS"));

        return ResponseEntity.ok(Map.of(
                "message", "Withdrawal processed successfully",
                "balance", user.getBalance()
        ));
    }


    @PostMapping("/transfer")
    public ResponseEntity<?> transferBetweenUsers(@RequestBody TransferRequest request) {
        if (request.senderEmail.equals(request.receiverEmail)) {
            return ResponseEntity
                    .badRequest()
                    .body("Cannot transfer to the same account");
        }

        UserModel sender = userRepository.findByEmail(request.senderEmail).orElse(null);
        if (sender == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sender not found");
        }

        UserModel receiver = userRepository.findByEmail(request.receiverEmail).orElse(null);
        if (receiver == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Receiver not found");
        }

        if (sender.getBalance() < request.amount) {
            return ResponseEntity.badRequest().body("Insufficient funds");
        }

        sender.setBalance(sender.getBalance() - request.amount);
        userRepository.save(sender);

        receiver.setBalance(receiver.getBalance() + request.amount);
        userRepository.save(receiver);

        String senderReference = UUID.randomUUID().toString();
        transactionRepository.save(new TransactionModel(
                senderReference,
                sender.getId(),
                "TRANSFER",
                request.amount,
                "SUCCESS"
        ));

        String receiverReference = UUID.randomUUID().toString();
        transactionRepository.save(new TransactionModel(
                receiverReference,
                receiver.getId(),
                "INCOME",
                request.amount,
                "SUCCESS"

        ));

        return ResponseEntity.ok(Map.of(
                "message", "Transfer successful",
                "balance", sender.getBalance()
        ));
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody Map<String, Object> payload) {
        System.out.println("webhook hit");
        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        String reference = (String) data.get("reference");
        String status = (String) data.get("status");

        if ("success".equals(status)) {
            TransactionModel trx = transactionRepository.findByReference(reference);
            if (trx != null && "PENDING".equals(trx.getStatus())) {
                trx.setStatus("SUCCESS");
                transactionRepository.save(trx);

                UserModel user = userRepository.findById(trx.getUserId()).orElseThrow();
                user.setBalance(user.getBalance() + trx.getAmount());
                userRepository.save(user);
            }
        }

        return ResponseEntity.ok().build();
    }
}
