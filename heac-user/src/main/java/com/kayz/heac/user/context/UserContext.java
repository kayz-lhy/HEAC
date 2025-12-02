package com.kayz.heac.user.context;

public class UserContext {
    private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();

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
