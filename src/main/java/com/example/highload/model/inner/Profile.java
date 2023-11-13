package com.example.highload.model.inner;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "profile", schema = "public")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    User user;
    @OneToOne
    @JoinColumn(name = "image_id", referencedColumnName = "id")
    Image image;

    @NotBlank
    @Column(name = "name", nullable = false)
    String name;
    @Column(name = "experience")
    String experience;
    @Column(name = "education")
    String education;
    @Column(name = "about")
    String about;

    @NotBlank
    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{1,4}$")
    @Column(name = "mail", nullable = false)
    String mail;

    @ManyToMany
    @JoinTable(
            name = "profile_images",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "image_id"))
    List<Image> images;

    @OneToMany(mappedBy = "receiverProfile")
    List<Notification> receivedNotifications;

    @OneToMany(mappedBy = "senderProfile")
    List<Notification> sentNotifications;
}
