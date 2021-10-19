package com.it.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.it.community.entity.DiscussPost;
import com.it.community.dao.DiscussPostDao;
import com.it.community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * (DiscussPost)表服务实现类
 *
 * @author makejava
 * @since 2021-09-13 15:54:54
 */
@Service
public class DiscussPostService {

    @Resource
    private DiscussPostDao discussPostDao;

    @Resource
    private SensitiveFilter sensitiveFilter;

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    // Caffeine核心接口: Cache, LoadingCache, AsyncLoadingCache

    // 帖子列表缓存（key-value形式）
    private LoadingCache<String, List<DiscussPost>> postListCache;

    // 帖子总数缓存
    private LoadingCache<Integer, Integer> postRowsCache;

    // 被注解的方法，在对象加载完依赖注入后执行
    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        // postListCache.get(key)调用此方法，此方法执行过一次后，下次执行从缓存中取数据
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Nullable
                    @Override
                    public List<DiscussPost> load(@NonNull String key) throws Exception {

                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数错误!");
                        }

                        String[] params = key.split(":");
                        if (params == null || params.length != 2) {
                            throw new IllegalArgumentException("参数错误!");
                        }

                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        // 二级缓存: Redis -> mysql

                        logger.debug("load post list from DB.");
                        // 全部未被拉黑的按热度排序的数据
                        return discussPostDao.selectDiscussPosts(null, offset, limit, 1);
                    }
                });
        // 初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull Integer key) throws Exception {
                        logger.debug("load post rows from DB.");
                        return discussPostDao.selectDiscussPostRows(null);
                    }
                });
    }

    /**
     * Description: 修改帖子类型
     *
     * @param id:
     * @param type:
     * @return int:
     */
    public int updateType(int id, int type) {
        return discussPostDao.updateType(id, type);
    }

    /**
     * Description: 修改帖子状态
     *
     * @param id:
     * @param status:
     * @return int:
     */
    public int updateStatus(int id, int status) {
        return discussPostDao.updateStatus(id, status);
    }

    /**
     * Description: 修改帖子分数
     *
     * @param id:
     * @param score:
     * @return int:
     */
    public int updateScore(int id, double score) {
        return discussPostDao.updateScore(id, score);
    }

    /**
     * Description: 通过id修改帖子评论数量
     *
     * @param id:
     * @param commentCount:
     * @return int:
     */
    public int updateCommentCountById(int id, int commentCount) {
        return discussPostDao.updateCommentCountById(id, commentCount);
    }

    /**
     * Description: 添加帖子（过滤敏感词）
     * @param discussPost:
     * @return int:
     */
    public int addDiscussPost(DiscussPost discussPost) {
        if (discussPost == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        // 转义HTML标记(恶意注册的时候，会使用诸如 <script>alert('papapa')</script>，转义标签)
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        // 过滤敏感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        return discussPostDao.insert(discussPost);
    }

    /**
     * Description: 通过userId查询所有数据总数（排除拉黑status != 2）
     *
     * @param userId:
     * @return int:
     */
    public int findDiscussPostRows(Integer userId) {
        // 查询全部时且按照热帖排序时启用缓存
        if (userId == null) {
            return postRowsCache.get(0);
        }

        logger.debug("load post list from DB.");
        return discussPostDao.selectDiscussPostRows(userId);
    }


    /**
     * Description: 通过userId查询所有数据（排除拉黑status != 2）
     * @param userId: null即为查询所有
     * @param offset:
     * @param limit:
     * @param orderMode: 0-普通排序（置顶，时间） 1-热帖排序（置顶，分数，时间）
     * @return java.util.List<com.it.community.entity.DiscussPost>:
     */
    public List<DiscussPost> findDiscussPosts(Integer userId, int offset, int limit, int orderMode) {
        // 查询全部时且按照热帖排序时启用缓存
        if (userId == null && orderMode == 1) {
            return postListCache.get(offset + ":" + limit);
        }

        logger.debug("load post list from DB.");
        return discussPostDao.selectDiscussPosts(userId, offset, limit, orderMode);
    }


    /**
     * 通过ID查询单条数据
     *
     * @param id 主键

     * @return 实例对象
     */
    public DiscussPost queryById(Integer id) {
        return this.discussPostDao.queryById(id);
    }

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    public List<DiscussPost> queryAllByLimit(int offset, int limit) {
        return this.discussPostDao.queryAllByLimit(offset, limit);
    }
    
    /**
     * 通过实体作为筛选条件查询（null为查询全部）
     *
     * @param discussPost 实例对象
     * @return 对象列表
     */
    public List<DiscussPost> queryAll(DiscussPost discussPost) {
        return this.discussPostDao.queryAll(discussPost);
    }
    
    /**
     * 新增数据
     *
     * @param discussPost 实例对象
     * @return 影响行数
     */
    public int insert(DiscussPost discussPost) {
        return this.discussPostDao.insert(discussPost);
    }

    /**
     * 修改数据
     *
     * @param discussPost 实例对象
     * @return 影响行数
     */
    public int update(DiscussPost discussPost) {
        return this.discussPostDao.update(discussPost);
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    public boolean deleteById(Integer id) {
        return this.discussPostDao.deleteById(id) > 0;
    }
}