package com.example.highload.model.inner;

import com.example.highload.model.enums.ImageObjectType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "image_object", schema = "public")
public class ImageObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    ImageObjectType type;

    @ManyToOne
    @JoinColumn(name = "profile_id", referencedColumnName = "id")
    Profile profile;

    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    ClientOrder order;

    @OneToOne
    @JoinColumn(name = "image_id", referencedColumnName = "id", nullable = false)
    Image image;
}
