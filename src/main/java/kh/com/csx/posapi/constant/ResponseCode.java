package kh.com.csx.posapi.constant;

public class ResponseCode {
    public static final String OK                            = "0000";
    public static final String INVALID_EMAIL                 = "0001";
    public static final String DUPLICATE_ACCOUNT             = "0002";
    public static final String EMAIL_ALREADY_USED            = "0003";
    public static final String PHONE_ALREADY_USED            = "0004";
    public static final String INVALID_INVESTOR_ID           = "0007";
    public static final String INVALID_INVESTOR_NAME         = "0008";
    public static final String INVALID_DATE_OF_BIRTH         = "0009";
    public static final String INVALID_EMAIL_PHONE           = "0010";
    public static final String OTP_SENT_ERROR                = "0011";
    public static final String INVALID_ACCOUNT_INFO          = "0012";
    public static final String OTP_EXPIRED                   = "0013";
    public static final String USERNAME_ALREADY_EXIST        = "0014";
    public static final String INVALID_PASSWORD              = "0015";
    public static final String DEPOSIT_NOT_ACTIVATED         = "0016";
    public static final String DEPOSIT_ALREADY_ACTIVATED     = "0017";
    public static final String DEPOSIT_WAIT_CONFIRM          = "0018";
    public static final String NOT_IN_WAITING                = "0019";
    public static final String TOO_MANY_PASSWORD_RESET_PHONE = "6429";
    public static final String TOO_MANY_PASSWORD_RESET_EMAIL = "7429";
    public static final String TOO_MANY_LOGIN                = "8429";
    public static final String BAD_REQUEST                   = "8400";
    public static final String INVALID_ACCESS_TOKEN          = "8401";
    public static final String UNAUTHORIZED                  = "8401";
    public static final String FORBIDDEN_OR_VALIDATION_ERROR = "8403";
    public static final String ITEM_NOT_FOUND                = "8404";
    public static final String SERVER_ERROR                  = "8500";

    // Role and Permission
    public static final String REQUEST_ERROR                 = "0400";
    public static final String INVALID_PERMISSION_NAME       = "1001";
    public static final String PERMISSION_ALREADY_EXIST      = "1002";
    public static final String PERMISSION_NOT_FOUND          = "1003";
    public static final String INVALID_ROLE_NAME             = "1001";
    public static final String ROLE_ALREADY_EXIST            = "1004";
    public static final String ROLE_NOT_FOUND                = "1005";
    public static final String CONFLICT                      = "4090";
    public static final String NOT_FOUND                     = "4040";
}
