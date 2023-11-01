package com.chendi.elasticsearch.demo.repository;

import com.chendi.elasticsearch.demo.entity.es.ESProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Highlight;
import org.springframework.data.elasticsearch.annotations.HighlightField;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface ESProductRepository extends ElasticsearchRepository<ESProduct, String> {

    @Query("{\"bool\":{\"should\":[{\"match\":{\"title\":\"?0\"}},{\"match\":{\"description\":\"?1\"}}],\"minimum_should_match\":\"1\"}}")
    List<ESProduct> findByTitleOrDescription(String title, String description);

    @Query("{\"bool\":{\"should\":[{\"match\":{\"title\":\"?0\"}},{\"match\":{\"description\":\"?1\"}}],\"minimum_should_match\":\"1\"}}")
    Page<ESProduct> findByTitleOrDescription(String title, String description, Pageable pageable);

    @Highlight(fields = {@HighlightField(name = "title")})
    @Query("{\"match\":{\"title\":\"?0\"}}")
    SearchHits<ESProduct> find(String keyword);

}
