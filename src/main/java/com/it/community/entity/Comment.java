package com.it.community.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (Comment)实体类
 *
 * @author makejava
 * @since 2021-09-22 12:30:28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    //private static final long serialVersionUID = 695413299825546846L;
    
    private Integer id;
    
    private Integer userId;
    /**
    * 1-帖子的评论; 2-评论的评论;
    */
    private Integer entityType;
    /**
    * 评论的id
    */
    private Integer entityId;

    /**
    * 被评论者的id（只发生在回复里）
    */
    private Integer targetId;
    
    private String content;
    
    private Integer status;
    
    private Date createTime;

}