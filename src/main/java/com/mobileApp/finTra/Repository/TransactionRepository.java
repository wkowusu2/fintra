package com.mobileApp.finTra.Repository;

import com.mobileApp.finTra.Entity.TransactionModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionModel,Long> {
    TransactionModel findByReference(String reference);

    List<TransactionModel> findByUserIdOrderByCreatedAtDesc(Long userId);
}
