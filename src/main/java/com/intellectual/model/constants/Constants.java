package com.intellectual.model.constants;

public class Constants {
    //存checkCode
    public static final String REDIS_KEY_CHECK_CODE = "patent:checkcode:";
    //存SmsCode
    public static final String REDIS_MOBILE_CHECK_CODE = "patent:smscode:";
    //存token
    public static final String REDIS_KEY_TOKEN = "patent:token:";

    public static final String REDIS_KEY_JWT = "jwt:";

    public static final String REDIS_KEY_JWT_TOKEN = "jwt:token:";

    //Redis过期时间
    public static final long REDIS_TIME_1MIN = 60L;
    public static final long REDIS_TIME_5MIN = 300L;
    public static final long REDIS_TIME_30MIN = 1800L;
    public static final long REDIS_TIME_1HOUR = 3600L;
    public static final long REDIS_TIME_1DAY = 86400L;
}
