package net.skycomposer.moviebets.bet.service.handler.votes;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = "${friend.user.topic.name}", groupId = "${spring.kafka.consumer.friend-user.group-id}")
public class FriendUserHandler {

}
