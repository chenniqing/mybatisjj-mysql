package cn.javaex.mybatisjj.util;

/**
 * Mybatisjj专用字符串工具类
 * 
 * @author 陈霓清
 */
public class SqlStringUtils {
	
	private static final char SEPARATOR = '_';
	
    /**
     * 首字母转大写
     *
     * <pre>
     * StringUtils.capitalize(null)  = null
     * StringUtils.capitalize("")    = ""
     * StringUtils.capitalize("cat") = "Cat"
     * StringUtils.capitalize("cAt") = "CAt"
     * </pre>
     */
	public static String capitalize(String str) {
		int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        return new StringBuffer(strLen)
            .append(Character.toTitleCase(str.charAt(0)))
            .append(str.substring(1))
            .toString();
	}
	
	/**
	 * 所有字母转小写，单词之间用下划线分隔
	 * 
	 * <pre>
     * StringUtils.toUnderlineName(myTest)  = my_test
     * </pre>
	 * 
	 * @param text
	 * @return
	 */
	public static String toUnderlineName(String text) {
		if (isEmpty(text)) {
			return text;
		}
		
		StringBuilder sb = new StringBuilder();
		boolean upperCase = false;
		for (int i=0; i<text.length(); i++) {
			char c = text.charAt(i);
			
			boolean nextUpperCase = true;
			
			if (i<(text.length()-1)) {
				nextUpperCase = Character.isUpperCase(text.charAt(i + 1));
			}
			
			if ((i>=0) && Character.isUpperCase(c)) {
				if (!upperCase || !nextUpperCase) {
					if (i>0) {
						sb.append(SEPARATOR);
					}
				}
				upperCase = true;
			} else {
				upperCase = false;
			}
			
			sb.append(Character.toLowerCase(c));
		}
		
		return sb.toString();
	}
	
    // Empty checks
    //-----------------------------------------------------------------------
    /**
     * <p>Checks if a String is empty ("") or null.</p>
     *
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * </pre>
     *
     * <p>NOTE: This method changed in Lang version 2.0.
     * It no longer trims the String.
     * That functionality is available in isBlank().</p>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if the String is empty or null
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
	
    /**
     * <p>Checks if a String is not empty ("") and not null.</p>
     *
     * <pre>
     * StringUtils.isNotEmpty(null)      = false
     * StringUtils.isNotEmpty("")        = false
     * StringUtils.isNotEmpty(" ")       = true
     * StringUtils.isNotEmpty("bob")     = true
     * StringUtils.isNotEmpty("  bob  ") = true
     * </pre>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if the String is not empty and not null
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
}
