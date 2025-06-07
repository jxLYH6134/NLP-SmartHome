package link.crychic.smarthome.service;

import link.crychic.smarthome.constant.ApiConstants;
import link.crychic.smarthome.entity.User;
import link.crychic.smarthome.model.ApiRequest;
import link.crychic.smarthome.model.ApiResponse;
import link.crychic.smarthome.repository.UserRepository;
import link.crychic.smarthome.util.SignUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ApiService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    public ApiResponse sendVerifyCode(ApiRequest request) {
        String codeKey = ApiConstants.VERIFY_CODE_PREFIX + request.getUserId() + ":" + request.getType();

        // 检查是否在10分钟内发送过
        if (Boolean.TRUE.equals(redisTemplate.hasKey(codeKey))) {
            return ApiResponse.error(4, "10分钟内多次发送同类验证码");
        }

        // 对于注册(type=0)，检查用户是否存在
        if ("0".equals(request.getType())) {
            if (userRepository.existsById(request.getUserId())) {
                return ApiResponse.error(5, "该用户已存在");
            }
        }
        // 对于密码重置(type=1)，检查用户是否不存在
        else if ("1".equals(request.getType())) {
            if (!userRepository.existsById(request.getUserId())) {
                return ApiResponse.error(6, "该用户不存在");
            }
        }

        try {
            // 生成随机4位验证码
            String verifyCode = String.format("%04d", new Random().nextInt(10000));

            // 发送短信
            String notificationUrl = String.format("%s/%s/%s",
                    ApiConstants.NOTIFICATION_API_ENDPOINT,
                    request.getUserId(),
                    "您的验证码是: " + verifyCode);

            ResponseEntity<String> response = restTemplate.getForEntity(notificationUrl, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                return ApiResponse.error(3, "发送失败，请检查短信平台");
            }

            // 在Redis存储带TTL的验证码
            redisTemplate.opsForValue().set(codeKey, verifyCode, ApiConstants.VERIFY_CODE_TTL, TimeUnit.SECONDS);

            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error(100, "服务器错误");
        }
    }

    public ApiResponse registerUser(ApiRequest request) {
        // 检查用户是否存在
        if (userRepository.existsById(request.getUserId())) {
            return ApiResponse.error(3, "该用户已存在");
        }

        // 验证vCode
        String codeKey = ApiConstants.VERIFY_CODE_PREFIX + request.getUserId() + ":0";  // type=0 表示注册
        String storedVCode = redisTemplate.opsForValue().get(codeKey);
        if (storedVCode == null || !storedVCode.equals(request.getVerifyCode())) {
            return ApiResponse.error(4, "验证码无效");
        }

        try {
            User user = new User();
            user.setUserId(request.getUserId());
            user.setPassword(SignUtil.encryptPassword(request.getPassword(), request.getUserId()));
            userRepository.save(user);
            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error(100, "服务器错误");
        }
    }

    public ApiResponse loginUser(ApiRequest request) {
        try {
            // 检查用户是否存在
            User user = userRepository.findById(request.getUserId()).orElse(null);
            if (user == null) {
                return ApiResponse.error(3, "用户不存在");
            }

            // 验证密码
            String encryptedPassword = SignUtil.encryptPassword(request.getPassword(), request.getUserId());
            if (!user.getPassword().equals(encryptedPassword)) {
                return ApiResponse.error(4, "密码错误");
            }

            // 生成Authorization令牌
            String token = UUID.randomUUID().toString();
            String authKey = request.getUserId() + ":auth";

            // 存储令牌到Redis，设置5分钟过期
            redisTemplate.opsForValue().set(authKey, token, 5, TimeUnit.MINUTES);

            // 返回成功响应，包含Authorization令牌
            return ApiResponse.success(token);

        } catch (Exception e) {
            return ApiResponse.error(100, "服务器错误");
        }
    }

    public ApiResponse testAuth(String userId, String token) {
        try {
            String authKey = userId + ":auth";
            String storedToken = redisTemplate.opsForValue().get(authKey);

            if (storedToken == null || !storedToken.equals(token)) {
                return ApiResponse.error(401, "Unauthorized");
            }

            // 刷新token的TTL
            redisTemplate.expire(authKey, 5, TimeUnit.MINUTES);

            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error(100, "服务器错误");
        }
    }

    public ApiResponse changePassword(ApiRequest request) {
        try {
            // 检查用户是否存在
            User user = userRepository.findById(request.getUserId()).orElse(null);
            if (user == null) {
                return ApiResponse.error(3, "用户不存在");
            }

            // 验证旧密码
            String encryptedOldPassword = SignUtil.encryptPassword(request.getPassword(), request.getUserId());
            if (!user.getPassword().equals(encryptedOldPassword)) {
                return ApiResponse.error(4, "原密码错误");
            }

            // 更新密码
            String encryptedNewPassword = SignUtil.encryptPassword(request.getPasswordNew(), request.getUserId());
            user.setPassword(encryptedNewPassword);
            userRepository.save(user);

            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error(100, "服务器错误");
        }
    }

    public ApiResponse forgetPassword(ApiRequest request) {
        try {
            // 检查用户是否存在
            User user = userRepository.findById(request.getUserId()).orElse(null);
            if (user == null) {
                return ApiResponse.error(3, "用户不存在");
            }

            // 验证验证码
            String codeKey = ApiConstants.VERIFY_CODE_PREFIX + request.getUserId() + ":1";  // type=1 表示忘记密码
            String storedVCode = redisTemplate.opsForValue().get(codeKey);
            if (storedVCode == null || !storedVCode.equals(request.getVerifyCode())) {
                return ApiResponse.error(4, "验证码无效");
            }

            // 更新密码
            String encryptedNewPassword = SignUtil.encryptPassword(request.getPasswordNew(), request.getUserId());
            user.setPassword(encryptedNewPassword);
            userRepository.save(user);

            // 删除已使用的验证码
            redisTemplate.delete(codeKey);

            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error(100, "服务器错误");
        }
    }
}
