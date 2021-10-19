package com.it.community.quartz;

import com.it.community.entity.DiscussPost;
import com.it.community.service.DiscussPostService;
import com.it.community.service.ElasticsearchService;
import com.it.community.service.LikeService;
import com.it.community.util.CommunityConstant;
import com.it.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: KiKi
 * @date: 2021/10/10 - 22:23
 * @project_name：community
 * @description:
 */

// 刷新帖子分数任务
public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    // 社区纪元
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化社区纪元失败!", e);
        }
    }

    // 要执行的任务
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(postScoreKey);

        if (operations.size() == 0) {
            logger.info("任务取消，没有要刷新的帖子！");
        }

        logger.info("任务开始，正在刷新帖子分数：" + operations.size());
        // 从集合中pop出待刷新分数的帖子Id
        while (operations.size() > 0) {
            this.refresh((Integer) operations.pop());
        }
        logger.info("任务结束，帖子分数刷新完毕！");
    }

    // 刷新某个帖子分数
    private void refresh(int postId) {

        DiscussPost post = discussPostService.queryById(postId);
        // 刷新时帖子是否还存在
        if (post == null) {
            logger.error("该帖子不存在：id = " + postId);
        }

        // 是否精华
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        // 计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // 分数 = 帖子权重 + 距离天数
        double score = Math.log10(Math.max(w, 1))
                + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
        // 更新帖子分数
        discussPostService.updateScore(postId, score);
        // 同步ES搜索数据
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);

    }
}
