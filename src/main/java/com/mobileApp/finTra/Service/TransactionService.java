package com.mobileApp.finTra.Service;

import com.mobileApp.finTra.Entity.TransactionModel;
import com.mobileApp.finTra.Entity.UserModel;
import com.mobileApp.finTra.Repository.TransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {
    private final TransactionRepository trxRepo;

    public TransactionService(TransactionRepository trxRepo) {
        this.trxRepo = trxRepo;
    }

    public TransactionModel create(String reference, long userId, String type, int amount) {
        TransactionModel trx = new TransactionModel();
        trx.setReference(reference);
        trx.setUserId(userId);
        trx.setType(type);
        trx.setAmount(amount);
        trx.setStatus("PENDING");
        return trxRepo.save(trx);
    }

    public void markSuccess(String reference) {
        TransactionModel trx = trxRepo.findByReference(reference);
        trx.setStatus("SUCCESS");
        trxRepo.save(trx);
    }

    public void markFailed(String reference) {
        TransactionModel trx = trxRepo.findByReference(reference);
        trx.setStatus("FAILED");
        trxRepo.save(trx);
    }
}

