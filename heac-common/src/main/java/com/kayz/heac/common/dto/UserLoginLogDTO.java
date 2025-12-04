package com.kayz.heac.common.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Builder
public class UserLoginLogDTO implements Serializable {
    private String userId;
    private String account;
    private String ip;
    private LocalDateTime loginTime;
}
