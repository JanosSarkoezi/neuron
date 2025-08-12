package com.example.sandbox.validator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BoundaryTest {

    @Test
    void testEnumValues() {
        Boundary[] boundaries = Boundary.values();
        assertEquals(2, boundaries.length);
        assertEquals(Boundary.OPEN, boundaries[0]);
        assertEquals(Boundary.CLOSED, boundaries[1]);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(Boundary.OPEN, Boundary.valueOf("OPEN"));
        assertEquals(Boundary.CLOSED, Boundary.valueOf("CLOSED"));
    }
}
