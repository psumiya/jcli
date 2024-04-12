package jcli.core.type;

import java.time.Instant;
import java.util.function.Function;

public enum LongInput {

    ofEpochSecond((Long epochSecond) -> String.valueOf(Instant.ofEpochSecond(epochSecond)));

    private final Function<Long, String> longFunction;

    LongInput(Function<Long, String> longFunction) {
        this.longFunction = longFunction;
    }

    public String run(Long longParam) {
        return this.longFunction.apply(longParam);
    }

}
