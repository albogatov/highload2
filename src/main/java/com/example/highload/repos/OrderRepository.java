package com.example.highload.repos;

import com.example.highload.model.enums.OrderStatus;
import com.example.highload.model.inner.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findAllByUser_Id(Integer id, Pageable pageable);

    List<Order> findAllByUser_Id(Integer id);
    List<Order> findAllByUser_IdAndStatus(Integer id, OrderStatus status, Pageable pageable);
    List<Order> findAllByTags_Name(String name, Pageable pageable);
    List<Order> findAllByTags_Id(Integer id, Pageable pageable);

    @Query(value = "select * from order where id in (select order.id from order " +
            "join order_tags on order.id = order_tags.order_id " +
            "where order_tags.tag_id in :tagIds)" , nativeQuery = true)
    List<Order> findAllByMultipleTagsIds(@Param("tagIds") List<Integer> tagIds, Pageable pageable);

    @Query(value = "select * from order where id in (select order.id from order " +
            "join order_tags on order.id = order_tags.order_id " +
            "where order_tags.tag_id in :tagIds and order.is_closed = false)" , nativeQuery = true)
    List<Order> findAllByMultipleTagsIdsAndIsClosedFalse(@Param("tagIds") List<Integer> tagIds, Pageable pageable);

}
