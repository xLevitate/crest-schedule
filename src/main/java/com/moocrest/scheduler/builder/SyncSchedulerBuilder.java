package com.moocrest.scheduler.builder;

import org.bukkit.scheduler.BukkitTask;

import com.moocrest.scheduler.ScheduledTask;
import com.moocrest.scheduler.impl.ScheduledTaskImpl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SyncSchedulerBuilder extends BaseSchedulerBuilder<SyncSchedulerBuilder> {

    @Override
    protected ScheduledTask executeTask(Supplier<Object> task) {
        if (timeout > 0) {
            return executeWithTimeout(task);
        }

        BukkitTask bukkitTask = scheduleTask(() -> {
            try {
                task.get();
            } catch (Exception e) {
                handleError(e);
            }
        }, false);

        return new ScheduledTaskImpl(bukkitTask);
    }

    @Override
    protected ScheduledTask executeRepeatingTask(Supplier<Boolean> task) {
        if (repeat <= 0) {
            throw new IllegalStateException("Repeat interval must be set for repeating tasks");
        }

        AtomicInteger executionCount = new AtomicInteger(0);
        final BukkitTask[] taskRef = new BukkitTask[1];

        BukkitTask bukkitTask = scheduleRepeatingTask(() -> {
            try {
                boolean shouldContinue = task.get();

                if (!shouldContinue || (times > 0 && executionCount.incrementAndGet() >= times)) {
                    if (taskRef[0] != null) {
                        taskRef[0].cancel();
                    }
                    return;
                }
            } catch (Exception e) {
                handleError(e);
                if (taskRef[0] != null) {
                    taskRef[0].cancel();
                }
            }
        }, false);

        taskRef[0] = bukkitTask;
        return new ScheduledTaskImpl(bukkitTask);
    }

    private ScheduledTask executeWithTimeout(Supplier<Object> task) {
        CompletableFuture<Object> future = new CompletableFuture<>();

        BukkitTask timeoutTask = scheduleTask(() -> {
            if (!future.isDone()) {
                future.cancel(true);
                handleTimeout();
            }
        }, false);

        BukkitTask mainTask = scheduleTask(() -> {
            try {
                Object result = task.get();
                future.complete(result);
                timeoutTask.cancel();
            } catch (Exception e) {
                future.completeExceptionally(e);
                timeoutTask.cancel();
                handleError(e);
            }
        }, false);

        return new ScheduledTaskImpl(mainTask) {
            @Override
            public void cancel() {
                super.cancel();
                timeoutTask.cancel();
                future.cancel(true);
            }
        };
    }

    public SyncSchedulerBuilder thenRun(Runnable task) {
        SyncSchedulerBuilder nextBuilder = new SyncSchedulerBuilder();
        nextBuilder.delay = 0;

        executeTask(() -> {
            nextBuilder.run(task);
            return null;
        });

        return nextBuilder;
    }

    public <R> AsyncSchedulerBuilder thenAsync(Supplier<R> task) {
        AsyncSchedulerBuilder asyncBuilder = new AsyncSchedulerBuilder();

        executeTask(() -> {
            asyncBuilder.run(() -> {
                try {
                    return task.get();
                } catch (Exception e) {
                    handleError(e);
                    return null;
                }
            });
            return null;
        });

        return asyncBuilder;
    }

    public <R> CompletableFuture<R> supply(Supplier<R> supplier) {
        CompletableFuture<R> future = new CompletableFuture<>();

        executeTask(() -> {
            try {
                R result = supplier.get();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
                handleError(e);
            }
            return null;
        });

        return future;
    }
}