package jcli.core;

import jcli.core.type.NoParam;

import static jcli.core.type.NoParam.*;

public interface NoInputTemplate extends BaseTemplate {

    NoParam getFunction();

    @Override
    default void run() {
        init();
        if (isValid()) {
            String result = execute();
            System.out.println(result);
        }
    }

    default String execute() {
        NoParam function = getFunction();
        return switch (function) {
            case hello -> hello.run();
            case uuid -> uuid.run();
            case now -> now.run();
            case getEpochSecond -> getEpochSecond.run();
            case getNano -> getNano.run();
            case toEpochMilli -> toEpochMilli.run();
        };
    }

}
