package com.example.highload.model.inner;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "order", schema = "public")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    User user;

    @Column(name = "created", columnDefinition = "TIMESTAMP", nullable = false)
    LocalDateTime created;

    @Column(name = "price", nullable = false)
    Integer price;

    @NotBlank
    @Column(name = "description", nullable = false)
    String description;

    @ManyToMany
    @JoinTable(
            name = "order_tags",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    List<Tag> tags;

    @ManyToMany
    @JoinTable(
            name = "order_images",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "image_id"))
    List<Image> images;

    @Column(name = "is_closed", nullable = false)
    Boolean isClosed;
}
