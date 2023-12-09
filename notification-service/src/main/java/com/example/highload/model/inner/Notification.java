package com.example.highload.model.inner;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notification", schema = "public")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "sender_profile_id", referencedColumnName = "id")
    Profile senderProfile;

    @ManyToOne
    @JoinColumn(name = "receiver_profile_id", referencedColumnName = "id")
    Profile receiverProfile;

    @Column(name = "is_read", nullable = false)
    Boolean isRead;

    @Column(name = "time", columnDefinition = "TIMESTAMP", nullable = false)
    LocalDateTime time;

}
