package com.kayz.heac.opinion.service.impl;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kayz.heac.common.client.UserClient;
import com.kayz.heac.common.context.UserContext;
import com.kayz.heac.opinion.domain.dto.OpinionPostDTO;
import com.kayz.heac.opinion.domain.dto.OpinionPostMsgDTO;
import com.kayz.heac.opinion.domain.vo.OpinionVO;
import com.kayz.heac.opinion.entity.Opinion;
import com.kayz.heac.opinion.mapper.OpinionMapper;
import com.kayz.heac.opinion.mq.OpinionProducer;
import com.kayz.heac.opinion.service.OpinionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpinionServiceImpl extends ServiceImpl<OpinionMapper, Opinion> implements OpinionService {

    private final OpinionProducer opinionProducer;
    private final UserClient userClient;

    @Override
    public OpinionVO postOpinion(OpinionPostDTO dto, String clientIp, String userAgent) {
        String userId = UserContext.getUserId();

        // 1. 生成 ID (关键：提前生成，用于返回给前端和 MQ 传递)
        String opinionId = UUID.fastUUID().toString(true);
        LocalDateTime now = LocalDateTime.now();

        // 2. 组装 MQ 消息
        OpinionPostMsgDTO msg = OpinionPostMsgDTO.builder()
                .id(opinionId)
                .eventId(dto.getEventId())
                .userId(userId)
                .standpoint(dto.getStandpoint())
                .tags(dto.getTags())
                .content(dto.getContent())
                .clientIp(clientIp)
                .userAgent(userAgent)
                .createTime(now)
                .build();

        // 3. 发送消息 (异步)
        opinionProducer.sendPostMessage(msg);

        // 4. 构造"假"VO返回给前端 (Fake Result)
        // 这样前端可以立即把评论展示出来，用户体验极佳
        OpinionVO vo = new OpinionVO();
        vo.setId(opinionId);
        vo.setStandpoint(dto.getStandpoint());
        vo.setTags(dto.getTags());
        vo.setContent(dto.getContent());
        vo.setCreateTime(now);
        vo.setLikeCount(0L);
        vo.setIsMine(true); // 肯定是自己的
        vo.setUserInfo(userClient.getUserInfo(userId).getData()); // 前端通常缓存了自己的头像，这里可以不返，或者简单设一下

        return vo;
    }
}
