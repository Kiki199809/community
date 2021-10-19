package com.it.community.service;

import com.it.community.entity.User;
import com.it.community.util.CommunityConstant;
import com.it.community.util.RedisKeyUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author: KiKi
 * @date: 2021/10/2 - 19:15
 * @project_name：community
 * @description:
 */

@Service
public class FollowService implements CommunityConstant {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private UserService userService;


    /**
     * Description: 查询某个用户关注的用户
     *
     * @param userId:
     * @param offset:
     * @param limit:
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>:
     */
    public List<Map<String, Object>> findFollowees(int userId, long offset, long limit) {

        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        // zset范围倒序查询
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);

        if (targetIds == null) {
            return null;
        }

        // 每个map里面有user信息和关注时间
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.queryById(targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }

        return list;
    }

    /**
     * Description: 查询某个用户的粉丝
     *
     * @param userId:
     * @param offset:
     * @param limit:
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>:
     */
    public List<Map<String, Object>> findFollowers(int userId, long offset, long limit) {

        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        // zset范围倒序查询（一个有序的set接口的实现类）
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);

        if (targetIds == null) {
            return null;
        }

        // 每个map里面有user信息和关注时间
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.queryById(targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }

        return list;
    }

    /**
     * Description: 查询关注的实体的数量(帖子或者用户)
     *
     * @param userId:
     * @param entityType:
     * @return long:
     */
    public long findFolloweeCount(int userId, int entityType) {

        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    /**
     * Description: 查询实体(帖子或者用户)的粉丝数量
     *
     * @param entityType:
     * @param entityId:
     * @return long:
     */
    public long findFollowerCount(int entityType, int entityId) {

        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }


    /**
     * Description: 查询当前用户是否已关注该实体(帖子或者用户)
     *
     * @param userId:
     * @param entityType:
     * @param entityId:
     * @return boolean:
     */
    public boolean hasFollowed(int userId, int entityType, int entityId) {

        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    /**
     * Description: 关注
     *
     * @param userId:     关注者
     * @param entityType: 被关注实体
     * @param entityId:   被关注实体id
     * @return void:
     */
    public void follow(int userId, int entityType, int entityId) {

        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                // 当前用户关注的实体（用户，帖子） zset(entityId,now)
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                // 当前实体（用户，帖子）的粉丝 zset(userId,now)
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                // 启用事务
                redisOperations.multi();

                redisOperations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                redisOperations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());

                // 提交事务
                return redisOperations.exec();
            }
        });
    }

    /**
     * Description: 取消关注
     *
     * @param userId:     取消关注者
     * @param entityType: 被取消关注实体
     * @param entityId:   被取消关注实体id
     * @return void:
     */
    public void unfollow(int userId, int entityType, int entityId) {

        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                // 当前用户关注的实体（用户，帖子） zset(entityId,now)
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                // 当前实体（用户，帖子）的粉丝 zset(userId,now)
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                // 启用事务
                redisOperations.multi();

                redisOperations.opsForZSet().remove(followeeKey, entityId, System.currentTimeMillis());
                redisOperations.opsForZSet().remove(followerKey, userId, System.currentTimeMillis());

                // 提交事务
                return redisOperations.exec();
            }
        });
    }

}
