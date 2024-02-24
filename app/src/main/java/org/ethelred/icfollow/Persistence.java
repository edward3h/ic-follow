package org.ethelred.icfollow;

import java.util.function.Consumer;

public interface Persistence {
    void store(Result result);
    void load(Consumer<Result> resultConsumer);
}
