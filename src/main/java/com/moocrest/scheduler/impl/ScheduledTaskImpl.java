package com.moocrest.scheduler.impl;

import org.bukkit.scheduler.BukkitTask;

import com.moocrest.scheduler.ScheduledTask;

public class ScheduledTaskImpl implements ScheduledTask {
    private final BukkitTask bukkitTask;

    public ScheduledTaskImpl(BukkitTask bukkitTask) {
        this.bukkitTask = bukkitTask;
    }

    @Override
    public void cancel() {
        if (bukkitTask != null && !bukkitTask.isCancelled()) {
            bukkitTask.cancel();
        }
    }

    @Override
    public boolean isCancelled() {
        return bukkitTask == null || bukkitTask.isCancelled();
    }

    @Override
    public BukkitTask getBukkitTask() {
        return bukkitTask;
    }
}