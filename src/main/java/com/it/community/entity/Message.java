package com.it.community.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (Message)实体类
 *
 * @author makejava
 * @since 2021-09-27 10:09:05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    //private static final long serialVersionUID = -46782791601792611L;
    
    private Integer id;
    
    private Integer fromId;
    
    private Integer toId;
    
    private String conversationId;
    
    private String content;

    /**
    * 0-未读;1-已读;2-删除;
    */
    private Integer status;
    
    private Date createTime;

}