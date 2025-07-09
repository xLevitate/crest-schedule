package com.moocrest.scheduler;

import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

import com.moocrest.scheduler.Scheduler;
import com.moocrest.scheduler.builder.AsyncSchedulerBuilder;
import com.moocrest.scheduler.builder.SyncSchedulerBuilder;
import com.moocrest.scheduler.group.TaskGroup;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SimpleSchedulerTest {

    @Test
    void testSchedulerInitialization() {
        Plugin mockPlugin = mock(Plugin.class);

        assertDoesNotThrow(() -> {
            Scheduler.initialize(mockPlugin);
        });

        assertEquals(mockPlugin, Scheduler.getPlugin());
    }

    @Test
    void testSyncBuilderCreation() {
        Plugin mockPlugin = mock(Plugin.class);
        Scheduler.initialize(mockPlugin);

        SyncSchedulerBuilder builder = Scheduler.sync();
        assertNotNull(builder);
    }

    @Test
    void testAsyncBuilderCreation() {
        Plugin mockPlugin = mock(Plugin.class);
        Scheduler.initialize(mockPlugin);

        AsyncSchedulerBuilder builder = Scheduler.async();
        assertNotNull(builder);
    }

    @Test
    void testTaskGroupCreation() {
        Plugin mockPlugin = mock(Plugin.class);
        Scheduler.initialize(mockPlugin);

        TaskGroup group = Scheduler.createTaskGroup();
        assertNotNull(group);
        assertEquals(0, group.getActiveTaskCount());
    }

    @Test
    void testBuilderChaining() {
        Plugin mockPlugin = mock(Plugin.class);
        Scheduler.initialize(mockPlugin);

        SyncSchedulerBuilder syncBuilder = Scheduler.sync()
                .delay(10)
                .repeat(20)
                .times(5);

        assertNotNull(syncBuilder);

        AsyncSchedulerBuilder asyncBuilder = Scheduler.async()
                .delay(5)
                .timeout(100);

        assertNotNull(asyncBuilder);
    }
}