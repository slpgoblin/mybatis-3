/**
 *    Copyright 2009-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.parsing;

/**
 * @author Clinton Begin
 */
public class GenericTokenParser {

  /**
   * 开始的Token字符串
   */
  private final String openToken;

  /**
   * 结束的Token字符串
   */
  private final String closeToken;

  private final TokenHandler handler;

  public GenericTokenParser(String openToken, String closeToken, TokenHandler handler) {
    this.openToken = openToken;
    this.closeToken = closeToken;
    this.handler = handler;
  }

  public String parse(String text) {
    if (text == null || text.isEmpty()) {
      return "";
    }
    // search open token
    // 寻找起始的 openToken 位置
    int start = text.indexOf(openToken);
    // 如果没有直接返回
    if (start == -1) {
      return text;
    }
    char[] src = text.toCharArray();
    // 查找的起始位置
    int offset = 0;
    // 处理结果
    final StringBuilder builder = new StringBuilder();
    // 匹配到 openToken 和 closeToken 之间的表达式
    StringBuilder expression = null;
    while (start > -1) {

      if (start > 0 && src[start - 1] == '\\') {
        // this open token is escaped. remove the backslash and continue.
        // 处理转义，移除反斜杠
        // 添加转义后的src 和opentoken到builder中
        builder.append(src, offset, start - offset - 1).append(openToken);
        offset = start + openToken.length();
      } else {
        // found open token. let's search close token.
        if (expression == null) {
          expression = new StringBuilder();
        } else {
          expression.setLength(0);
        }
        // builder.append：需要加长字符串，str：追加字符串，offset：从哪一位开始加长，len：加长位数
        // 将 offset 与 openToken 之间的内容添加到处理结果 builder 中
        builder.append(src, offset, start - offset);
        // 将 offset 改为 openToken 之后
        offset = start + openToken.length();
        // 获取closeToken 的位置
        int end = text.indexOf(closeToken, offset);
        while (end > -1) {
          // 处理转义，移除反斜杠
          if (end > offset && src[end - 1] == '\\') {
            // this close token is escaped. remove the backslash and continue.
            // 添加 [offset, end - offset - 1] 和 endToken 的内容，添加到 builder 中
            expression.append(src, offset, end - offset - 1).append(closeToken);
            // 修改 offset
            offset = end + closeToken.length();
            // 继续寻找结束的 closeToken 的位置
            end = text.indexOf(closeToken, offset);
          } else {
            // 添加 [offset, end - offset] 的内容，添加到 builder 中
            expression.append(src, offset, end - offset);
            break;
          }
        }
        if (end == -1) {
          // close token was not found.
          // closeToken 未找到，直接拼接
          builder.append(src, start, src.length - start);
          offset = src.length;
        } else {
          // 将 expression 提交给 handler 处理 ，并将处理结果添加到 builder 中
          builder.append(handler.handleToken(expression.toString()));
          // 重新定位 offset 改为当前的 closeToken 之后
          offset = end + closeToken.length();
        }
      }
      // 获取下一个 openToken 的位置
      start = text.indexOf(openToken, offset);
    }
    // 拼接剩余处理结果
    if (offset < src.length) {
      builder.append(src, offset, src.length - offset);
    }
    return builder.toString();
  }

}
