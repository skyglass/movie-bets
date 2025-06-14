package net.skycomposer.moviebets.market.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class MarketNotFoundException extends IllegalArgumentException {

    public MarketNotFoundException(UUID marketId) {
        super(String.format("Couldn't find market %s", marketId));
    }

}


