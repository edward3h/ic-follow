package org.ethelred.icgraph;

import org.ethelred.icfollow.Base;
import org.ethelred.icfollow.FilePersistence;
import org.ethelred.icfollow.Item;
import org.ethelred.icfollow.Node;
import org.ethelred.icfollow.Pair;
import org.ethelred.icfollow.Persistence;
import org.ethelred.icfollow.Result;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.jgrapht.Graphs.*;

public class Main implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private final Graph<Node, DefaultEdge> graph = GraphTypeBuilder
            .directed()
            .allowingMultipleEdges(false)
            .allowingSelfLoops(false)
            .vertexClass(Node.class)
            .edgeClass(DefaultEdge.class)
            .buildGraph();

    private final Map<Pair, Item> results = new HashMap<>();
    private final Map<Node, Integer> distances = new HashMap<>();

    public static void main(String[] args) {
        new Main().run();
    }

    @Override
    public void run() {
        addAllVertices(graph, Base.ELEMENTS);
        Persistence p = new FilePersistence(Path.of("icfollow.psv"));
        p.load(this::loadResult);
        Set<Item> items = new HashSet<>(Base.ELEMENTS);
        Set<Pair> pairs = new HashSet<>();
        for (int i = 0; i < 15; i++) {
            pairs.addAll(allPairs(items));
            int finalI = i;
            items.forEach(item -> distances.putIfAbsent(item, finalI));
            pairs.forEach(pair -> distances.putIfAbsent(pair, finalI));
            logItems(i);
            items.addAll(allResults(pairs));
        }

        var test = node("Human");
        Set<Node> incoming = graph.incomingEdgesOf(test).stream().map(graph::getEdgeSource).filter(distances::containsKey).collect(Collectors.toSet());
        LOGGER.info("{}\nsize {}", incoming, incoming.size());

    }

    private void logItems(int distance) {
        Set<Item> items = distances.entrySet()
                .stream()
                .filter(e -> e.getValue() == distance)
                .map(Map.Entry::getKey)
                .filter(i -> i instanceof Item)
                .map(i -> (Item) i)
                .collect(Collectors.toCollection(TreeSet::new));
        LOGGER.info("{}: {}", distance, items.size());
    }

    private Collection<Item> allResults(Set<Pair> pairs) {
        return pairs.stream()
                .map(results::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Collection<Pair> allPairs(Set<Item> items) {
        var r = new HashSet<Pair>();
        for (var first: items) {
            for (var second: items) {
                r.add(new Pair(first, second));
            }
        }
        return r;
    }

    private Item node(String name) {
        return new Item(name, "");
    }

    private Pair node(String first, String second) {
        return new Pair(node(first), node(second));
    }

    private void loadResult(Result result) {
        addEdgeWithVertices(graph, result.pair(), result.result());
        addEdgeWithVertices(graph, result.pair().first(), result.pair());
        addEdgeWithVertices(graph, result.pair().second(), result.pair());
        results.put(result.pair(), result.result());
    }
}
