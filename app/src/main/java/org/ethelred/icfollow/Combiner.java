package org.ethelred.icfollow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

public class Combiner {
    private static final Logger STATS_LOGGER = LoggerFactory.getLogger("stats");
    private static final long STATS_TIME = 4L * 60L * 1000L;
    private final Set<Item> allItems = new LinkedHashSet<>();
    private final Map<Pair, Item> results = new HashMap<>();

    private final List<Pair> todo = new ArrayList<>();
    private final PairClient client;
    private final Persistence persistence;
    private final Consumer<Result> resultConsumer;
    private final Clock clock = Clock.systemDefaultZone();
    private final Random random = new Random();

    public Combiner(PairClient client, List<Item> initialItems, Persistence persistence, Consumer<Result> resultConsumer) {
        this.client = client;
        this.persistence = persistence;
        this.resultConsumer = resultConsumer;

        initialItems.forEach(this::addItem);

        Set<Item> toAdd = new LinkedHashSet<>();
        persistence.load(result -> {
            results.put(result.pair(), result.result());
            resultConsumer.accept(result);
            toAdd.add(result.pair().first());
            toAdd.add(result.pair().second());
            toAdd.add(result.result());
        });
        toAdd.forEach(this::addItem);
    }

    public void addItem(Item item) {
        if ("Nothing".equals(item.name())) {
            return;
        }
        if (allItems.add(item)) {
            generatePairs(item);
        }
    }


    private void generatePairs(Item item) {
        for (var i2 : allItems) {
            if ("Nothing".equals(i2.name())) {
                continue;
            }
            var next = new Pair(item, i2);
            if (!results.containsKey(next)) {
                todo.add(next);
            }
        }
    }

    void awaitCompletion() {
        long timestamp = clock.millis();
        int count = 30000;
        for (Pair nextPair = todo.remove(random.nextInt(todo.size())); !todo.isEmpty() && count > 0; nextPair = todo.remove(random.nextInt(todo.size()))) {
            Result nextResult = client.pair(nextPair);
            if (results.putIfAbsent(nextResult.pair(), nextResult.result()) == null) {
                persistence.store(nextResult);
                resultConsumer.accept(nextResult);
                addItem(nextResult.result());
            }
            count--;
            long now = clock.millis();
            if (timestamp + STATS_TIME < now) {
                timestamp = now;
                logStats();
            }
        }
    }

    private void logStats() {
        STATS_LOGGER.info("{} items. {} results. {} to do.", allItems.size(), results.size(), todo.size());
    }
}
