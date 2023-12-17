package com.example.image.model.inner;

import com.example.image.model.enums.ImageObjectType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "image_object", schema = "public")
public class ImageObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ImageObjectType type;

    @ManyToOne
    @JoinColumn(name = "profile_id", referencedColumnName = "id")
    private Integer profileId;

    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private Integer orderId;

    @OneToOne
    @JoinColumn(name = "image_id", referencedColumnName = "id", nullable = false)
    private Image image;
}
