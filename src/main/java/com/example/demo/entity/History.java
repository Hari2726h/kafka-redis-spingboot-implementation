package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "history",
    indexes = {
        @Index(name = "idx_history_object", columnList = "object_type, object_id"),
        @Index(name = "idx_history_created_at", columnList = "created_at")
    }
)
@Getter
@Setter
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String objectType;

    private Long objectId;

    private String action;

    private LocalDateTime createdAt;
}