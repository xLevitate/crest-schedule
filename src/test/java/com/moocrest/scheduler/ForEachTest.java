package com.moocrest.scheduler;

import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.moocrest.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ForEachTest {
    private Plugin plugin;

    @BeforeEach
    void setUp() {
        plugin = Mockito.mock(Plugin.class);
        Mockito.when(plugin.getName()).thenReturn("TestPlugin");
        Scheduler.initialize(plugin);
    }

    @Test
    void testForEachBuilderCreation() {
        List<String> items = Arrays.asList("apple", "banana", "cherry");

        assertNotNull(Scheduler.sync().forEach(items));
        assertNotNull(Scheduler.async().forEach(items));
        assertNotNull(Scheduler.sync().forEach(items).delay(5));
        assertNotNull(Scheduler.async().forEach(items).delay(10));
    }

    @Test
    void testForEachWithDifferentTypes() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        List<String> strings = Arrays.asList("a", "b", "c");

        assertNotNull(Scheduler.sync().forEach(numbers));
        assertNotNull(Scheduler.async().forEach(strings));
        assertNotNull(Scheduler.sync().forEach(numbers).delay(2));
        assertNotNull(Scheduler.async().forEach(strings).delay(100));
    }

    @Test
    void testForEachWithEmptyList() {
        List<String> emptyList = new ArrayList<>();

        assertNotNull(Scheduler.sync().forEach(emptyList));
        assertNotNull(Scheduler.async().forEach(emptyList).delay(5));
    }

    @Test
    void testForEachBuilderChaining() {
        List<String> items = Arrays.asList("item1", "item2", "item3", "item4", "item5");

        assertNotNull(Scheduler.sync().forEach(items).delay(5));
        assertNotNull(Scheduler.async().forEach(items).delay(100));
        assertNotNull(Scheduler.sync().forEach(items).delay(0));
        assertNotNull(Scheduler.async().forEach(items).delay(1));
    }

    @Test
    void testForEachWithSingleItem() {
        List<String> singleItem = Arrays.asList("only");

        assertNotNull(Scheduler.sync().forEach(singleItem));
        assertNotNull(Scheduler.async().forEach(singleItem).delay(7));
        assertNotNull(Scheduler.sync().forEach(singleItem).delay(1));
    }

    @Test
    void testForEachWithLargeList() {
        List<Integer> largeList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            largeList.add(i);
        }

        assertNotNull(Scheduler.sync().forEach(largeList));
        assertNotNull(Scheduler.async().forEach(largeList).delay(1));
        assertEquals(100, largeList.size());
    }
}