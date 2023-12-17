package com.example.notification.model.inner;

import jakarta.persistence.*;
import lombok.Data;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notification", schema = "public")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "sender_profile_id", referencedColumnName = "id")
    private Integer senderProfileId;

    @ManyToOne
    @JoinColumn(name = "receiver_profile_id", referencedColumnName = "id")
    private Integer receiverProfileId;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "time", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime time;

}
