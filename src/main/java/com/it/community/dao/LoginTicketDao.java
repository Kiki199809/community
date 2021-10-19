package com.it.community.dao;

import com.it.community.entity.LoginTicket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * (LoginTicket)表数据库访问层
 *
 * @author makejava
 * @since 2021-09-15 14:41:27
 */

@Mapper
@Deprecated
public interface LoginTicketDao {

    /**
     * 通过Ticket更新凭证状态
     *
     * @param ticket 登录凭证
     * @return 实例对象
     */
    int updateStatusByTicket(String ticket, int status);

    /**
     * 通过Ticket查询单条数据
     *
     * @param ticket 登录凭证
     * @return 实例对象
     */
    LoginTicket queryByTicket(String ticket);

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    LoginTicket queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<LoginTicket> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询（null为查询全部）
     *
     * @param loginTicket 实例对象
     * @return 对象列表
     */
    List<LoginTicket> queryAll(LoginTicket loginTicket);

    /**
     * 新增数据
     *
     * @param loginTicket 实例对象
     * @return 影响行数
     */
    int insert(LoginTicket loginTicket);

    /**
     * 修改数据
     *
     * @param loginTicket 实例对象
     * @return 影响行数
     */
    int update(LoginTicket loginTicket);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}