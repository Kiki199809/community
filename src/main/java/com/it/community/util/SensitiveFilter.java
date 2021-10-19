package com.it.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: KiKi
 * @date: 2021/9/19 - 23:14
 * @project_name：community
 * @description: 敏感词过滤器
 */

@Component
public class SensitiveFilter {

    public static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static final String REPLACEMENT = "***";

    // 根结点
    private TrieNode root = new TrieNode();

    // 初始化方法，当容器实例化这个bean，调用构造方法后调用
    @PostConstruct
    public void init() {
        try (
                // 使用类加载器获取资源（编译后会存在class目录下）
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                // 字节流转化为字符流，字符流转化为缓冲流
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 添加到前缀树
                this.addKeyword(keyword);
            }

        } catch (IOException e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }

    }

    // 将一个敏感词添加到前缀树当中
    private void addKeyword(String keyword) {
        TrieNode tempNode = root;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            // 是否已经有该字符
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null) {
                // 无该字符，初始化此结点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            // temp指向子结点，进行下一轮循环
            tempNode = subNode;
            // 给最后一个字符设置结束标识
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * Description: 过滤敏感词
     * @param text: 待过滤的文本
     * @return java.lang.String: 已过滤的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        // 指针1，指向树
        TrieNode tempNode = root;
        // 指针2，text文本首指针
        int begin = 0;
        // 指针3，text文本尾指针
        int position = 0;
        // 过滤后的结果
        StringBuilder sb = new StringBuilder();

        while(begin < text.length()){
            if(position < text.length()) {
                Character c = text.charAt(position);

                // 跳过符号(不是普通字符(abc123...)且不是东亚字符)
                if (!CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF)) {
                    // 若此时处于根结点，首指针++
                    if (tempNode == root) {
                        begin++;
                        sb.append(c);
                    }
                    // 无论符号在开头还是中间，尾指针++
                    position++;
                    // 进行下一次循环
                    continue;
                }

                // 检查下级节点
                tempNode = tempNode.getSubNode(c);
                if (tempNode == null) {
                    // 以begin开头的字符串不是敏感词
                    sb.append(text.charAt(begin));
                    // 进入下一个位置
                    begin++;
                    position = begin;
                    // 重新指向根节点
                    tempNode = root;
                }
                // 发现敏感词，将begin~position字符串替换
                else if (tempNode.isKeywordEnd()) {
                    sb.append(REPLACEMENT);
                    // 进入下一个位置
                    position++;
                    begin = position;
                    // 重新指向根节点
                    tempNode = root;
                }
                // 检查到敏感词部分，继续检查下一个字符
                else {
                    position++;
                }
            }
            // position遍历越界仍未匹配到敏感词
            else{
                sb.append(text.charAt(begin));
                // 进入下一个位置
                begin++;
                position = begin;
                // 重新指向根节点
                tempNode = root;
            }
        }
        return sb.toString();

    }

    // 前缀树结点
    private class TrieNode {

        // 关键词结束标识
        private boolean isKeywordEnd = false;

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 子结点的值和数据结构组成的Map（当前结点的value存在父结点的map）
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        // 添加子结点的方法
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子结点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}
