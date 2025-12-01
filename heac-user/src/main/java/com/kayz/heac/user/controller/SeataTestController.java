package com.kayz.heac.user.controller;

//import com.kayz.heac.user.mapper.MamMapper;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seata")
public class SeataTestController {
//
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
//    @Autowired
//    private MamMapper mamMapper;
//
//    @GetMapping("/test")
//    @GlobalTransactional(timeoutMills = 30000, rollbackFor = Exception.class, name = "heac-user-group")
//    public String test() throws SQLException, InterruptedException {
//        mamMapper.updateById(new Mam(1, "test2222"));
//        Thread.sleep(50000);
//        if(true){
//            throw new RuntimeException("test");
//        }
//        return "success";
//    }
}
