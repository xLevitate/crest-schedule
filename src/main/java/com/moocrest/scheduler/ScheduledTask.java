package com.moocrest.scheduler;

import org.bukkit.scheduler.BukkitTask;

public interface ScheduledTask {
    void cancel();

    boolean isCancelled();

    BukkitTask getBukkitTask();
}