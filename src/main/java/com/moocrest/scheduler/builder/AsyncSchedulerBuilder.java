package com.moocrest.scheduler.builder;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import com.moocrest.scheduler.ScheduledTask;
import com.moocrest.scheduler.Scheduler;
import com.moocrest.scheduler.impl.ScheduledTaskImpl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AsyncSchedulerBuilder extends BaseSchedulerBuilder<AsyncSchedulerBuilder> {

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
        }, true);

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
        }, true);

        taskRef[0] = bukkitTask;
        return new ScheduledTaskImpl(bukkitTask);
    }

    private ScheduledTask executeWithTimeout(Supplier<Object> task) {
        CompletableFuture<Object> future = new CompletableFuture<>();

        BukkitTask timeoutTask = Bukkit.getScheduler().runTaskLaterAsynchronously(
                Scheduler.getPlugin(),
                () -> {
                    if (!future.isDone()) {
                        future.cancel(true);
                        handleTimeout();
                    }
                },
                timeout);

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
        }, true);

        return new ScheduledTaskImpl(mainTask) {
            @Override
            public void cancel() {
                super.cancel();
                timeoutTask.cancel();
                future.cancel(true);
            }
        };
    }

    public <R> ScheduledTask run(Supplier<R> supplier) {
        return executeTask(() -> {
            try {
                lastResult = supplier.get();
                return lastResult;
            } catch (Exception e) {
                handleError(e);
                return null;
            }
        });
    }

    public <R> AsyncSchedulerBuilder storeResult(Supplier<R> supplier) {
        executeTask(() -> {
            try {
                lastResult = supplier.get();
                return lastResult;
            } catch (Exception e) {
                handleError(e);
                return null;
            }
        });
        return this;
    }

    public <R> SyncSchedulerBuilder thenSync(Consumer<R> callback) {
        SyncSchedulerBuilder syncBuilder = new SyncSchedulerBuilder();

        CompletableFuture<R> future = new CompletableFuture<>();

        executeTask(() -> {
            try {
                R result = (R) lastResult;
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
                handleError(e);
            }
            return null;
        });

        future.thenAccept(result -> {
            Bukkit.getScheduler().runTask(Scheduler.getPlugin(), () -> {
                try {
                    callback.accept(result);
                } catch (Exception e) {
                    handleError(e);
                }
            });
        });

        return syncBuilder;
    }

    public <R, T> SyncSchedulerBuilder thenSync(Function<R, T> callback) {
        SyncSchedulerBuilder syncBuilder = new SyncSchedulerBuilder();

        CompletableFuture<R> future = new CompletableFuture<>();

        executeTask(() -> {
            try {
                R result = (R) lastResult;
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
                handleError(e);
            }
            return null;
        });

        future.thenAccept(result -> {
            Bukkit.getScheduler().runTask(Scheduler.getPlugin(), () -> {
                try {
                    callback.apply(result);
                } catch (Exception e) {
                    handleError(e);
                }
            });
        });

        return syncBuilder;
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

    private Object lastResult;
}