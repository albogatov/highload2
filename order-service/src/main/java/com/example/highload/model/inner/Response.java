package com.example.highload.model.inner;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "response", schema = "public")
public class Response {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    User user;
    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    ClientOrder order;

    @Column(name = "text")
    String text;

    @Column(name = "is_approved", nullable = false)
    Boolean isApproved;
}
