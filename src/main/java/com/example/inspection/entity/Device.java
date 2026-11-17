package com.example.inspection.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "device")
@Getter @Setter
public class Device {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;
    private String manufacturer;
    private String model;
    private String fingerprint;

    @Column(name = "android_version")
    private String androidVersion;

    @Column(name = "sdk_int")
    private Integer sdkInt;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}