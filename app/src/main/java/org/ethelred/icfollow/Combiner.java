package org.ethelred.icfollow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Combiner {
    private static final Logger STATS_LOGGER = LoggerFactory.getLogger("stats");
    private static final Logger LOGGER = LoggerFactory.getLogger(Combiner.class);
    private static final long STATS_TIME = 4L * 60L * 1000L;
    private final Set<Item> allItems = new LinkedHashSet<>();
    private final List<Set<Item>> itemsByDistance = new ArrayList<>();
    private final Map<Pair, Item> results = new HashMap<>();

    private final List<Pair> allOutstandingPairs = new ArrayList<>();
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
        //noinspection SequencedCollectionMethodCanBeUsed
        itemsByDistance.add(0, Set.copyOf(initialItems));

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
                allOutstandingPairs.add(next);
            }
        }
    }

    private Set<Pair> allPairs(Set<Item> items) {
        var r = new HashSet<Pair>();
        for (var first : items) {
            for (var second : items) {
                r.add(new Pair(first, second));
            }
        }
        return r;
    }

    void awaitCompletion() {
        long timestamp = clock.millis();
        for (int distance = 1; distance < 8; distance++) {
            itemsByDistance.add(distance, new HashSet<>());
            Set<Item> inputItems = IntStream.rangeClosed(0, distance - 1).mapToObj(itemsByDistance::get).flatMap(Set::stream).collect(Collectors.toSet());
            Set<Pair> inputPairs = allPairs(inputItems);
            for (var inputPair : inputPairs) {
                var item = getResult(inputPair);
                if (!inputItems.contains(item)) {
                    if (itemsByDistance.get(distance).add(item)) {
                        LOGGER.debug("{}: {}", distance, item);
                    }
                }

                long now = clock.millis();
                if (timestamp + STATS_TIME < now) {
                    timestamp = now;
                    logStats();
                }
            }
            STATS_LOGGER.info("Distance {} complete. {} items", distance, itemsByDistance.get(distance).size());
            logStats();
        }
    }

    private Item getResult(Pair pair) {
        return results.computeIfAbsent(pair, p -> {
            Result nextResult = client.pair(pair);
            persistence.store(nextResult);
            resultConsumer.accept(nextResult);
            addItem(nextResult.result());
            return nextResult.result();

        });
    }

    private void logStats() {
        STATS_LOGGER.info("{} items. {} results. {} to do.", allItems.size(), results.size(), allOutstandingPairs.size());
    }
}
