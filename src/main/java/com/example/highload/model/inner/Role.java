package com.example.highload.model.inner;

import com.example.highload.model.enums.OrderStatus;
import com.example.highload.model.enums.RoleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Entity
@Table(name = "role", schema = "public")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false)
    RoleType name;
}
