package com.kayz.heac.common.consts;


public class RedisPrefix {

    public static final String LOGIN_TIME_CACHE_KEY = "sys:user:login_time_map:";
    public static final String LOGIN_IP_CACHE_KEY = "sys:user:login_ip_map:";
    public static final String TOKEN_CACHE_PREFIX = "auth:token:";

    private RedisPrefix() {
        throw new UnsupportedOperationException("RedisPrefix is a constant class and cannot be instantiated");
    }

}
