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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair pair = (Pair) o;
        return (Objects.equals(first, pair.first) && Objects.equals(second, pair.second)) || (Objects.equals(first, pair.second) && Objects.equals(second, pair.first));
    }

    @Override
    public int hashCode() {
        return first.hashCode() + second.hashCode();
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
