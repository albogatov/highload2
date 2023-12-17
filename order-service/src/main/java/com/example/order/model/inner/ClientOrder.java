package com.example.order.model.inner;

import com.example.order.model.enums.OrderStatus;
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
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private Integer userId;

    @Column(name = "created", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime created;

    @Min(0)
    @Column(name = "price", nullable = false)
    private Integer price;

    @NotBlank
    @Column(name = "description", nullable = false)
    private String description;

    @ManyToMany
    @JoinTable(
            name = "order_tags",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @Size(max=10)
    private List<Tag> tags;

    // TODO ???
//    @OneToMany(mappedBy = "order")
//    List<ImageObject> images;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;
}
