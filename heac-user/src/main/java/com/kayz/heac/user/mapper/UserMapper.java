package com.kayz.heac.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kayz.heac.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    String findUserIdByAccount(String account);
    User findUserByAccount(@NotBlank(message = "账号不能为空") String account);
}
