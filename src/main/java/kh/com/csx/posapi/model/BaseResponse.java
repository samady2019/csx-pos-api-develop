package kh.com.csx.posapi.model;

import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class BaseResponse {
    protected String message = "";
    protected long totalRecords = 0;
    protected Object data;

    public BaseResponse(String message) {
        this.message = message;
    }

    public BaseResponse(Object data) {
        this.data = data;
    }

    public BaseResponse(Object data, String message) {
        this.data = data;
        this.message = message;
    }
}

