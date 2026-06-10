package cn.javaex.mybatisjj.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.javaex.mybatisjj.config.customize.DynamicTableNameContextHolder;

/**
 * 动态表名工具类
 * 
 * @author 陈霓清
 */
public class DynamicTableNameUtils {
	
	/**
	 * 提取SQL中常见位置的表名，覆盖SELECT、JOIN、INSERT、UPDATE、DELETE等场景
	 */
	private static final Pattern TABLE_NAME_PATTERN = Pattern.compile(
			"(?i)\\b(from|join|update|into)(\\s+)((?:`[^`]+`|\"[^\"]+\"|\\[[^\\]]+\\]|[a-zA-Z_][a-zA-Z0-9_$]*)(?:\\s*\\.\\s*(?:`[^`]+`|\"[^\"]+\"|\\[[^\\]]+\\]|[a-zA-Z_][a-zA-Z0-9_$]*))?)");
	
	private DynamicTableNameUtils() {
		
	}
	
	/**
	 * 根据当前上下文替换SQL中的逻辑表名
	 * @param sql
	 * @return
	 */
	public static String replaceTableName(String sql) {
		if (SqlStringUtils.isEmpty(sql) || DynamicTableNameContextHolder.hasTableNameHandler() == false) {
			return sql;
		}
		
		Matcher matcher = TABLE_NAME_PATTERN.matcher(sql);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			// 跳过字符串字面量和注释里的 from/join/update/into，避免误替换业务文本
			if (isSqlCodePosition(sql, matcher.start()) == false) {
				matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()));
				continue;
			}
			
			String tableExpression = matcher.group(3);
			String tableName = extractSimpleTableName(tableExpression);
			String fullTableName = normalizeTableExpression(tableExpression);
			String dynamicTableName = DynamicTableNameContextHolder.resolveTableName(sql, tableName);
			if (SqlStringUtils.isEmpty(dynamicTableName)) {
				dynamicTableName = DynamicTableNameContextHolder.resolveTableName(sql, fullTableName);
			}
			if (SqlStringUtils.isNotEmpty(dynamicTableName) && tableName.equals(dynamicTableName) == false) {
				String replacement = matcher.group(1) + matcher.group(2) + buildReplacementTableName(tableExpression, dynamicTableName);
				matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
			} else {
				matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()));
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
	
	/**
	 * 判断当前位置是否处于 SQL 代码区域，排除单引号字符串、单行注释和块注释
	 * @param sql SQL
	 * @param position 当前位置
	 * @return true表示可安全处理
	 */
	private static boolean isSqlCodePosition(String sql, int position) {
		boolean inSingleQuote = false;
		boolean inLineComment = false;
		boolean inBlockComment = false;
		for (int i = 0; i < position; i++) {
			char c = sql.charAt(i);
			char next = i + 1 < position ? sql.charAt(i + 1) : '\0';
			
			if (inLineComment) {
				if (c == '\n' || c == '\r') {
					inLineComment = false;
				}
				continue;
			}
			if (inBlockComment) {
				if (c == '*' && next == '/') {
					inBlockComment = false;
					i++;
				}
				continue;
			}
			if (inSingleQuote) {
				if (c == '\'' && next == '\'') {
					i++;
				} else if (c == '\'') {
					inSingleQuote = false;
				}
				continue;
			}
			
			if (c == '\'') {
				inSingleQuote = true;
			} else if (c == '-' && next == '-') {
				inLineComment = true;
				i++;
			} else if (c == '/' && next == '*') {
				inBlockComment = true;
				i++;
			}
		}
		return inSingleQuote == false && inLineComment == false && inBlockComment == false;
	}
	
	/**
	 * 提取 schema.table 中最后一段作为逻辑表名，保留用户按简单表名映射的习惯
	 * @param tableExpression 表达式
	 * @return 简单表名
	 */
	private static String extractSimpleTableName(String tableExpression) {
		String normalized = normalizeTableExpression(tableExpression);
		int dotIndex = normalized.lastIndexOf('.');
		return dotIndex >= 0 ? normalized.substring(dotIndex + 1) : normalized;
	}
	
	/**
	 * 去掉常见引用符和空白，得到用于匹配 handler 的表名
	 * @param tableExpression 表达式
	 * @return 规范化表名
	 */
	private static String normalizeTableExpression(String tableExpression) {
		return tableExpression.replace("`", "")
				.replace("\"", "")
				.replace("[", "")
				.replace("]", "")
				.replaceAll("\\s+", "");
	}
	
	/**
	 * schema.table 只替换最后一段表名；handler 返回带 schema 的表名时按 handler 结果整体替换
	 * @param originalTableExpression 原始表表达式
	 * @param dynamicTableName 动态表名
	 * @return 替换后的表表达式
	 */
	private static String buildReplacementTableName(String originalTableExpression, String dynamicTableName) {
		if (dynamicTableName.indexOf('.') >= 0) {
			return dynamicTableName;
		}
		int dotIndex = originalTableExpression.lastIndexOf('.');
		if (dotIndex < 0) {
			return dynamicTableName;
		}
		return originalTableExpression.substring(0, dotIndex + 1) + dynamicTableName;
	}
	
}
