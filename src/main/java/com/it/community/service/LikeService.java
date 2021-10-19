package com.it.community.service;

import com.it.community.util.RedisKeyUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: KiKi
 * @date: 2021/9/29 - 19:45
 * @project_name：community
 * @description:
 */
@Service
public class LikeService{

    @Resource
    private RedisTemplate redisTemplate;


    /**
     * Description: 返回某个用户收到的点赞数量
     *
     * @param userId:
     * @return int:
     */
    public int findUserLikeCount(int userId) {

        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }

    /**
     * Description: 点赞功能，双击取消
     *
     * @param userId:       点赞者id
     * @param entityType:   待点赞的实体类型
     * @param entityId:     待点赞的实体Id
     * @param entityUserId: 被点赞者Id
     * @return void:
     */
    public void like(int userId, int entityType, int entityId, int entityUserId) {

        // 编程式事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                // 生成当前实体的点赞在Redis中的key -> set
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                // 生成当前实体的User收到的点赞在Redis中的key -> int
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                // 判断Redis中是否有此用户对当前实体的点赞(集合set)
                boolean isMember = redisOperations.opsForSet().isMember(entityLikeKey, userId);

                // 开启事务
                redisOperations.multi();


                if (isMember) {
                    // 有点赞，双击取消点赞，用户收到的赞的数量-1
                    redisOperations.opsForSet().remove(entityLikeKey, userId);
                    redisOperations.opsForValue().decrement(userLikeKey);
                } else {
                    // 没有点赞，记录点赞，用户收到的赞的数量+1
                    redisOperations.opsForSet().add(entityLikeKey, userId);
                    redisOperations.opsForValue().increment(userLikeKey);
                }

                // 提交事务
                return redisOperations.exec();
            }
        });
    }

    /**
     * Description: 查询某个实体点赞的数量
     *
     * @param entityType:
     * @param entityId:
     * @return long:
     */
    public long findEntityLikeCount(int entityType, int entityId) {
        // 生成当前实体的点赞在Redis中的key
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    /**
     * Description: 查询某人对某实体的点赞状态
     *
     * @param userId:
     * @param entityType:
     * @param entityId:
     * @return int: 1--已点赞；0--未点赞
     */
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        // 生成当前实体的点赞在Redis中的key
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }
}
