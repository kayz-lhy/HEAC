package com.kayz.heac.opinion.mq;


import com.kayz.heac.opinion.domain.dto.OpinionPostMsgDTO;
import com.kayz.heac.opinion.entity.Opinion;
import com.kayz.heac.opinion.enums.OpinionStandpoint;
import com.kayz.heac.opinion.enums.OpinionStatus;
import com.kayz.heac.opinion.mapper.OpinionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "opinion-topic",          // 监听的主题
        consumerGroup = "heac-opinion-consumer-group", // 消费者组
        selectorExpression = "post"       // 过滤 Tag (只处理发表消息)
)
public class OpinionConsumer implements RocketMQListener<OpinionPostMsgDTO> {

    private final OpinionMapper opinionMapper;

    @Override
    public void onMessage(OpinionPostMsgDTO msg) {
        log.info("接收到观点发表消息: id={}, eventId={}", msg.getId(), msg.getEventId());

        try {
            // 1. 转换 Entity
            Opinion opinion = new Opinion();
            opinion.setId(msg.getId());
            opinion.setEventId(msg.getEventId());
            opinion.setUserId(msg.getUserId());
            opinion.setContent(msg.getContent());
            opinion.setTags(msg.getTags());

            // 枚举转换 (int -> Enum)
            opinion.setStandpoint(convertStandpoint(msg.getStandpoint()));

            // 审计字段
            opinion.setClientIp(msg.getClientIp());
            opinion.setUserAgent(msg.getUserAgent());
            opinion.setCreateTime(msg.getCreateTime()); // 使用消息生成的时间，保证准确性

            // 2. 默认值设置
            opinion.setLikeCount(0L);
            opinion.setRiskScore(0);
            opinion.setDeleted(0);
            opinion.setVersion(0);

            // 3. 敏感词过滤 (风控简易版)
            if (isSensitive(msg.getContent())) {
                opinion.setStatus(OpinionStatus.REJECTED);
                opinion.setRiskScore(100);
                log.warn("观点包含敏感词，自动驳回: {}", msg.getId());
            } else {
                opinion.setStatus(OpinionStatus.PUBLISHED); // 默认直接公开
            }

            // 4. 落库
            opinionMapper.insert(opinion);
            log.info("观点落库成功: {}", msg.getId());

        } catch (Exception e) {
            // 严重异常：可能是数据库挂了，或者字段超长
            log.error("观点落库失败! msg={}", msg, e);
            // 抛出异常，RocketMQ 会自动重试 (默认16次)
//            throw new OpinionWriteException("消费失败,原因:"+e.getMessage());
        }
    }

    /**
     * 简单的敏感词检测
     * 生产环境应接入 阿里云/网易云 文本检测 API
     */
    private boolean isSensitive(String content) {

        // TODO 接入 AI 用于风控检测
        if (content == null) return false;
        return content.contains("fuck") || content.contains("死") || content.contains("垃圾");
    }

    /**
     * int 转 Enum 辅助方法
     */
    private OpinionStandpoint convertStandpoint(Integer code) {
        for (OpinionStandpoint s : OpinionStandpoint.values()) {
            if (s.getCode() == code) return s;
        }
        return OpinionStandpoint.NEUTRAL; // 默认中立
    }
}
