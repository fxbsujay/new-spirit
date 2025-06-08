package cn.spirit.go.common.util;

import java.util.regex.Pattern;

/**
 * <p>Description: Regex</p>
 * <p>正则表达式</p>
 *
 * @author sujay
 * @version 18:36 2022/5/5
 * @since JDK1.8 <br/>
 */
public class RegexUtils {

    /**
     * 身份证号
     */
    public static final String ID_NUMBER = "\\d{17}[\\d|x]|\\d{15}";

    /**
     * 汉字
     */
    public static final String CHINESE_CHARACTERS = "[\\u4e00-\\u9fa5]";

    /**
     * 邮箱地址
     */
    public static final String EMAIL = "\\w[-\\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\\.)+[A-Za-z]{2,14}";

    /**
     * 网址
     */
    public static final String WEBSITE = "^((https|http|ftp|rtsp|mms)?:\\/\\/)[^\\s]+";

    /**
     * 中国内地手机号
     */
    public static final String PHONE_NUMBER = "0?(13|14|15|17|18)[0-9]{9}";

    /**
     * 账户号 英文字母、数字
     */
    public static final String USERNAME = "[a-zA-Z0-9]{2,20}";

    /**
     * 账户密码 英文字符、数字、!$^.*_%
     */
    public static final String PASSWORD = "^[a-zA-Z0-9@!$^.*_%]{6,30}$";

    public static boolean matches(String line, String pattern) {
        if (line == null || line.trim().isBlank()) {
            return false;
        }
        return Pattern.matches(pattern, line);
    }

    public static void main(String[] args) {
        System.out.println(matches("234223@", PASSWORD));
    }

}
