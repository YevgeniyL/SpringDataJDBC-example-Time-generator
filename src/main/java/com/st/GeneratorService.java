package com.st;

import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class GeneratorService {
    private int reconnectWaitTimeMs;
    private final int batchSize;
    private final int delayToGenerateNewTimestampMs;
    private boolean debugMode;
    private volatile List<GeneratedDataEntity> list = Collections.synchronizedList(new LinkedList<>());
    private ExecutorService savingExecutorService = Executors.newSingleThreadExecutor();
    private volatile Future<?> stateOfSaveTask = null;
    @Autowired
    private GeneratedDataRepository dataRepository;

    public GeneratorService(int reconnectTimeout, int saveBatchSize, int delayToGenerateNewTimestampMs) {
        this.reconnectWaitTimeMs = reconnectTimeout;
        this.batchSize = saveBatchSize;
        this.delayToGenerateNewTimestampMs = delayToGenerateNewTimestampMs;
    }

    public void run(String[] args) {
        List<String> strings = args != null ? Arrays.asList(args) : new ArrayList<>();
        if (strings.contains("-debug"))
            debugMode = true;

        if (strings.contains("-p")) {
            new TaskToPrintDbData().run();
        } else {
            new Timer().scheduleAtFixedRate(taskToGenerateNewTimestamp, 0, delayToGenerateNewTimestampMs);
        }
    }

    private class TaskToPrintDbData extends TimerTask {
        @Override
        public void run() {
            try {
                if (debugMode)
                    System.out.println("Start load database data");
                List<GeneratedDataEntity> items = dataRepository.findAll();
                items.forEach(item -> System.out.println(item.getCreated()));
                if (debugMode)
                    System.out.println("Founded count: " + items.size());
            } catch (Exception e) {
                System.err.println("Connection failed. Retry to print DB data after " + reconnectWaitTimeMs / 1000 + " s");
                new Timer().schedule(new TaskToPrintDbData(), reconnectWaitTimeMs);
            }
        }
    }

    private TimerTask taskToGenerateNewTimestamp = new TimerTask() {
        @Override
        public void run() {
            LocalDateTime now = LocalDateTime.now();
            GeneratedDataEntity testEntity = new GeneratedDataEntity(now);
            list.add(testEntity);
            System.out.println("Generated time: " + now.toString() + ". Queue size: " + list.size());

            if (stateOfSaveTask == null || stateOfSaveTask.isDone()) {
                if (debugMode) System.out.println("State of saving task = done");
                stateOfSaveTask = savingExecutorService.submit(() -> saveEntityList());
            } else {
                if (debugMode) System.out.println("State of saving task = busy.");
            }
        }
    };

    private void saveEntityList() {
        if (!list.isEmpty()) {
            if (debugMode)
                System.out.println("List items  " + list.stream().map(GeneratedDataEntity::getCreated).collect(Collectors.toList()).toString());

            if (list.size() <= batchSize) {
                LinkedList<GeneratedDataEntity> toSaveList = new LinkedList<>(list);
                saveListBySingleItem(toSaveList);
            } else {
                System.err.println("Warning! Connection is too slow or not stable. Items count to save more than batchSize = " + batchSize);
                if (debugMode)
                    System.out.println("Save " + batchSize + " of " + list.size() + " elements:" + list.stream().map(GeneratedDataEntity::getCreated).collect(Collectors.toList()).toString());

                List<GeneratedDataEntity> toSaveList = list.stream().limit(batchSize).collect(Collectors.toList());
                saveListInBatch(toSaveList);
            }
        }
    }

    private void saveListInBatch(List<GeneratedDataEntity> toSaveList) {
        try {
            dataRepository.saveAll(toSaveList);
            list.removeAll(toSaveList);
        } catch (Exception e) {
            System.err.println("Connection exception. Waiting " + reconnectWaitTimeMs / 1000 + "s.");
            sleep(reconnectWaitTimeMs);
        } finally {
            saveEntityList();
        }
    }

    private void saveListBySingleItem(final List<GeneratedDataEntity> toSaveList) {
        if (debugMode)
            System.out.println("Try save " + toSaveList.stream().map(GeneratedDataEntity::getCreated).collect(Collectors.toList()).toString());

        for (GeneratedDataEntity testEntity : toSaveList) {
            try {
                dataRepository.save(testEntity);
                list.remove(testEntity);
                System.out.println("Saved " + testEntity.getCreated());
            } catch (Exception e) {
                System.err.println("Connection exception. Waiting " + reconnectWaitTimeMs / 1000 + "s.");
                sleep(reconnectWaitTimeMs);
                saveEntityList();
            }
        }
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }
}
