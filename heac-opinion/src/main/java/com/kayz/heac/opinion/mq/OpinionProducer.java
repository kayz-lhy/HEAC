package com.kayz.heac.opinion.mq;

import com.kayz.heac.opinion.domain.dto.OpinionPostMsgDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpinionProducer {

    // Topic 定义 (建议在 Nacos 配置中管理，这里简化硬编码)
    private static final String TOPIC_OPINION_POST = "opinion-topic:post";
    private final RocketMQTemplate rocketMQTemplate;

    /**
     * 发送发表观点消息 (异步发送)
     * 异步发送性能最好，不会阻塞主线程等待 Broker 确认
     */
    public void sendPostMessage(OpinionPostMsgDTO msg) {
        rocketMQTemplate.asyncSend(TOPIC_OPINION_POST, msg, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.debug("观点发表消息发送成功: msgId={}, opinionId={}",
                        sendResult.getMsgId(), msg.getId());
            }

            @Override
            public void onException(Throwable e) {
                // 严重错误：消息没发出去，数据丢了！
                // TODO: 生产环境应写入本地"失败消息表"或告警
                log.error("观点发表消息发送失败! opinionId={}", msg.getId(), e);
            }
        });
    }
}
