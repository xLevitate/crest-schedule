package com.moocrest.scheduler.group;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.moocrest.scheduler.ScheduledTask;
import com.moocrest.scheduler.builder.AsyncSchedulerBuilder;
import com.moocrest.scheduler.builder.SyncSchedulerBuilder;

public class TaskGroup {
    private final String name;
    private final ConcurrentMap<Long, ScheduledTask> tasks = new ConcurrentHashMap<>();
    private final AtomicLong taskIdCounter = new AtomicLong(0);

    public TaskGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public GroupSyncSchedulerBuilder sync() {
        return new GroupSyncSchedulerBuilder(this);
    }

    public GroupAsyncSchedulerBuilder async() {
        return new GroupAsyncSchedulerBuilder(this);
    }

    public void cancelAll() {
        tasks.values().forEach(ScheduledTask::cancel);
        tasks.clear();
    }

    public void cancelTask(long taskId) {
        ScheduledTask task = tasks.remove(taskId);
        if (task != null) {
            task.cancel();
        }
    }

    public int getActiveTaskCount() {
        tasks.entrySet().removeIf(entry -> entry.getValue().isCancelled());
        return tasks.size();
    }

    long addTask(ScheduledTask task) {
        long taskId = taskIdCounter.incrementAndGet();
        tasks.put(taskId, task);
        return taskId;
    }

    void removeTask(long taskId) {
        tasks.remove(taskId);
    }

    public static class GroupSyncSchedulerBuilder extends SyncSchedulerBuilder {
        private final TaskGroup group;

        public GroupSyncSchedulerBuilder(TaskGroup group) {
            this.group = group;
        }

        @Override
        public ScheduledTask run(Runnable task) {
            ScheduledTask scheduledTask = super.run(task);
            long taskId = group.addTask(scheduledTask);

            return new GroupScheduledTask(scheduledTask, group, taskId);
        }
    }

    public static class GroupAsyncSchedulerBuilder extends AsyncSchedulerBuilder {
        private final TaskGroup group;

        public GroupAsyncSchedulerBuilder(TaskGroup group) {
            this.group = group;
        }

        @Override
        public ScheduledTask run(Runnable task) {
            ScheduledTask scheduledTask = super.run(task);
            long taskId = group.addTask(scheduledTask);

            return new GroupScheduledTask(scheduledTask, group, taskId);
        }
    }

    private static class GroupScheduledTask implements ScheduledTask {
        private final ScheduledTask delegate;
        private final TaskGroup group;
        private final long taskId;

        public GroupScheduledTask(ScheduledTask delegate, TaskGroup group, long taskId) {
            this.delegate = delegate;
            this.group = group;
            this.taskId = taskId;
        }

        @Override
        public void cancel() {
            delegate.cancel();
            group.removeTask(taskId);
        }

        @Override
        public boolean isCancelled() {
            return delegate.isCancelled();
        }

        @Override
        public org.bukkit.scheduler.BukkitTask getBukkitTask() {
            return delegate.getBukkitTask();
        }
    }
}