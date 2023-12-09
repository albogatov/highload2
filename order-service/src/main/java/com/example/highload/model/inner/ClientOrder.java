package com.example.highload.model.inner;

import com.example.highload.model.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "order", schema = "public")
public class ClientOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    User user;

    @Column(name = "created", columnDefinition = "TIMESTAMP", nullable = false)
    LocalDateTime created;

    @Min(0)
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
    @Size(max=10)
    List<Tag> tags;

    @OneToMany(mappedBy = "order")
    List<ImageObject> images;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    OrderStatus status;
}
