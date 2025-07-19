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

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
public class PaystackController {
    private final TransactionRepository transactionRepository;
    private final PaystackService paystackService;
    private final UserRepository userRepository;

    public PaystackController(TransactionRepository transactionRepository,PaystackService paystackService, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.paystackService = paystackService;
        this.userRepository = userRepository;
    }

//    @PostMapping("/topup")
    @PostMapping("/topup")
    public ResponseEntity<?> topUp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        int amount = Integer.parseInt(body.get("amount"));
        Long userId = Long.parseLong(body.get("userId"));
        String paymentType = body.get("type"); // "card" or "momo"
        String callbackUrl = body.get("callback_url"); // ✅ read from frontend

        String reference = UUID.randomUUID().toString();

        // Save transaction
        transactionRepository.save(new TransactionModel(reference, userId, "TOPUP", amount, "PENDING"));

        // Send callbackUrl to Paystack
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

            // Update balance
            UserModel user = userRepository.findById(trx.getUserId()).orElseThrow();
            user.setBalance(user.getBalance() + amount);
            userRepository.save(user);

            return ResponseEntity.ok("Top-up successful");
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

        UserModel user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.getBalance() < amount) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient balance");
        }

        // Save transaction
        String reference = UUID.randomUUID().toString();
        transactionRepository.save(new TransactionModel(reference, userId, "WITHDRAWAL", amount, "PENDING"));

        String recipientCode = paystackService.createTransferRecipient(name, accountNumber, bankCode);
        String transferResponse = paystackService.initiateTransfer(amount, recipientCode, "Withdrawal by " + user.getEmail());

        // Update balance
        user.setBalance(user.getBalance() - amount);
        userRepository.save(user);
        TransactionModel trx = transactionRepository.findByReference(reference);
        trx.setStatus("SUCCESS");
        transactionRepository.save(trx);

        return ResponseEntity.ok("Withdrawal initiated");
    }

    @PostMapping("/confirm-withdrawal")
    public ResponseEntity<?> confirmWithdrawal(@RequestBody Map<String, String> body) {
        String transferCode = body.get("transferCode");
        String otp = body.get("otp");

        String result = paystackService.finalizeTransfer(transferCode, otp);
        return ResponseEntity.ok(result);
    }


    
    @PostMapping("/transfer")
    public ResponseEntity<?> transferBetweenUsers(@RequestBody TransferRequest request) {
        if (request.senderEmail.equals(request.receiverEmail)) {
            return ResponseEntity
                    .badRequest()
                    .body("Cannot transfer to the same account");
        }

        UserModel sender = userRepository.findByEmail(request.senderEmail)
                .orElse(null);

        if (sender == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Sender not found");
        }

        UserModel receiver = userRepository.findByEmail(request.receiverEmail)
                .orElse(null);

        if (receiver == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Receiver not found");
        }

        if (sender.getBalance() < request.amount) {
            return ResponseEntity
                    .badRequest()
                    .body("Insufficient funds");
        }

        // Debit sender
        sender.setBalance(sender.getBalance() - request.amount);
        userRepository.save(sender);

        // Credit receiver
        receiver.setBalance(receiver.getBalance() + request.amount);
        userRepository.save(receiver);

        return ResponseEntity.ok("Transfer successful");
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
