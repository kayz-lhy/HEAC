package com.kayz.heac.user.domain.vo;

import lombok.*;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC, staticName = "of")
@NoArgsConstructor(access = AccessLevel.PUBLIC, staticName = "empty")
public class UserInfoVO {
    private String avatar;
    private String annonymousName;
}
