package com.it.community;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

/**
 * @author: KiKi
 * @date: 2021/9/29 - 16:13
 * @project_name：community
 * @description:
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    // 统计20万个重复数据的独立总数.
    @Test
    public void testHyperLogLog() {
        String redisKey = "test:hll:01";

        for (int i = 1; i <= 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }

        for (int i = 1; i <= 100000; i++) {
            int r = (int) (Math.random() * 100000 + 1);
            redisTemplate.opsForHyperLogLog().add(redisKey, r);
        }

        long size = redisTemplate.opsForHyperLogLog().size(redisKey);
        System.out.println(size);
    }

    // 将3组数据合并, 再统计合并后的重复数据的独立总数.
    @Test
    public void testHyperLogLogUnion() {
        String redisKey2 = "test:hll:02";
        for (int i = 1; i <= 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2, i);
        }

        String redisKey3 = "test:hll:03";
        for (int i = 5001; i <= 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey3, i);
        }

        String redisKey4 = "test:hll:04";
        for (int i = 10001; i <= 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey4, i);
        }

        String unionKey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionKey, redisKey2, redisKey3, redisKey4);

        long size = redisTemplate.opsForHyperLogLog().size(unionKey);
        System.out.println(size);
    }

    // 统计一组数据的布尔值
    @Test
    public void testBitMap() {
        String redisKey = "test:bm:01";

        // 记录
        redisTemplate.opsForValue().setBit(redisKey, 1, true);
        redisTemplate.opsForValue().setBit(redisKey, 4, true);
        redisTemplate.opsForValue().setBit(redisKey, 7, true);

        // 查询
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));

        // 统计true的个数
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(redisKey.getBytes());
            }
        });

        System.out.println(obj);
    }

    // 统计3组数据的布尔值, 并对这3组数据做OR运算.
    @Test
    public void testBitMapOperation() {
        String redisKey2 = "test:bm:02";
        redisTemplate.opsForValue().setBit(redisKey2, 0, true);
        redisTemplate.opsForValue().setBit(redisKey2, 1, true);
        redisTemplate.opsForValue().setBit(redisKey2, 2, true);

        String redisKey3 = "test:bm:03";
        redisTemplate.opsForValue().setBit(redisKey3, 2, true);
        redisTemplate.opsForValue().setBit(redisKey3, 3, true);
        redisTemplate.opsForValue().setBit(redisKey3, 4, true);

        String redisKey4 = "test:bm:04";
        redisTemplate.opsForValue().setBit(redisKey4, 4, true);
        redisTemplate.opsForValue().setBit(redisKey4, 5, true);
        redisTemplate.opsForValue().setBit(redisKey4, 6, true);

        // 相当于多个字节相应的每个位进行位运算
        String redisKey = "test:bm:or";
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), redisKey2.getBytes(), redisKey3.getBytes(), redisKey4.getBytes());
                return connection.bitCount(redisKey.getBytes());
            }
        });

        System.out.println(obj);

        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 3));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 4));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 5));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 6));
    }

    @Test
    public void test() {
        String redisKey = "test:count";
        redisTemplate.opsForValue().set(redisKey, 1);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));

        String redisKey1 = "test:user";
        redisTemplate.opsForHash().put(redisKey1,"id",1);
        redisTemplate.opsForHash().put(redisKey1,"username","zhangsan");
        System.out.println(redisTemplate.opsForHash().get(redisKey1, "id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey1, "username"));

        String redisKey2 = "test:ids";
        redisTemplate.opsForList().leftPush(redisKey2, 101);
        redisTemplate.opsForList().leftPush(redisKey2, 102);
        redisTemplate.opsForList().leftPush(redisKey2, 103);
        System.out.println(redisTemplate.opsForList().size(redisKey2));
        System.out.println(redisTemplate.opsForList().index(redisKey2, 0));
        System.out.println(redisTemplate.opsForList().range(redisKey2, 0, 2));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey2));

        String redisKey3 = "test:teachers";
        redisTemplate.opsForSet().add(redisKey3, "aaa", "bbb", "ccc", "ddd");
        System.out.println(redisTemplate.opsForSet().size(redisKey3));
        // 随机弹出
        System.out.println(redisTemplate.opsForSet().pop(redisKey3));
        System.out.println(redisTemplate.opsForSet().members(redisKey3));

        String redisKey4 = "test:students";
        redisTemplate.opsForZSet().add(redisKey4, "aaa", 80);
        redisTemplate.opsForZSet().add(redisKey4, "bbb", 50);
        redisTemplate.opsForZSet().add(redisKey4, "ccc", 70);
        System.out.println(redisTemplate.opsForZSet().zCard(redisKey4));
        System.out.println(redisTemplate.opsForZSet().score(redisKey4, "aaa"));
        System.out.println(redisTemplate.opsForZSet().rank(redisKey4, "aaa"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey4, "aaa"));
        System.out.println(redisTemplate.opsForZSet().range(redisKey4, 0, 3));

        redisTemplate.delete("test:user");
        System.out.println(redisTemplate.hasKey("test:user"));
        redisTemplate.expire("test:students", 10, TimeUnit.SECONDS);

        // 多次访问同一个key
        String redisKey5 = "test:counts";
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey5);
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());

    }

    // 编程式事务
    // redis事务是把操作放入队列一起执行，所以其中不要做查询操作
    @Test
    public void test1() {
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String redisKey = "test:tx";
                // 启用事务
                redisOperations.multi();

                redisOperations.opsForSet().add(redisKey, "aaa");
                redisOperations.opsForSet().add(redisKey, "bbb");
                redisOperations.opsForSet().add(redisKey, "ccc");

                // 提交事务
                return redisOperations.exec();
            }
        });
        System.out.println(obj);
    }
}
