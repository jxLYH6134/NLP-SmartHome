package link.crychic.smarthome.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiRequest {
    private String sign;
    private String timestamp;
    private String type;

    @NotBlank(message = "手机号不能为空", groups = {SendCode.class, Register.class, Login.class, ChangePassword.class, ForgetPassword.class})
    private String userId;//永远需要校验

    @NotBlank(message = "密码不能为空", groups = {Register.class, Login.class, ChangePassword.class})
    private String password;//注册 登录 修改密码需要校验

    @NotBlank(message = "新密码不能为空", groups = {ChangePassword.class, ForgetPassword.class})
    private String passwordNew;//修改和忘记密码需要校验

    @NotBlank(message = "验证码不能为空", groups = {Register.class, ForgetPassword.class})
    private String verifyCode;//注册和忘记密码需要校验
    // 怎么会有这么唐的校验机制? yue, 不要乱转换我的拼写啊

    // 验证码发送接口的验证组
    public interface SendCode {
    }

    // 注册接口的验证组
    public interface Register {
    }

    // 登录接口的验证组
    public interface Login {
    }

    // 修改密码接口的验证组
    public interface ChangePassword {
    }

    // 忘记密码接口的验证组
    public interface ForgetPassword {
    }
}
