package com.it.community.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (DiscussPost)实体类
 *
 * @author makejava
 * @since 2021-09-13 15:54:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscussPost {
    //private static final long serialVersionUID = 270601098922567852L;
    
    private Integer id;
    
    private Integer userId;
    
    private String title;
    
    private String content;
    /**
    * 0-普通; 1-置顶;
    */
    private Integer type;
    /**
    * 0-正常; 1-精华; 2-拉黑;
    */
    private Integer status;
    
    private Date createTime;
    
    private Integer commentCount;
    
    private Double score;

}