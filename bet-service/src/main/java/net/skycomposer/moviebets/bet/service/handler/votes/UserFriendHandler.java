package net.skycomposer.moviebets.bet.service.handler.votes;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = "${user.friend.topic.name}", groupId = "${spring.kafka.consumer.user-friend.group-id}")
public class UserFriendHandler {

}
