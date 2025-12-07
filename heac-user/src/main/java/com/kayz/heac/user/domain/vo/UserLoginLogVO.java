package com.kayz.heac.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLoginLogVO {
    @Field("user_id")
    private String userId;
    @Field("account")
    private String account;
    @Field("login_time")
    private String loginTime;
    @Field("ip")
    private String ip;
    @Field("status")
    private int status;
}
