package net.skycomposer.moviebets.bet.service.handler.votes;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = "${item.user.topic.name}", groupId = "${spring.kafka.consumer.item-user.group-id}")
public class ItemUserHandler {
}
