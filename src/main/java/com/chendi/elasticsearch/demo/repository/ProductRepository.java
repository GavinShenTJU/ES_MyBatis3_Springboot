package com.chendi.elasticsearch.demo.repository;

import com.chendi.elasticsearch.demo.entity.db.Product;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product,Integer> {
    @Modifying
    @Query("delete from t_product t where t.id in (?1)")
    Integer deleteAllByIdIn(List<Integer> ids);
}
