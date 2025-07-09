package com.moocrest.scheduler;

import org.bukkit.plugin.Plugin;

import com.moocrest.scheduler.builder.AsyncSchedulerBuilder;
import com.moocrest.scheduler.builder.SyncSchedulerBuilder;
import com.moocrest.scheduler.group.TaskGroup;

public final class Scheduler {
    private static Plugin plugin;

    public static void initialize(Plugin plugin) {
        Scheduler.plugin = plugin;
    }

    public static Plugin getPlugin() {
        if (plugin == null) {
            throw new IllegalStateException("Scheduler not initialized. Call Scheduler.initialize(plugin) first.");
        }
        return plugin;
    }

    public static SyncSchedulerBuilder sync() {
        return new SyncSchedulerBuilder();
    }

    public static AsyncSchedulerBuilder async() {
        return new AsyncSchedulerBuilder();
    }

    public static TaskGroup group(String name) {
        return new TaskGroup(name);
    }

    public static TaskGroup createTaskGroup() {
        return new TaskGroup("default");
    }
}