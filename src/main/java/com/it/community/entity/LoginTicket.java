package com.it.community.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (LoginTicket)实体类
 *
 * @author makejava
 * @since 2021-09-15 14:41:27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginTicket {
    //private static final long serialVersionUID = -87386595583697486L;
    
    private Integer id;
    
    private Integer userId;
    
    private String ticket;
    /**
    * 0-有效; 1-无效;
    */
    private Integer status;
    
    private Date expired;

}