package com.it.community.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (User)实体类
 *
 * @author makejava
 * @since 2021-09-13 15:45:23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    //private static final long serialVersionUID = 621487539946254370L;
    
    private Integer id;
    
    private String username;
    
    private String password;
    
    private String salt;
    
    private String email;
    /**
    * 0-普通用户; 1-超级管理员; 2-版主;
    */
    private Integer type;
    /**
    * 0-未激活; 1-已激活;
    */
    private Integer status;
    
    private String activationCode;
    
    private String headerUrl;
    
    private Date createTime;

}