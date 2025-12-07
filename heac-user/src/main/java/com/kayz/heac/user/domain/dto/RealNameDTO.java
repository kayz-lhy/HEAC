package com.kayz.heac.user.domain.dto;

import lombok.*;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC, staticName = "of")
@NoArgsConstructor(access = AccessLevel.PUBLIC, staticName = "empty")
public class RealNameDTO {
    private String realName;
    private String idNumber;
}
