package link.crychic.smarthome.util;

import link.crychic.smarthome.constant.ApiConstants;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

public class SignUtil {

    public static String generateSign(String paramStr, String timestamp) {
        String signStr = paramStr + timestamp + ApiConstants.SIGN_SALT;
        return DigestUtils.md5DigestAsHex(signStr.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean isTimestampValid(String timestamp) {
        try {
            long requestTime = Long.parseLong(timestamp);
            long currentTime = System.currentTimeMillis();
            return Math.abs(currentTime - requestTime) <= ApiConstants.TIMESTAMP_VALID_PERIOD;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String encryptPassword(String password, String salt) {
        return DigestUtils.md5DigestAsHex((password + salt).getBytes(StandardCharsets.UTF_8));
    }
}
