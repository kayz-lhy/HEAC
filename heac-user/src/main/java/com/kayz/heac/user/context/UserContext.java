package com.kayz.heac.user.context;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("userContext")
@Scope("singleton")
public class UserContext {
    private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();

    public String getUserId() {
        return USER_ID.get();
    }

    public void setUserId(String userId) {
        USER_ID.set(userId);
    }

    public void clear() {
        USER_ID.remove();
    }
}
