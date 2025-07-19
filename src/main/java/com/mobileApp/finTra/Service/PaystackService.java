package com.mobileApp.finTra.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaystackService {

    @Value("${paystack.secret-key}")
    private String secretKey;

    @Value("${paystack.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public PaystackService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    // 1️⃣ Initialize a payment (Top-up)
    public String initializeTransaction(String email, int amount, String reference, String type, String callbackUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("amount", amount); // in pesewas
        payload.put("reference", reference);
        payload.put("currency", "GHS");
        payload.put("callback_url", callbackUrl); // ✅ Include the callback URL here

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("payment_type", type); // momo or card
        payload.put("metadata", metadata);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/transaction/initialize",
                entity,
                Map.class
        );

        if (response.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(response.getBody().get("status"))) {
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            return (String) data.get("authorization_url");
        }

        throw new RuntimeException("Failed to initialize transaction: " + response.getBody().get("message"));
    }

    // 2️⃣ Verify a transaction (Top-up confirmation)
    public Map<String, Object> verifyTransaction(String reference) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(secretKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/transaction/verify/" + reference,
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(response.getBody().get("status"))) {
            return response.getBody();
        }

        throw new RuntimeException("Failed to verify transaction");
    }

    // 3️⃣ Create transfer recipient (Withdrawal setup)
    public String createTransferRecipient(String name, String accountNumber, String bankCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "mobile_money"); // or "nuban" if it's bank
        payload.put("name", name);
        payload.put("account_number", accountNumber);
        payload.put("bank_code", bankCode); // e.g., for MTN, VOD, ATL, use Paystack Ghana codes
        payload.put("currency", "GHS");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/transferrecipient",
                entity,
                Map.class
        );

        if (response.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(response.getBody().get("status"))) {
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            return (String) data.get("recipient_code");
        }

        throw new RuntimeException("Failed to create transfer recipient");
    }

    // 4️⃣ Initiate transfer (Withdrawal action)
    public String initiateTransfer(int amount, String recipientCode, String reason) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("source", "balance");
        payload.put("amount", amount);
        payload.put("recipient", recipientCode);
        payload.put("reason", reason);
        payload.put("currency", "GHS");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/transfer",
                entity,
                Map.class
        );

        if (response.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(response.getBody().get("status"))) {
            return "Transfer initiated";
        }

        throw new RuntimeException("Failed to initiate transfer: " + response.getBody().get("message"));
    }

    public String finalizeTransfer(String transferCode, String otp) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> payload = new HashMap<>();
        payload.put("transfer_code", transferCode);
        payload.put("otp", otp);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/transfer/finalize_transfer",
                entity,
                Map.class
        );

        if (response.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(response.getBody().get("status"))) {
            return "Transfer finalized successfully";
        }

        throw new RuntimeException("OTP verification failed: " + response.getBody().get("message"));
    }

}

