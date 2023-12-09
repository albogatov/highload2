package com.example.user.model.inner;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Entity
@Table(name = "review", schema = "public")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "profile_id", referencedColumnName = "id")
    Profile profile;

    @NotBlank
    @Column(name = "text", nullable = false)
    String text;
}
