package link.crychic.smarthome.aspect;

import link.crychic.smarthome.model.ApiRequest;
import link.crychic.smarthome.model.ApiResponse;
import link.crychic.smarthome.util.SignUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SignValidationAspect {
    // 这切片怎么这么难用啊? 好吧比校验好用一点
    @Around("execution(* link.crychic.smarthome.controller.ApiController.*(..))")
    public Object validateSign(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof ApiRequest request) {
                if (request.getTimestamp() == null || request.getTimestamp().isEmpty()) {
                    return joinPoint.proceed();
                }

                // 验证时间戳
                if (!SignUtil.isTimestampValid(request.getTimestamp())) {
                    return ApiResponse.error(1, "请求已过期");
                }

                // 验证签名
                String paramStr = request.getUserId();
                String expectedSign = SignUtil.generateSign(paramStr, request.getTimestamp());
                if (!expectedSign.equals(request.getSign())) {
                    return ApiResponse.error(1, "签名验证失败");
                }
            }
        }
        return joinPoint.proceed();
    }
}
