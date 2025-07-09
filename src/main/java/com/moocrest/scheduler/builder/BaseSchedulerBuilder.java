package com.moocrest.scheduler.builder;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import com.moocrest.scheduler.ScheduledTask;
import com.moocrest.scheduler.Scheduler;
import com.moocrest.scheduler.impl.ScheduledTaskImpl;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class BaseSchedulerBuilder<T extends BaseSchedulerBuilder<T>> {
    protected long delay = 0;
    protected long repeat = -1;
    protected int times = -1;
    protected long timeout = -1;
    protected Consumer<Throwable> errorHandler;
    protected Runnable timeoutHandler;

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    public T delay(long ticks) {
        this.delay = ticks;
        return self();
    }

    public T repeat(long ticks) {
        this.repeat = ticks;
        return self();
    }

    public T times(int times) {
        this.times = times;
        return self();
    }

    public T timeout(long ticks) {
        this.timeout = ticks;
        return self();
    }

    public T onError(Consumer<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
        return self();
    }

    public T onTimeout(Runnable timeoutHandler) {
        this.timeoutHandler = timeoutHandler;
        return self();
    }

    public ScheduledTask run(Runnable task) {
        return executeTask(() -> {
            task.run();
            return null;
        });
    }

    public ScheduledTask runIf(BooleanSupplier condition, Runnable task) {
        return run(() -> {
            if (condition.getAsBoolean()) {
                task.run();
            }
        });
    }

    public ScheduledTask runWhile(BooleanSupplier condition, Runnable task) {
        return executeRepeatingTask(() -> {
            if (condition.getAsBoolean()) {
                task.run();
                return true;
            }
            return false;
        });
    }

    public ScheduledTask runUntil(BooleanSupplier condition, Runnable task) {
        return executeRepeatingTask(() -> {
            if (!condition.getAsBoolean()) {
                task.run();
                return true;
            }
            return false;
        });
    }

    public <E> ForEachBuilder<E> forEach(List<E> items) {
        return new ForEachBuilder<>(items, this);
    }

    protected abstract ScheduledTask executeTask(Supplier<Object> task);

    protected abstract ScheduledTask executeRepeatingTask(Supplier<Boolean> task);

    protected BukkitTask scheduleTask(Runnable task, boolean async) {
        if (async) {
            if (delay > 0) {
                return Bukkit.getScheduler().runTaskLaterAsynchronously(Scheduler.getPlugin(), task, delay);
            } else {
                return Bukkit.getScheduler().runTaskAsynchronously(Scheduler.getPlugin(), task);
            }
        } else {
            if (delay > 0) {
                return Bukkit.getScheduler().runTaskLater(Scheduler.getPlugin(), task, delay);
            } else {
                return Bukkit.getScheduler().runTask(Scheduler.getPlugin(), task);
            }
        }
    }

    protected BukkitTask scheduleRepeatingTask(Runnable task, boolean async) {
        if (async) {
            return Bukkit.getScheduler().runTaskTimerAsynchronously(Scheduler.getPlugin(), task, delay, repeat);
        } else {
            return Bukkit.getScheduler().runTaskTimer(Scheduler.getPlugin(), task, delay, repeat);
        }
    }

    protected void handleError(Throwable throwable) {
        if (errorHandler != null) {
            try {
                errorHandler.accept(throwable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throwable.printStackTrace();
        }
    }

    protected void handleTimeout() {
        if (timeoutHandler != null) {
            try {
                timeoutHandler.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class ForEachBuilder<E> {
        private final List<E> items;
        private final BaseSchedulerBuilder<?> builder;
        private long itemDelay = 0;

        public ForEachBuilder(List<E> items, BaseSchedulerBuilder<?> builder) {
            this.items = items;
            this.builder = builder;
        }

        public ForEachBuilder<E> delay(long ticks) {
            this.itemDelay = ticks;
            return this;
        }

        public ScheduledTask run(Consumer<E> action) {
            if (items.isEmpty()) {
                return new ScheduledTaskImpl(null);
            }

            return new ForEachTask<>(items, action, builder, itemDelay);
        }
    }

    private static class ForEachTask<E> extends ScheduledTaskImpl {
        private final List<E> items;
        private final Consumer<E> action;
        private final BaseSchedulerBuilder<?> builder;
        private final long itemDelay;
        private int currentIndex = 0;
        private BukkitTask currentTask;

        public ForEachTask(List<E> items, Consumer<E> action, BaseSchedulerBuilder<?> builder, long itemDelay) {
            super(null);
            this.items = items;
            this.action = action;
            this.builder = builder;
            this.itemDelay = itemDelay;
            scheduleNext();
        }

        private void scheduleNext() {
            if (currentIndex >= items.size()) {
                return;
            }

            E item = items.get(currentIndex++);

            if (itemDelay > 0 && currentIndex > 1) {
                currentTask = Bukkit.getScheduler().runTaskLater(Scheduler.getPlugin(), () -> {
                    processItem(item);
                }, itemDelay);
            } else {
                currentTask = builder.scheduleTask(() -> {
                    processItem(item);
                }, false);
            }
        }

        private void processItem(E item) {
            try {
                action.accept(item);
            } catch (Exception e) {
                builder.handleError(e);
            }

            if (currentIndex < items.size()) {
                scheduleNext();
            }
        }

        @Override
        public void cancel() {
            if (currentTask != null && !currentTask.isCancelled()) {
                currentTask.cancel();
            }
        }

        @Override
        public boolean isCancelled() {
            return currentTask == null || currentTask.isCancelled();
        }

        @Override
        public BukkitTask getBukkitTask() {
            return currentTask;
        }
    }
}