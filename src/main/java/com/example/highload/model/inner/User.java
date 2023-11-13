package com.example.highload.model.inner;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "user", schema = "public")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;

    @NotBlank
    @Size(min = 1, max = 50)
    @Column(name = "login", nullable = false)
    String login;

    @NotBlank
    @Column(name = "hash_password", nullable = false)
    String hashPassword;

    @ManyToOne
    @JoinColumn(name = "role_id", referencedColumnName = "id", insertable = false, updatable = false)
    Role role;

    @Column(name = "is_actual", nullable = false)
    Boolean isActual;

    @Column(name = "when_deleted_time", columnDefinition = "TIMESTAMP")
    private LocalDateTime whenDeletedTime;

    @OneToMany(mappedBy = "user")
    List<Response> responses;

    @OneToMany(mappedBy = "user")
    List<Order> orders;
}
