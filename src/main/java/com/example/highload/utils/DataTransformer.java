package com.example.highload.utils;

import com.example.highload.model.inner.*;
import com.example.highload.model.network.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("dataTransformer")
public class DataTransformer {

    /* users */

    public UserDto userToDto(User user) {
        return null;
    }

    public User userFromDto(UserDto userDto) {
        return null;
    }

    /* tags */

    public TagDto tagToDto(Tag tag) {
        return null;
    }

    public Tag tagFromDto(TagDto tagDto) {
        return null;
    }

    public List<TagDto> tagListToDto(List<Tag> entities) {
        return entities.stream().map(this::tagToDto).toList();
    }


    /* reviews */

    public ReviewDto reviewToDto(Review review) {
        return null;
    }

    public Review reviewFromDto(ReviewDto reviewDto) {
        return null;
    }

    public List<ReviewDto> reviewListToDto(List<Review> entities) {
        return entities.stream().map(this::reviewToDto).toList();
    }

    /* responses */

    public ResponseDto responseToDto(Response response) {
        return null;
    }

    public Response responseFromDto(ResponseDto responseDto) {
        return null;
    }

    public List<ResponseDto> responseListToDto(List<Response> entities) {
        return entities.stream().map(this::responseToDto).toList();
    }


    /* profiles */

    public ProfileDto profileToDto(Profile profile) {
        return null;
    }

    public Profile profileFromDto(ProfileDto profileDto) {
        return null;
    }

    public List<ProfileDto> profileListToDto(List<Profile> entities) {
        return entities.stream().map(this::profileToDto).toList();
    }

    /* orders */

    public OrderDto orderToDto(Order order) {
        return null;
    }

    public Order orderFromDto(OrderDto orderDto) {
        return null;
    }

    public List<OrderDto> orderListToDto(List<Order> entities) {
        return entities.stream().map(this::orderToDto).toList();
    }

    /* notifications */

    public NotificationDto notificationToDto(Notification notification) {
        return null;
    }

    public Notification notificationFromDto(NotificationDto notificationDto) {
        return null;
    }

    public List<NotificationDto> notificationListToDto(List<Notification> entities) {
        return entities.stream().map(this::notificationToDto).toList();
    }

    /* images */

    public ImageDto imageToDto(Image image) {
        return null;
    }

    public Image imageFromDto(ImageDto imageDto) {
        return null;
    }

    public List<ImageDto> imageListToDto(List<Image> entities) {
        return entities.stream().map(this::imageToDto).toList();
    }
}
