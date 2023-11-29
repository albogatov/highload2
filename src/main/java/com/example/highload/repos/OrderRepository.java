package com.example.highload.repos;

import com.example.highload.model.enums.OrderStatus;
import com.example.highload.model.inner.ClientOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<ClientOrder, Integer> {

    Page<ClientOrder> findAllByUser_Id(Integer id, Pageable pageable);
    Page<ClientOrder> findAllByUser_IdAndStatus(Integer id, OrderStatus status, Pageable pageable);
    Page<ClientOrder> findAllByTags_Name(String name, Pageable pageable);
    Page<ClientOrder> findAllByTags_Id(Integer id, Pageable pageable);

    @Query(value = "select * from order where id in (select order.id from order " +
            "join order_tags on order.id = order_tags.order_id " +
            "where order_tags.tag_id in :tagIds)" , nativeQuery = true)
    Page<ClientOrder> findAllByMultipleTagsIds(@Param("tagIds") List<Integer> tagIds, Pageable pageable);

    @Query(value = "select * from order where id in (select order.id from order " +
            "join order_tags on order.id = order_tags.order_id " +
            "where order_tags.tag_id in :tagIds and order.status = :orderStatus)" , nativeQuery = true)
    Page<ClientOrder> findAllByMultipleTagsIdsAndStatus(@Param("tagIds") List<Integer> tagIds,
                                                        @Param("orderStatus")String status,
                                                        Pageable pageable);

}
