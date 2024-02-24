package org.ethelred.icfollow;

import java.util.List;

public record Base() {
    public static final List<Item> ELEMENTS = List.of(
            new Item("Water", "\uD83D\uDCA7"),
            new Item("Fire", "\uD83D\uDD25"),
            new Item("Wind", "\uD83C\uDF2C️"),
            new Item("Earth", "\uD83C\uDF0D")
    );
}
