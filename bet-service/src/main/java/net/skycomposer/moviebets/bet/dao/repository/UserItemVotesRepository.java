package net.skycomposer.moviebets.bet.dao.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.skycomposer.moviebets.bet.dao.entity.UserItemVotesEntity;

@Repository
public interface UserItemVotesRepository extends JpaRepository<UserItemVotesEntity, UUID> {

}
