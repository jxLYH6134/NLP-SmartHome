package link.crychic.smarthome.controller;

import com.fasterxml.jackson.databind.JsonNode;
import link.crychic.smarthome.model.ApiRequest;
import link.crychic.smarthome.model.ApiResponse;
import link.crychic.smarthome.service.ApiService;
import link.crychic.smarthome.service.OllamaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ApiController {
    @Autowired
    private ApiService apiService;

    @Autowired
    private OllamaService ollamaService;

    @PostMapping("/sendVerifyCode")
    public ApiResponse sendVerifyCode(
            @Validated(ApiRequest.SendCode.class) @RequestBody ApiRequest request) {
        String userId = request.getUserId();
        String type = request.getType();

        if (!userId.matches("\\d{11}")) {
            return ApiResponse.error(2, "参数错误: 无效的手机号");
        }
        if (type == null || !type.matches("^([01])$")) {
            return ApiResponse.error(2, "参数错误: 非法的type取值");
        }
        return apiService.sendVerifyCode(request);
    }

    @PostMapping("/userRegister")
    public ApiResponse userRegister(
            @Validated(ApiRequest.Register.class) @RequestBody ApiRequest request) {
        String userId = request.getUserId();
        String password = request.getPassword();
        if (!userId.matches("\\d{11}")) {
            return ApiResponse.error(2, "参数错误: 无效的手机号");
        }
        if (!password.matches("^[a-zA-Z0-9]{8,12}$")) {
            return ApiResponse.error(2, "参数错误: 密码需8-12位且只能包括字母数字");
        }
        return apiService.registerUser(request);
    }

    @PostMapping("/userLogin")
    public ApiResponse userLogin(
            @Validated(ApiRequest.Login.class) @RequestBody ApiRequest request) {

        return apiService.loginUser(request);
    }

    @PostMapping("/changePassword")
    public ApiResponse changePassword(
            @Validated(ApiRequest.ChangePassword.class) @RequestBody ApiRequest request) {
        if (!request.getPasswordNew().matches("^[a-zA-Z0-9]{8,12}$")) {
            return ApiResponse.error(2, "参数错误: 密码需8-12位且只能包括字母数字");
        }
        return apiService.changePassword(request);
    }

    @PostMapping("/forgetPassword")
    public ApiResponse forgetPassword(
            @Validated(ApiRequest.ForgetPassword.class) @RequestBody ApiRequest request) {
        if (!request.getPasswordNew().matches("^[a-zA-Z0-9]{8,12}$")) {
            return ApiResponse.error(2, "参数错误: 密码需8-12位且只能包括字母数字");
        }
        return apiService.forgetPassword(request);
    }

    @PostMapping("/testAuth")
    public ApiResponse testAuth() {
        return ApiResponse.success();
    }

    @PostMapping("/ollama")
    public JsonNode ollama(@RequestBody String request) {
        return ollamaService.generateResponse(request);
    }
}
