package net.skycomposer.moviebets.bet.exception;

public class UserItemStatusRequestDeniedException extends IllegalArgumentException {

    public UserItemStatusRequestDeniedException(String authenticatedCustomerId, String betCustomerId) {
        super(String.format("Authenticated customer %s can't create bets for other customer %s", authenticatedCustomerId, betCustomerId));
    }
}
