package net.skycomposer.moviebets.bet.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import net.skycomposer.moviebets.bet.dao.repository.UserItemVotesRepository;
import net.skycomposer.moviebets.common.dto.bet.commands.UserVotesUpdateCommand;
import net.skycomposer.moviebets.common.dto.item.ItemType;
import net.skycomposer.moviebets.common.dto.votes.UserItemVotes;
import net.skycomposer.moviebets.common.dto.votes.UserItemVotesList;

@Service
@RequiredArgsConstructor
public class UserItemVotesService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserItemVotesRepository userItemVotesRepository;

    @Transactional
    public void updateUserVotesAndFriendWeights(UserVotesUpdateCommand command) {
        userItemVotesRepository.updateUserVotesAndFriendWeights(
                command.getMarketId(), command.getItemWon(),
                command.getItemLost(), command.getItemType().getValue());
    }

    @Transactional(readOnly = true)
    public UserItemVotesList getRecommendedMoviesExcludingVoted(String userId) {
        List<UserItemVotes> result = userItemVotesRepository.findFriendWeightedVotesExcludingVoted(userId, ItemType.MOVIE);
        return new UserItemVotesList(result);
    }

    @Transactional(readOnly = true)
    public UserItemVotesList getRecommendedMovies(String userId) {
        List<UserItemVotes> result = userItemVotesRepository.findFriendWeightedVotesWithVotedFlag(userId, ItemType.MOVIE);
        return new UserItemVotesList(result);
    }
}
