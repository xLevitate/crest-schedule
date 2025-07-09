package com.moocrest.scheduler;

import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.moocrest.scheduler.Scheduler;
import com.moocrest.scheduler.group.TaskGroup;

import static org.junit.jupiter.api.Assertions.*;

class TaskGroupTest {
    private Plugin plugin;

    @BeforeEach
    void setUp() {
        plugin = Mockito.mock(Plugin.class);
        Mockito.when(plugin.getName()).thenReturn("TestPlugin");
        Scheduler.initialize(plugin);
    }

    @Test
    void testTaskGroupCreation() {
        TaskGroup group = Scheduler.group("test-group");

        assertNotNull(group);
        assertEquals("test-group", group.getName());
        assertEquals(0, group.getActiveTaskCount());
    }

    @Test
    void testTaskGroupBuilderCreation() {
        TaskGroup group = Scheduler.group("sync-group");

        assertNotNull(group.sync());
        assertNotNull(group.async());
        assertNotNull(group.sync().delay(10));
        assertNotNull(group.async().repeat(20));
    }

    @Test
    void testTaskGroupName() {
        TaskGroup group1 = Scheduler.group("group1");
        TaskGroup group2 = Scheduler.group("group2");

        assertEquals("group1", group1.getName());
        assertEquals("group2", group2.getName());
        assertNotEquals(group1.getName(), group2.getName());
    }

    @Test
    void testTaskGroupBuilderChaining() {
        TaskGroup group = Scheduler.group("chain-group");

        assertNotNull(group.sync().delay(10).repeat(20).times(3));
        assertNotNull(group.async().delay(5).timeout(100));
        assertNotNull(group.sync().onError(throwable -> {
        }));
        assertNotNull(group.async().onTimeout(() -> {
        }));
    }

    @Test
    void testMultipleTaskGroups() {
        TaskGroup group1 = Scheduler.group("group1");
        TaskGroup group2 = Scheduler.group("group2");
        TaskGroup group3 = Scheduler.group("group3");

        assertNotNull(group1);
        assertNotNull(group2);
        assertNotNull(group3);

        assertEquals("group1", group1.getName());
        assertEquals("group2", group2.getName());
        assertEquals("group3", group3.getName());

        assertEquals(0, group1.getActiveTaskCount());
        assertEquals(0, group2.getActiveTaskCount());
        assertEquals(0, group3.getActiveTaskCount());
    }
}