package com.bagrov.tgzakupkibot.model.repository;

import com.bagrov.tgzakupkibot.model.Purchase;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface PurchaseRepository extends CrudRepository<Purchase, Long> {

    @Query("SELECT p FROM purchaseTable p WHERE p.startDate = :date")
    Iterable<Purchase> findPurchasesByStartDate(String date);

}
