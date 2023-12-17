package com.example.highload.model.inner;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Entity
@Table(name = "image", schema = "public")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Column(name = "url", nullable = false)
    private String url;

    @OneToOne(mappedBy = "image")
    private ImageObject imageObject;

}
