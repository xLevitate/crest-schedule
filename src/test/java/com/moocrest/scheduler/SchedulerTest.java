package com.moocrest.scheduler;

import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.moocrest.scheduler.Scheduler;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerTest {
    private Plugin plugin;

    @BeforeEach
    void setUp() {
        plugin = Mockito.mock(Plugin.class);
        Mockito.when(plugin.getName()).thenReturn("TestPlugin");
        Scheduler.initialize(plugin);
    }

    @Test
    void testSchedulerInitialization() {
        assertNotNull(Scheduler.getPlugin());
        assertEquals("TestPlugin", Scheduler.getPlugin().getName());
    }

    @Test
    void testSyncBuilderCreation() {
        assertNotNull(Scheduler.sync());
        assertNotNull(Scheduler.sync().delay(10));
        assertNotNull(Scheduler.sync().repeat(20));
        assertNotNull(Scheduler.sync().times(3));
    }

    @Test
    void testAsyncBuilderCreation() {
        assertNotNull(Scheduler.async());
        assertNotNull(Scheduler.async().delay(10));
        assertNotNull(Scheduler.async().repeat(20));
        assertNotNull(Scheduler.async().times(3));
    }

    @Test
    void testTaskGroupCreation() {
        assertNotNull(Scheduler.group("test-group"));
        assertNotNull(Scheduler.createTaskGroup());

        assertEquals("test-group", Scheduler.group("test-group").getName());
        assertEquals("default", Scheduler.createTaskGroup().getName());
    }

    @Test
    void testBuilderChaining() {
        assertNotNull(Scheduler.sync().delay(10).repeat(20).times(3));
        assertNotNull(Scheduler.async().delay(5).timeout(100));
    }

    @Test
    void testErrorHandlerSetting() {
        assertNotNull(Scheduler.sync().onError(throwable -> {
        }));
        assertNotNull(Scheduler.async().onError(throwable -> {
        }));
    }

    @Test
    void testTimeoutHandlerSetting() {
        assertNotNull(Scheduler.sync().onTimeout(() -> {
        }));
        assertNotNull(Scheduler.async().onTimeout(() -> {
        }));
    }
}