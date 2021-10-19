package com.it.community.service;

import com.it.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author: KiKi
 * @date: 2021/10/9 - 15:39
 * @project_name：community
 * @description:
 */

@Service
public class DataService {

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 当前时间格式化作为Redis的key的一部分
     */
    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    /**
     * Description: 将指定的IP计入UV独立访客
     *
     * @param ip:
     * @return void:
     */
    public void recordUV(String ip) {
        String redisKey = RedisKeyUtil.getUVKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey, ip);
    }

    /**
     * Description: 统计指定日期范围内的UV独立访客数量
     *
     * @param start:
     * @param end:
     * @return long:
     */
    public long calculateUV(Date start, Date end) {

        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 整理该日期范围内的key存入List
        List<String> keyList = new ArrayList<>();

        // Calendar用于遍历日期
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(key);
            // 日期加一天
            calendar.add(Calendar.DATE, 1);
        }

        // 合并这些数据存入Redis
        String redisKey = RedisKeyUtil.getUVKey(df.format(start), df.format(end));
        redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray());

        // 返回统计数量
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    /**
     * Description: 将指定用户计入DAU日活跃用户（以userId作为索引存入Bitmap）
     *
     * @param userId:
     * @return void:
     */
    public void recordDAU(int userId) {
        String redisKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey, userId, true);
    }

    //
    /**
     * Description: 统计指定日期范围内的DAU活跃用户
     *              指定日期内登录过就算活跃用户
     * @param start:
     * @param end:
     * @return long:
     */
    public long calculateDAU(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 整理该日期范围内的key
        List<byte[]> keyList = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            keyList.add(key.getBytes());
            // 日期加一天
            calendar.add(Calendar.DATE, 1);
        }

        // 进行OR运算
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                // 新增一个key
                String redisKey = RedisKeyUtil.getDAUKey(df.format(start), df.format(end));
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), keyList.toArray(new byte[0][0]));
                return connection.bitCount(redisKey.getBytes());
            }
        });
    }
}
