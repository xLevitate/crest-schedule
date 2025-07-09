# Crest Schedule

A powerful and intuitive task scheduling library for Bukkit/Paper plugins with fluent API design.

## Overview

Crest Schedule provides a modern, chainable API for scheduling synchronous and asynchronous tasks in Minecraft plugins. It supports delays, repeating tasks, timeouts, error handling, task groups, and advanced chaining operations.

## Usage

### Basic Setup

```java
import com.moocrest.scheduler.Scheduler;

// Initialize the scheduler in your plugin's onEnable()
Scheduler.initialize(this);
```

### Synchronous Tasks

```java
import com.moocrest.scheduler.Scheduler;
import com.moocrest.scheduler.ScheduledTask;

// Simple sync task
Scheduler.sync().run(() -> {
    // Runs on main thread immediately
    getLogger().info("Hello from sync task!");
});

// Delayed sync task
Scheduler.sync()
    .delay(20) // Wait 1 second (20 ticks)
    .run(() -> {
        getLogger().info("Delayed sync task!");
    });

// Repeating sync task
Scheduler.sync()
    .delay(20)    // Initial delay
    .repeat(40)   // Repeat every 2 seconds
    .times(5)     // Run 5 times total
    .run(() -> {
        getLogger().info("Repeating sync task!");
    });

// Conditional sync tasks
Scheduler.sync().runIf(() -> getServer().getOnlinePlayers().size() > 0, () -> {
    getLogger().info("Players are online!");
});

Scheduler.sync()
    .repeat(20)
    .runWhile(() -> getServer().getOnlinePlayers().size() > 0, () -> {
        getLogger().info("Still have players online!");
    });

// Task chaining
Scheduler.sync()
    .run(() -> getLogger().info("First task"))
    .thenRun(() -> getLogger().info("Second task"))
    .thenAsync(() -> {
        // Switch to async
        return "async result";
    });
```

### Asynchronous Tasks

```java
// Simple async task
Scheduler.async().run(() -> {
    // Runs on async thread
    // Safe for database operations, file I/O, etc.
    performDatabaseOperation();
});

// Async task with result
CompletableFuture<String> future = Scheduler.async().supply(() -> {
    return fetchDataFromAPI();
});

// Async to sync chaining
Scheduler.async()
    .storeResult(() -> {
        // Async operation
        return fetchPlayerData();
    })
    .thenSync(result -> {
        // Back to main thread with result
        updatePlayerDisplay(result);
    });

// Complex async chaining
Scheduler.async()
    .delay(10)
    .timeout(100) // Cancel if takes longer than 5 seconds
    .onTimeout(() -> getLogger().warning("Task timed out!"))
    .onError(throwable -> getLogger().severe("Task failed: " + throwable.getMessage()))
    .storeResult(() -> {
        return performLongRunningOperation();
    })
    .thenSync(result -> {
        // Process result on main thread
        handleResult(result);
    });
```

### Error Handling and Timeouts

```java
// Error handling
Scheduler.async()
    .onError(throwable -> {
        getLogger().severe("Task failed: " + throwable.getMessage());
        throwable.printStackTrace();
    })
    .run(() -> {
        // Risky operation
        riskyOperation();
    });

// Timeout handling
Scheduler.async()
    .timeout(100) // 5 seconds
    .onTimeout(() -> {
        getLogger().warning("Operation timed out!");
    })
    .run(() -> {
        // Long running operation
        longRunningOperation();
    });

// Combined error and timeout handling
Scheduler.sync()
    .delay(20)
    .timeout(200)
    .onError(throwable -> handleError(throwable))
    .onTimeout(() -> handleTimeout())
    .run(() -> {
        performOperation();
    });
```

### ForEach Operations

```java
import java.util.Arrays;
import java.util.List;

// Process list items with delay between each
List<Player> players = new ArrayList<>(getServer().getOnlinePlayers());

Scheduler.sync()
    .forEach(players)
    .delay(10) // 0.5 second delay between each player
    .run(player -> {
        player.sendMessage("Hello " + player.getName() + "!");
    });

// Async forEach for heavy operations
List<String> playerNames = Arrays.asList("player1", "player2", "player3");

Scheduler.async()
    .forEach(playerNames)
    .delay(20) // 1 second delay between each
    .run(playerName -> {
        // Heavy operation for each player
        processPlayerData(playerName);
    });
```

### Task Groups

```java
import com.moocrest.scheduler.group.TaskGroup;

// Create a task group
TaskGroup group = Scheduler.group("my-tasks");
// or
TaskGroup group = Scheduler.createTaskGroup(); // Creates "default" group

// Add tasks to the group
group.sync()
    .delay(20)
    .repeat(40)
    .run(() -> {
        getLogger().info("Group task 1");
    });

group.async()
    .run(() -> {
        getLogger().info("Group task 2");
    });

// Cancel all tasks in the group
group.cancelAll();

// Check group status
int activeTasks = group.getActiveTaskCount();
boolean hasActiveTasks = group.hasActiveTasks();

// Get group name
String groupName = group.getName();
```

### Task Management

```java
// Get task reference for manual control
ScheduledTask task = Scheduler.sync()
    .delay(100)
    .repeat(20)
    .run(() -> {
        getLogger().info("Repeating task");
    });

// Check task status
boolean isRunning = !task.isCancelled();

// Cancel specific task
task.cancel();

// Store multiple tasks
List<ScheduledTask> tasks = new ArrayList<>();
tasks.add(Scheduler.sync().delay(20).run(() -> task1()));
tasks.add(Scheduler.async().run(() -> task2()));

// Cancel all stored tasks
tasks.forEach(ScheduledTask::cancel);
```

### Advanced Examples

```java
// Complex workflow: async data fetch -> sync processing -> async save
Scheduler.async()
    .storeResult(() -> {
        // Fetch data from external API
        return apiClient.fetchUserData(userId);
    })
    .thenSync(userData -> {
        // Process on main thread (access Bukkit API)
        Player player = getServer().getPlayer(userData.getName());
        if (player != null) {
            player.sendMessage("Welcome back, " + userData.getDisplayName() + "!");
            return userData;
        }
        return null;
    })
    .thenAsync(processedData -> {
        if (processedData != null) {
            // Save to database
            database.saveUserData(processedData);
        }
    });

// Batch processing with error handling
List<UUID> playerUUIDs = getPlayerUUIDs();

Scheduler.async()
    .onError(throwable -> {
        getLogger().severe("Batch processing failed: " + throwable.getMessage());
    })
    .forEach(playerUUIDs)
    .delay(5) // Small delay between each UUID
    .run(uuid -> {
        try {
            processPlayerData(uuid);
        } catch (Exception e) {
            getLogger().warning("Failed to process player " + uuid + ": " + e.getMessage());
        }
    });

// Conditional repeating task with automatic cleanup
Scheduler.sync()
    .repeat(20) // Every second
    .runUntil(
        () -> getServer().getOnlinePlayers().isEmpty(), // Stop condition
        () -> {
            // Task to run until condition is met
            broadcastMessage("Server has players online!");
        }
    );
```