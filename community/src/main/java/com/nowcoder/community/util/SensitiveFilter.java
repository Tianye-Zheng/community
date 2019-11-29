package com.nowcoder.community.util;

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

@Component
public class SensitiveFilter {

    public static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    public static final String REPLACEMENT = "***";

    // 定义前缀树
    private class TrieNode {

        // 关键词结束标识
        private boolean isKeywordEnd = false;

        // 子节点(key 是下级字符，value 是下级节点)
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }

    // 将一个敏感词添加到前缀树当中
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            if(subNode == null) {
                // 初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            // 指向子节点，进入下一轮循环
            tempNode = subNode;

            // 设置结束标识
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    // 根据敏感词，初始化前缀树
    private TrieNode rootNode = new TrieNode();

    @PostConstruct // 在实例化本bean的构造器之后这个方法就会被调用
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().
                        getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));
                ) {

            String keyword;
            while ((keyword = reader.readLine()) != null) {

                // 添加到前缀树
                this.addKeyword(keyword);
            }

        } catch (IOException e) {
            logger.error("加载敏感词文件失败: " + e.getMessage());
        }
    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {

        if (StringUtils.isBlank(text)) {
            return null;
        }

        // 指针1 指向树
        TrieNode tempNode = rootNode;
        // 指针2 遍历字符串
        int begin = 0;
        // 指针3 辅助定位
        int position = 0;

        // 结果
        StringBuilder result = new StringBuilder();

        while (position < text.length()) {

            char c = text.charAt(position);
            // 跳过符号
            if (isSymbol(c)) {
                if (tempNode == rootNode) {
                    result.append(c);
                    begin++;
                }
                position++;
                continue;
            }

            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // 以begin开头的字符串不是敏感词
                result.append(text.charAt(begin));
                // 进入下一个位置
                position = ++begin;
                // 重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // 发现敏感词，将begin~position部分替换
                result.append(REPLACEMENT);
                // 进入下一个位置
                begin = ++position;
                // 重新指向根节点
                tempNode = rootNode;
            } else {
                // 检查途中，继续检查下一个字符
                position++;
            }
        }

        // 将最后一批字符计入结果
        result.append(text.substring(begin));

        return result.toString();
    }

    // 判断是否为符号
    private boolean isSymbol(Character c) {
        // 0x2E80 ~ 0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c)
                && (c < 0x2E80 || c > 0x9FFF);
    }
}
