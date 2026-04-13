package com.manus.digitalecosystem.repository;

import com.manus.digitalecosystem.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    Page<Notification> findByRecipientUserIdOrderByCreatedAtDesc(String recipientUserId, Pageable pageable);

    Page<Notification> findByRecipientUserIdAndReadOrderByCreatedAtDesc(String recipientUserId, boolean read, Pageable pageable);

    Optional<Notification> findByIdAndRecipientUserId(String id, String recipientUserId);
}

