package com.moocrest.scheduler;

import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.moocrest.scheduler.Scheduler;
import com.moocrest.scheduler.builder.AsyncSchedulerBuilder;

import static org.junit.jupiter.api.Assertions.*;

class AsyncSchedulerTest {
    private Plugin plugin;

    @BeforeEach
    void setUp() {
        plugin = Mockito.mock(Plugin.class);
        Mockito.when(plugin.getName()).thenReturn("TestPlugin");
        Scheduler.initialize(plugin);
    }

    @Test
    void testAsyncBuilderCreation() {
        assertNotNull(Scheduler.async());
        assertNotNull(Scheduler.async().delay(10));
        assertNotNull(Scheduler.async().repeat(20));
        assertNotNull(Scheduler.async().timeout(100));
    }

    @Test
    void testAsyncBuilderChaining() {
        assertNotNull(Scheduler.async().delay(10).repeat(20).times(3));
        assertNotNull(Scheduler.async().timeout(100).onTimeout(() -> {
        }));
        assertNotNull(Scheduler.async().onError(throwable -> {
        }));
        assertNotNull(Scheduler.async().delay(5).timeout(50));
    }

    @Test
    void testAsyncBuilderMethods() {
        AsyncSchedulerBuilder builder = Scheduler.async();
        assertNotNull(builder);
        assertNotNull(builder.delay(10));
        assertNotNull(builder.repeat(20));
        assertNotNull(builder.times(5));
    }

    @Test
    void testAsyncBuilderErrorHandling() {
        AsyncSchedulerBuilder builder = Scheduler.async();
        assertNotNull(builder.onError(throwable -> {
        }));
        assertNotNull(builder.onTimeout(() -> {
        }));
    }

    @Test
    void testAsyncWithDelayAndTimeout() {
        assertNotNull(Scheduler.async().delay(10).timeout(100));
        assertNotNull(Scheduler.async().timeout(50).delay(5));
    }
}