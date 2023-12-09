package com.example.highload.repos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<ClientOrder, Integer> {

    Page<ClientOrder> findAllByUser_Id(Integer id, Pageable pageable);
    Page<ClientOrder> findAllByUser_IdAndStatus(Integer id, OrderStatus status, Pageable pageable);
    Page<ClientOrder> findAllByTags_Name(String name, Pageable pageable);
    Page<ClientOrder> findAllByTags_Id(Integer id, Pageable pageable);


    @Query(value = "select * from public.order where id in " +
            "(select order_id from order_tags " +
            "where tag_id in :tagIds " +
            "group by order_tags.order_id " +
            "having count(order_id) = :tagNum)", nativeQuery = true)
    Page<ClientOrder> findAllByMultipleTagsIds(@Param("tagIds") List<Integer> tagIds,
                                               @Param("tagNum") int tagNum,
                                               Pageable pageable);

    @Query(value = "select * from public.order where id in " +
            "(select order_id from order_tags " +
            "where tag_id in :tagIds " +
            "group by order_tags.order_id " +
            "having count(order_id) = :tagNum) " +
            "and public.order.status = :orderStatus", nativeQuery = true)
    Page<ClientOrder> findAllByMultipleTagsIdsAndStatus(@Param("tagIds") List<Integer> tagIds,
                                                        @Param("tagNum") int tagNum,
                                                        @Param("orderStatus") String status,
                                                        Pageable pageable);



}
