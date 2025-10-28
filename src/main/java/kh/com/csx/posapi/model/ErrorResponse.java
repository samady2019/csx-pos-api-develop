package kh.com.csx.posapi.model;

import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class ErrorResponse extends BaseResponse {
    public ErrorResponse(String message) {
        super(message);
    }
}
