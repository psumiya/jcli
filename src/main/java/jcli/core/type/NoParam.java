package jcli.core.type;

import java.time.Instant;
import java.util.function.Supplier;

public enum NoParam {

    hello(() -> "Welcome to Jcli! Type `jcli -h` to view help manual."),
    now(() -> String.valueOf(Instant.now())),
    getEpochSecond(() -> String.valueOf(Instant.now().getEpochSecond())),
    getNano(() -> String.valueOf(Instant.now().getNano())),
    toEpochMilli(() -> String.valueOf(Instant.now().toEpochMilli())),
    uuid(() -> java.util.UUID.randomUUID().toString());

    private final Supplier<String> supplier;

    NoParam(Supplier<String> supplier) {
        this.supplier = supplier;
    }

    public String run() {
        return this.supplier.get();
    }

}
