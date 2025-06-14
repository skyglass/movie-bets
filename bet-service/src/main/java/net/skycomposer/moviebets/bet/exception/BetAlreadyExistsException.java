package net.skycomposer.moviebets.bet.exception;

public class BetAlreadyExistsException extends IllegalArgumentException {

    public BetAlreadyExistsException(String customerId, String marketName) {
        super(String.format("Duplicate Bet Request: Only one bet is allowed for customer '%s' and event '%s'", customerId, marketName));
    }
}
