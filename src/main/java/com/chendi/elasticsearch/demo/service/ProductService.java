package com.chendi.elasticsearch.demo.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chendi.elasticsearch.demo.dto.QueryReq;
import com.chendi.elasticsearch.demo.entity.db.Product;
import com.chendi.elasticsearch.demo.entity.es.ESProduct;
import com.chendi.elasticsearch.demo.entity.mybatis.TProduct;
import com.chendi.elasticsearch.demo.repository.ProductRepository;
import com.chendi.elasticsearch.demo.repository.ESProductRepository;
import com.chendi.elasticsearch.demo.repository.impl.TProductRepositoryImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.ParsedStats;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor //构造器注入
public class ProductService {
    private final ProductRepository productRepository;
    private final ESProductRepository esProductRepository;
    private final TransactionTemplate transactionTemplate;
    private final TProductRepositoryImpl tProductService;
    private final RestHighLevelClient restHighLevelClient;

    public void addProduct(Product product) {
        //保存数据库
        //product.setCreateTime(new Date());
        //product.setUpdateTime(new Date());
//        final Product saveProduct = transactionTemplate.execute((status) ->
//                productRepository.save(product)
//        );

        TProduct saveProduct = new TProduct();
        BeanUtils.copyProperties(product, saveProduct);
        saveProduct.setSharding_id(IdUtil.getSnowflake(1).nextId());

        //批量插入存在默认值没有复制的 bug，比如createTime，id
//        Integer insertCnt = tProductService.insertBatchSomeColumn(Arrays.asList(saveProduct));
//        assert insertCnt>0;
        boolean save = tProductService.save(saveProduct);
        assert save;
        final ESProduct esProduct = new ESProduct();
        BeanUtils.copyProperties(saveProduct, esProduct);
        esProduct.setId(saveProduct.getSharding_id().toString());
        //es的_id是字符串
        esProduct.setId(saveProduct.getSharding_id() + "");
        try {
            esProductRepository.save(esProduct);
        } catch (Exception e) {
            log.error(String.format("保存ES错误！%s", e.getMessage()));
        }
    }

    public Map<String, Object> searchProduct(QueryReq queryReq) throws IOException {
        Map<String, Object> result = new HashMap<>();

        long totalHits = 0L;
        List<Object> list = new ArrayList<>();

        if (queryReq.getSearch_type().equals((byte) 0)) {
            SearchResponse searchResponse = searchProductByEs(queryReq);
            SearchHit[] hits = searchResponse.getHits().getHits();
            ObjectMapper objectMapper = new ObjectMapper();
            for (SearchHit hit : hits) {
                ESProduct esProduct = objectMapper.readValue(hit.getSourceAsString(), ESProduct.class);
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                if (highlightFields.containsKey("title")) {
                    esProduct.setTitle(highlightFields.get("title").getFragments()[0].toString());
                }
                if (highlightFields.containsKey("description")) {
                    esProduct.setDescription(highlightFields.get("description").getFragments()[0].toString());
                }
                list.add(esProduct);
            }
            totalHits = searchResponse.getHits().getTotalHits().value;
//            org.springframework.data.domain.Page<ESProduct> esProductPage = searchProductByEsV2(queryReq);
//            list = new ArrayList<>(esProductPage.getContent());
//            totalHits = esProductPage.getTotalElements();
        } else {
            Page<TProduct> tProductPage = searchProductByDb(queryReq);
            list = new ArrayList<>(tProductPage.getRecords());
            totalHits = tProductPage.getTotal();
        }
        result.put("data", list);
        result.put("count", totalHits);
        result.put("code", 0);
        return result;
    }

    private org.springframework.data.domain.Page<ESProduct> searchProductByEsV2(QueryReq queryReq) {
        PageRequest idPage = PageRequest.of(queryReq.getPage() - 1, queryReq.getLimit(), Sort.Direction.DESC, "id");
        if (StringUtils.isEmpty(queryReq.getKeyword())) {
            return this.esProductRepository.findAll(idPage);
        }
        return this.esProductRepository.findByTitleOrDescription(queryReq.getKeyword(), queryReq.getKeyword(), idPage);
    }


    private SearchResponse searchProductByEs(QueryReq queryReq) throws IOException {
        // 指定只能在哪些文档库中查询：可以添加多个且没有限制，中间用逗号隔开 --- 默认是去所有文档库中进行查询
        SearchRequest searchRequest = new SearchRequest("es_product");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS)); //设置超时时间
        String[] includeFields = new String[]{"id", "title", "price", "description", "createTime"};
        String[] excludeFields = new String[]{""};

        //多字段高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        HighlightBuilder.Field highlightTitle = new HighlightBuilder.Field("title");
        highlightBuilder.field(highlightTitle);
        HighlightBuilder.Field highlightDescription = new HighlightBuilder.Field("description");
        highlightBuilder.field(highlightDescription);
        highlightBuilder.requireFieldMatch(false).preTags("<span style='color:red;'>").postTags("</span>");

        //sourceBuilder.fetchSource(includeFields, excludeFields);
        sourceBuilder
                //分页
                .from((queryReq.getPage() - 1) * queryReq.getLimit()).size(queryReq.getLimit()).sort("price", SortOrder.DESC)
                .fetchSource(includeFields, excludeFields)
                .highlighter(highlightBuilder);

        BoolQueryBuilder all = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery());

        //检索title和description
        if (!StringUtils.isEmpty(queryReq.getKeyword())) {
            all.filter(QueryBuilders.multiMatchQuery(queryReq.getKeyword(), "description", "title"));
        }

        //价格
        if (queryReq.getMin_price() != null) {
            all.filter(QueryBuilders.rangeQuery("price").gte(queryReq.getMin_price()));
        }

        if (queryReq.getMax_price() != null) {
            all.filter(QueryBuilders.rangeQuery("price").lte(queryReq.getMax_price()));
        }

        sourceBuilder.query(all);
        searchRequest.source(sourceBuilder);
        return restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
    }

    private Page<TProduct> searchProductByDb(QueryReq queryReq) {
        //QueryWrapper 实现分页查询
        QueryWrapper<TProduct> queryWrapper = new QueryWrapper<>();

        //检索title和description
        if (!StringUtils.isEmpty(queryReq.getKeyword())) {
            queryWrapper.like("title", queryReq.getKeyword()).or().like("description", queryReq.getKeyword());
        }
        if (queryReq.getMin_price() != null) {
            queryWrapper.ge("price", queryReq.getMin_price());
        }
        if (queryReq.getMax_price() != null) {
            queryWrapper.le("price", queryReq.getMax_price());
        }
        Page<TProduct> idPage = new Page<TProduct>(queryReq.getPage(), queryReq.getLimit()).addOrder(OrderItem.desc("id"));
        return tProductService.page(idPage, queryWrapper);
    }

    public Map<String, Object> del(List<Integer> ids) throws IOException {
        transactionTemplate.execute((status) -> productRepository.deleteAllByIdIn(ids));
        //删除es中的数据
        BulkRequest bulkRequest = new BulkRequest();
        for (Integer id : ids) {
            DeleteRequest deleteRequest = new DeleteRequest("es_product", id + "");
            bulkRequest.add(deleteRequest);
        }
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        if (bulkResponse.hasFailures()) {
            // 处理批量删除失败的情况
            throw new RuntimeException("Bulk delete failed");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        return result;
    }

    public Map<String, Object> aggregation() throws IOException {
        SearchRequest searchRequest = new SearchRequest("es_product");
        SearchSourceBuilder aggregationBuilder = new SearchSourceBuilder();
        aggregationBuilder.aggregation(AggregationBuilders.stats("priceStatsAgg").field("price"));
        searchRequest.source(aggregationBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        Aggregations aggregations = searchResponse.getAggregations();
        ParsedStats statsAgg = aggregations.get("priceStatsAgg");
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("min", statsAgg.getMin());
        dataMap.put("max", statsAgg.getMax());
        dataMap.put("avg", statsAgg.getAvg());
        dataMap.put("sum", statsAgg.getSum());
        dataMap.put("count", statsAgg.getCount());
        data.add(dataMap);
        result.put("data", data);
        result.put("code", 0);
        return result;
    }
}
