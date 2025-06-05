package net.skycomposer.moviebets.bet.service.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import net.skycomposer.moviebets.bet.service.UserItemStatusService;

@Component
public class UserItemStatusApplicationService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final UserItemStatusService userItemStatusService;

    private final String userItemStatusTopicName;

    public UserItemStatusApplicationService(final UserItemStatusService userItemStatusService,
                                            final KafkaTemplate<String, Object> kafkaTemplate,
                                            final @Value("${user.item-status.topic.name}") String userItemStatusTopicName) {
        this.userItemStatusService = userItemStatusService;
        this.kafkaTemplate = kafkaTemplate;
        this.userItemStatusTopicName = userItemStatusTopicName;
    }
}
