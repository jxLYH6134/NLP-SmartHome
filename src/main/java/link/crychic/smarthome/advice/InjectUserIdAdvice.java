package link.crychic.smarthome.advice;

import jakarta.servlet.http.HttpServletRequest;
import link.crychic.smarthome.annotation.InjectUserId;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

@Component
@ControllerAdvice
public class InjectUserIdAdvice extends RequestBodyAdviceAdapter {

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return targetType instanceof Class<?> && ((Class<?>) targetType).isAnnotationPresent(InjectUserId.class);
    }

    @Override
    public Object afterBodyRead(Object body, org.springframework.http.HttpInputMessage inputMessage,
                                MethodParameter parameter, Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes()).getRequest();
        String userId = request.getHeader("X-User-Id");
        if (userId != null && !userId.isBlank()) {
            try {
                Method setOwnerId = body.getClass().getMethod("setOwnerId", String.class);
                setOwnerId.invoke(body, userId);
            } catch (Exception ignored) {
            }
        }
        return body;
    }
}
