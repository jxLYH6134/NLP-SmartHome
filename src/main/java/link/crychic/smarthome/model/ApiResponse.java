package link.crychic.smarthome.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse {
    private int code;
    private String info;

    public static ApiResponse success() {
        ApiResponse response = new ApiResponse();
        response.setCode(0);
        response.setInfo("success");
        return response;
    }

    //自定义形参的success
    public static ApiResponse success(String info) {
        ApiResponse response = new ApiResponse();
        response.setCode(0);
        response.setInfo(info);
        return response;
    }

    public static ApiResponse error(int code, String info) {
        ApiResponse response = new ApiResponse();
        response.setCode(code);
        response.setInfo(info);
        return response;
    }
}
