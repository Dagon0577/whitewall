package com.Dagon.whitewall.service;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class SensitiveService implements InitializingBean {

  private static final Logger logger = LoggerFactory.getLogger(SensitiveService.class);

  private class TrieNode {

    //是不是敏感词结尾
    private boolean end = false;

    //当前节点下所有的子节点
    private Map<Character, TrieNode> subNodes = new HashMap<>();

    public void addSubNode(Character key, TrieNode node) {
      subNodes.put(key, node);
    }

    TrieNode getSubNode(Character key) {
      return subNodes.get(key);
    }

    public void setKeyWordEnd(boolean end) {
      this.end = end;
    }

    public boolean isKeyWordEnd() {
      return end;
    }

  }

  //根节点（空）
  private TrieNode rootNode = new TrieNode();

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
          InputStream io = Thread.currentThread().getContextClassLoader()
              .getResourceAsStream("SensitiveWords.txt");
          InputStreamReader read = new InputStreamReader(io, "UTF-8");
          BufferedReader bufferedReader = new BufferedReader(read);
          String lineTxt;
          while ((lineTxt = bufferedReader.readLine()) != null) {
            lineTxt = lineTxt.trim();
            addWord(lineTxt);
          }
          read.close();
        } catch (Exception e) {
            logger.error("读取敏感词文件失败" + e.getMessage());
        }
    }

  //增加敏感词关键词
  private void addWord(String lineTxt) {
    TrieNode tempNode = rootNode;
    for (int i = 0; i < lineTxt.length(); i++) {
      Character c = lineTxt.charAt(i);

      //如果是迷惑符号则跳过
      if (isSymbol(c)) {
        continue;
      }

      TrieNode node = tempNode.getSubNode(c);

      if (node == null) {
        node = new TrieNode();
        tempNode.addSubNode(c, node);
      }
      tempNode = node;

      if (i == lineTxt.length() - 1) {
        tempNode.setKeyWordEnd(true);
      }
    }
    }

  //过滤
  public String filter(String text) {
    if (StringUtils.isBlank(text)) {
      return text;
    }

    StringBuilder sb = new StringBuilder();

    String replaceText = "***";
    TrieNode tempNode = rootNode;
    int begin = 0;//回滚数
    int position = 0;//当前比较的位置

    while (position < text.length()) {
      char c = text.charAt(position);

      //如果是迷惑符号
      if (isSymbol(c)) {
        //若为开头，则加入，否则跳过
        if (tempNode == rootNode) {
          sb.append(c);
          ++begin;
        }
        ++position;
        continue;
      }

      tempNode = tempNode.getSubNode(c);

      if (tempNode == null) {
        sb.append(text.charAt(begin));
        position = begin + 1;
        begin = position;
        tempNode = rootNode;
      } else if (tempNode.isKeyWordEnd()) {
        //发现敏感词
        sb.append(replaceText);
        position = position + 1;
        begin = position;
        tempNode = rootNode;
      } else {
        ++position;
      }
        }

        sb.append(text.substring(begin));
        return sb.toString();
  }

  //判断是否为迷惑符号
  private boolean isSymbol(char c) {
    int ascii = (int) c;
    //东亚文字 0x2E80-0x9FFF
    return !CharUtils.isAsciiAlphanumeric(c) && (ascii < 0x2E80 || ascii > 0x9FFF);
  }


  public static void main(String[] args) {
    SensitiveService s = new SensitiveService();
    s.addWord("色情");
    s.addWord("赌博");
    System.out.println(s.filter("hello 你好色●情"));
  }
}
