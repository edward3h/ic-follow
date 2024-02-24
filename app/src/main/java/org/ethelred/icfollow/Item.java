package org.ethelred.icfollow;

import io.avaje.jsonb.Json;

import java.util.Objects;

@Json
public record Item(@Json.Alias("result") String name, String emoji) implements Node, Comparable<Item> {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(name, item.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return emoji + " " + name;
    }

    @Override
    public int compareTo(Item o) {
        return name.compareTo(o.name);
    }
}
