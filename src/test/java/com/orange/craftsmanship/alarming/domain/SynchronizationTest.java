package com.orange.craftsmanship.alarming.domain;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.stubbing.Answer;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class SynchronizationTest {

    private DeviceRepositoryPort mock;

    @Before
    public void setUp() throws Exception {
        mock = mock(DeviceRepositoryPort.class);
    }

    /*
     * Two options here:
     * - either check internal sequencing of calls (implemented hereafter), which allows to test
     *   using a mock, but exposes internal behavior and remains fragile,
     * - or test that the end result is correct, which requires implementing a test repository adapter,
     *   but allows this test to use handler as a real black-box.
     */
    @Test
    public void shall_have_no_race_conditions() throws InterruptedException {
        // Given
        AlertHandler handler = new AlertHandler(mock);
        // With a little help of my friends...
        // The bigger the delay, the more likely the race condition will happen...
        when(mock.findDevice(anyString())).thenAnswer(emptyWithDelay(50));
        List<Thread> workers = asList(
                createWorker(handler, "device", "other device", 1),
                createWorker(handler, "device", "other device", 1)
        );
        // When
        start(workers);
        join(workers, 500);
        // Then
        InOrder readWriteSequence = inOrder(mock);
        readWriteSequence.verify(mock).findDevice("device");
        readWriteSequence.verify(mock).findDevice("other device");
        readWriteSequence.verify(mock).updateAlertsCount(eq("device"), anyInt());
        readWriteSequence.verify(mock).updateAlertsCount(eq("other device"), anyInt());
        readWriteSequence.verify(mock).findDevice("device");
        readWriteSequence.verify(mock).findDevice("other device");
        readWriteSequence.verify(mock).updateAlertsCount(eq("device"), anyInt());
        readWriteSequence.verify(mock).updateAlertsCount(eq("other device"), anyInt());
    }

    /*
     * This test is trickier than the previous one.
     * We cannot use delays in mocks to make sure a deadlock will occur, as it depends on the locking code.
     * We should need to mock the locking system, which we dont want as this is precisely what we want to test.
     * Can's see a better solution than brute force, over-soliciting the system hoping to have a good chance
     * of deadlock occurrence...
     * We need to find a proper balance between execution time and deadlock probability.
     */
    @Test
    public void shall_have_no_deadlocks() throws InterruptedException {
        // Given
        DeviceRepositoryPort repositoryPort = mock(DeviceRepositoryPort.class);
        AlertHandler handler = new AlertHandler(repositoryPort);
        List<Thread> workers = asList(
                createWorker(handler, "device", "other device", 1000),
                createWorker(handler, "other device", "device", 1000),
                createWorker(handler, "device", "other device", 1000),
                createWorker(handler, "other device", "device", 1000)
        );
        // When
        start(workers);
        join(workers, 500);
        // Then
        if (someAreAlive(workers)) {
            fail("Deadlock detected: threads are still alive");
        }
    }

    private Answer emptyWithDelay(int delay) {
        return invocationOnMock -> {
            Thread.sleep(delay);
            return empty();
        };
    }

    private Thread createWorker(AlertHandler handler, String firstDeviceName, String secondDeviceName, int iterations) {
        return new Thread(() -> {
            for (int i = 0; i < iterations; i++) {
                handler.onNetworkInterfaceAlert(firstDeviceName, secondDeviceName);
            }
        });
    }

    private void start(List<Thread> workers) {
        workers.forEach(Thread::start);
    }

    private void join(List<Thread> workers, long timeout) throws InterruptedException {
        for (Thread thread : workers) {
            thread.join(timeout);
        }
    }

    private boolean someAreAlive(List<Thread> workers) {
        return 0 != workers.stream().filter(Thread::isAlive).count();
    }

}
