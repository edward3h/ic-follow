package org.ethelred.icfollow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PairTest {
    @Test
    public void testReorder() {
        var i1 = new Item("Earth", "");
        var i2 = new Item("Wind", "");
        var p1 = new Pair(i1, i2);
        var p2 = new Pair(i2, i1);
        assertEquals(p1, p2);
        assertEquals(p2.first(), i1);
    }
}
