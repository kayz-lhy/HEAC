package com.kayz.heac.common.context;

public class UserContext {
    private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> TOKEN = new ThreadLocal<>();

    public static String getToken() {
        return TOKEN.get();
    }

    public static void setToken(String token) {
        TOKEN.set(token);
    }

    public static String getUserId() {
        return USER_ID.get();
    }

    public static void setUserId(String userId) {
        USER_ID.set(userId);
    }

    public static void clear() {
        USER_ID.remove();
    }

    private UserContext() {
        throw new UnsupportedOperationException("Cannot instantiate UserContext");
    }
}
