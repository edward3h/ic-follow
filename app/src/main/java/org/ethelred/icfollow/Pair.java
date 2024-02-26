package org.ethelred.icfollow;

import java.util.Objects;

public record Pair(Item first, Item second) implements Node, Comparable<Pair> {
    public Pair {
        if (first.compareTo(second) > 0) {
            Item tmpFirst = first;
            first = second;
            second = tmpFirst;
        }
    }

    @Override
    public String toString() {
        return "(" + first().toString() + "," + second.toString() + ")";
    }

    @Override
    public int compareTo(Pair o) {
        var r = first.compareTo(o.first);
        if (r == 0) {
            r = second.compareTo(o.second);
        }
        return r;
    }
}
