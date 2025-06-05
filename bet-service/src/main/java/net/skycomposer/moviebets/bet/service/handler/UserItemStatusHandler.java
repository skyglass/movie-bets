package net.skycomposer.moviebets.bet.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.skycomposer.moviebets.bet.service.UserItemStatusService;

@Component
@KafkaListener(topics = "${user.item.status.topic.name}", groupId = "${spring.kafka.consumer.user-item-status.group-id}")
@RequiredArgsConstructor
public class UserItemStatusHandler {

    private final UserItemStatusService userItemStatusService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


}
