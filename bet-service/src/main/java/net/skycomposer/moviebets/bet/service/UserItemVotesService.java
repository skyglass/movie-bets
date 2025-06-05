package net.skycomposer.moviebets.bet.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import net.skycomposer.moviebets.bet.dao.repository.UserItemVotesRepository;

@Service
@RequiredArgsConstructor
public class UserItemVotesService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserItemVotesRepository userItemVotesRepository;
}
