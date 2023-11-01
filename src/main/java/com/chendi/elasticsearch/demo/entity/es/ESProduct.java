package com.chendi.elasticsearch.demo.entity.es;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

@Data
@Document(indexName = "es_product")
public class ESProduct {
    @Id
    @Field(type = FieldType.Text)
    private String id;
    @Field(type=FieldType.Text,analyzer="ik_max_word")
    private String title;
    @Field(type = FieldType.Double)
    private Double price;
    @Field(type=FieldType.Text,analyzer="ik_max_word")
    private String description;
    @Field( type = FieldType.Date,format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern ="yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @Field( type = FieldType.Date,format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern ="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
