package com.it.community.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * (DiscussPost)实体类
 *
 * @author makejava
 * @since 2021-09-13 15:54:54
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
// 将实体与ES内的索引对应，分为6片，备份3
@Document(indexName = "discusspost", type = "_doc", shards = 6, replicas = 3)
public class DiscussPost {
    //private static final long serialVersionUID = 270601098922567852L;

    @Id
    private Integer id;

    @Field(type = FieldType.Integer)
    private Integer userId;

    // 待搜索的字段，分词器ik_max_word尽可能拆分成词，分词器ik_smart在搜索时拆出较少的词
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    // 待搜索的字段，分词器ik_max_word尽可能拆分成词，分词器ik_smart在搜索时拆出较少的词
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    /**
     * 0-普通; 1-置顶;
     */
    @Field(type = FieldType.Integer)
    private Integer type;

    /**
     * 0-正常; 1-精华; 2-拉黑;
     */
    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Date)
    private Date createTime;

    @Field(type = FieldType.Integer)
    private Integer commentCount;

    @Field(type = FieldType.Double)
    private Double score;

}