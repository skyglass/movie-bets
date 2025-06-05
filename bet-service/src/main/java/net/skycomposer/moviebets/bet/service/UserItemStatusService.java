package net.skycomposer.moviebets.bet.service;

import lombok.RequiredArgsConstructor;
import net.skycomposer.moviebets.bet.dao.repository.UserItemStatusRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserItemStatusService {

    private final UserItemStatusRepository userItemStatusRepository;
}
