package com.anderson.msnotification.repository;

import com.anderson.msnotification.model.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<NotificationLog, String> {

    List<NotificationLog> findByCustomerIdOrderBySentAtDesc(String customerId);

    List<NotificationLog> findByCustomerIdAndDeliveredFalse(String customerId);

    Page<NotificationLog> findByCustomerId(String customerId, Pageable pageable);
}
