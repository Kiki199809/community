package com.it.community.util;

/**
 * @author: KiKi
 * @date: 2021/9/29 - 19:39
 * @project_name：community
 * @description:
 */
public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";
    private static final String PREFIX_POST = "post";

    // 某个实体(帖子、评论和回复)的赞在Redis中的key
    // like:entity:entityType:entityId -> 作为一个set的key，value为点赞者的id的集合
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 某个用户收到的赞
    // like:user:userId -> int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 某个用户关注的实体（用户，帖子）
    // followee:userId:entityType -> zset(entityId,now) 有序集合，按照时间排序
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体（用户，帖子）的粉丝
    // follower:entityType:entityId -> zset(userId,now) 有序集合，按照时间排序
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // 登录验证码
    // kaptcha:owner owner为随机生成字符串，存入cookie，60s过期 -> value为验证码的text
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 登录的凭证
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 用户信息缓存
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    // 单日UV独立访客（一个日期为一个HyperLogLog），用于统计
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    // 区间UV独立访客（对单日UV的合并，新增一个HyperLogLog），用于查询
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 单日DAU活跃用户（以userId作为索引存入Bitmap，一个日期为一个Bitmap），用于统计
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    // 区间DAU活跃用户（对单日DAU的合并，或运算，新增一个Bitmap），用于查询
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    // 帖子分数（用一个set存储分数有变化帖子的id（新增，评论，加精，点赞），在一个定时任务中去重新计算它们的分数）
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }
}
