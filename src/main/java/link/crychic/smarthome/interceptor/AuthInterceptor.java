package link.crychic.smarthome.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 认证拦截器
 * 用于验证请求中的Authorization头
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // 获取Authorization头
        String token = request.getHeader("Authorization");
        String userId = request.getHeader("X-User-Id");

        //todo temp skip auth
        if (Objects.equals(token, "1145141919810")) return true;

        // 如果没有userId或token，返回未授权错误
        if (userId == null || token == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":401,\"info\":\"Unauthorized\"}");
            return false;
        }

        // 验证token
        String authKey = userId + ":auth";
        String storedToken = redisTemplate.opsForValue().get(authKey);

        if (storedToken == null || !storedToken.equals(token)) {
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":401,\"info\":\"Unauthorized\"}");
            return false;
        }

        // 刷新token的TTL
        redisTemplate.expire(authKey, 5, TimeUnit.MINUTES);

        return true;
    }
}