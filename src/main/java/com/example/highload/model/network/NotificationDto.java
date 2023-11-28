package com.example.highload.model.network;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class NotificationDto implements Serializable {

    int id;
    int receiverId;
    int senderId;
    boolean isRead;
    LocalDateTime time;
    String senderMail;
}
