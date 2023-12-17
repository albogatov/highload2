package com.example.order.utils;

import com.example.order.model.inner.*;
import com.example.order.model.network.*;
import com.example.order.repos.*;
import com.example.order.model.inner.ClientOrder;
import com.example.order.model.inner.Response;
import com.example.order.model.inner.Tag;
import com.example.order.model.network.OrderDto;
import com.example.order.model.network.ResponseDto;
import com.example.order.model.network.TagDto;
import com.example.order.repos.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("dataTransformer")
@Data
@AllArgsConstructor
public class DataTransformer {

    private final OrderRepository orderRepository;

    /* users */

    public User userFromDto(UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId());
        user.setLogin(userDto.getLogin());
        user.setHashPassword(userDto.getPassword());
        RoleType roleName = userDto.getRole();
        Role role = roleRepository.findByName(roleName).orElseThrow();
        user.setRole(role);
        user.setIsActual(true);
        return user;
    }

    /* tags */

    public TagDto tagToDto(Tag tag) {
        TagDto tagDto = new TagDto();
        tagDto.setId(tag.getId());
        tagDto.setName(tag.getName());
        return tagDto;
    }

    public Tag tagFromDto(TagDto tagDto) {
        Tag tag = new Tag();
        tag.setId(tagDto.getId());
        tag.setName(tagDto.getName());
        return tag;
    }

    public List<TagDto> tagListToDto(List<Tag> entities) {
        return entities.stream().map(this::tagToDto).toList();
    }

    public List<Tag> tagDtoListToTag(List<TagDto> entities) {
        return entities.stream().map(this::tagFromDto).toList();
    }


    /* responses */

    public ResponseDto responseToDto(Response response) {
        ResponseDto responseDto = new ResponseDto();
        responseDto.setId(response.getId());
        responseDto.setText(response.getText());
        responseDto.setUserId(response.getUser().getId());
        responseDto.setApproved(response.getIsApproved());
        responseDto.setUserName(response.getUser().getLogin());
        responseDto.setOrderId(response.getOrder().getId());
        return responseDto;
    }

    public Response responseFromDto(ResponseDto responseDto) {
        Response response = new Response();
        response.setId(responseDto.getId());
        response.setText(responseDto.getText());
        response.setIsApproved(responseDto.isApproved());
        User user = userRepository.findById(responseDto.getUserId()).orElseThrow();
        response.setUser(user);
        ClientOrder order = orderRepository.findById(responseDto.getOrderId()).orElseThrow();
        response.setOrder(order);
        return response;
    }

    public List<ResponseDto> responseListToDto(List<Response> entities) {
        return entities.stream().map(this::responseToDto).toList();
    }

    /* orders */

    public OrderDto orderToDto(ClientOrder order) {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(order.getId());
        orderDto.setDescription(order.getDescription());
        orderDto.setCreated(order.getCreated());
        orderDto.setPrice(order.getPrice());
        orderDto.setStatus(order.getStatus());
        orderDto.setUserName(order.getUser().getLogin());
        orderDto.setUserId(order.getUser().getId());
        orderDto.setTags(tagListToDto(order.getTags()));
        return orderDto;
    }

    public ClientOrder orderFromDto(OrderDto orderDto) {
        ClientOrder order = new ClientOrder();
        order.setId(orderDto.getId());
        order.setDescription(orderDto.getDescription());
        order.setCreated(orderDto.getCreated());
        order.setPrice(orderDto.getPrice());
        order.setStatus(orderDto.getStatus());
        order.setTags(tagDtoListToTag(orderDto.getTags()));
        User user = userRepository.findById(orderDto.getUserId()).orElseThrow();
        order.setUser(user);
        return order;
    }

    public List<OrderDto> orderListToDto(List<ClientOrder> entities) {
        return entities.stream().map(this::orderToDto).toList();
    }

}
