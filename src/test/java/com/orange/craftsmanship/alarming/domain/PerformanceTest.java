package com.orange.craftsmanship.alarming.domain;

import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class PerformanceTest {

    private final Random random = new Random();
    Map<String, Device> devices = new HashMap<>();

    private DeviceRepositoryPort repositoryPort = new DeviceRepositoryPort() {
        @Override
        public synchronized Optional<Device> findDevice(String deviceName) {
            return devices.containsKey(deviceName) ? of(devices.get(deviceName)) : empty();
        }

        @Override
        public void updateAlertsCount(String deviceName, int alertsCount) {
            Device device = getDevice(deviceName);
            try {
                Thread.sleep(0, 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            device.alertsCount = alertsCount;
            putDevice(deviceName, device);
        }

        @Override
        public void updateMostFailingNeighbour(String deviceName, Device neighbour) {
            Device device = getDevice(deviceName);
            device.mostFailingNeighbour = of(neighbour);
            putDevice(deviceName, device);
        }

        private synchronized Device getDevice(String deviceName) {
            return findDevice(deviceName).orElse(new Device(deviceName));
        }

        private synchronized Device putDevice(String deviceName, Device device) {
            return devices.put(deviceName, device);
        }
    };

    @Test
    public void shall_have_a_proper_throughput() throws InterruptedException {
        // Given
        AlertHandler handler = new AlertHandler(repositoryPort);
        List<WorkerThread> workers = createWorkers(handler);
        // When
        start(workers);
        Thread.sleep(500);
        interrupt(workers);
        join(workers, 50);
        // Then
        assertThat(getHandledAlertsCount(workers)).isGreaterThan(5000);
    }

    private List<WorkerThread> createWorkers(AlertHandler handler) {
        return IntStream.range(1, 30)
                .mapToObj(i -> new WorkerThread(handler))
                .collect(Collectors.toList());
    }

    private int getHandledAlertsCount(List<WorkerThread> workers) {
        int count = workers.stream()
                .map(workerThread -> workerThread.handledAlertsCount)
                .mapToInt(Integer::intValue)
                .sum();
        System.out.println("Workers handled a total of " + count + " alerts");
        return count;
    }

    private class WorkerThread extends Thread {

        private AlertHandler handler;
        private int handledAlertsCount = 0;

        public WorkerThread(AlertHandler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    handler.onNetworkInterfaceAlert(randomDeviceName(), randomDeviceName());
                    handledAlertsCount++;
                }
            } catch (RuntimeException e) {
            }
        }

    }

    private String randomDeviceName() {
        return Integer.toString(random.nextInt(500));
    }

    private void start(List<WorkerThread> workers) {
        workers.forEach(Thread::start);
    }

    private void interrupt(List<WorkerThread> workers) {
        workers.forEach(Thread::interrupt);
    }

    private void join(List<WorkerThread> workers, long timeout) throws InterruptedException {
        for (Thread thread : workers) {
            thread.join(timeout);
        }
    }

}
