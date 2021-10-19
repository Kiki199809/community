package com.it.community.dao.elasticsearch;

import com.it.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author: KiKi
 * @date: 2021/10/7 - 14:40
 * @project_name：community
 * @description:
 */

@Repository
// 泛型参数：待处理的类和类中的主键类型
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {

}
