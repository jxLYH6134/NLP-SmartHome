package link.crychic.smarthome.constant;

public class ApiConstants {
    public static final String NOTIFICATION_API_ENDPOINT = "https://api.day.app/zy2A86jkV35LuAscTyWXRU";
    public static final int VERIFY_CODE_TTL = 60;
    public static final String VERIFY_CODE_PREFIX = "vCode:";
    public static final String SIGN_SALT = "salt";  // 加“盐”字符串
    public static final long TIMESTAMP_VALID_PERIOD = 50 * 60 * 1000; // 时间戳有效期50分钟
}
