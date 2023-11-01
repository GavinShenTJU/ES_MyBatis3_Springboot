package com.chendi.elasticsearch.demo.entity.db;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "t_product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String title;
    private Double price;
    private String description;
    private Date createTime;
    private Date updateTime;
}
