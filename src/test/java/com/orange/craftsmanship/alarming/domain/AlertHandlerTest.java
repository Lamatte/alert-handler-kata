package com.orange.craftsmanship.alarming.domain;

import org.junit.Before;
import org.junit.Test;

import static java.util.Optional.of;
import static org.mockito.Mockito.*;

public class AlertHandlerTest {

    private DeviceRepositoryPort alarmRepository = mock(DeviceRepositoryPort.class);
    private AlertHandler handler;

    @Before
    public void setUp() {
        handler = new AlertHandler(alarmRepository);
    }

    @Test
    public void shall_update_alerts_count_for_unknown_devices() {
        // When
        handler.onNetworkInterfaceAlert("first device", "second device");
        // Then
        verify(alarmRepository).updateAlertsCount("first device", 1);
        verify(alarmRepository).updateAlertsCount("second device", 1);
    }

    @Test
    public void shall_update_alerts_count_for_known_devices() {
        // Given
        when(alarmRepository.findDevice("device")).thenReturn(of(createDevice("device", 3)));
        when(alarmRepository.findDevice("other device")).thenReturn(of(createDevice("other device", 10)));
        // When
        handler.onNetworkInterfaceAlert("device", "other device");
        // Then
        verify(alarmRepository).updateAlertsCount("device", 4);
        verify(alarmRepository).updateAlertsCount("other device", 11);
    }

    @Test
    public void shall_update_most_failing_neighbour() {
        // Given
        Device device = createDevice("device", 3);
        when(alarmRepository.findDevice("device")).thenReturn(of(device));
        Device otherDevice = createDevice("other device", 10);
        when(alarmRepository.findDevice("other device")).thenReturn(of(otherDevice));
        // When
        handler.onNetworkInterfaceAlert("device", "other device");
        // Then
        verify(alarmRepository).updateMostFailingNeighbour("device", otherDevice);
        verify(alarmRepository).updateMostFailingNeighbour("other device", device);
    }

    @Test
    public void shall_not_update_most_failing_neighbour() {
        // Given
        Device device = createDevice("device", 3);
        device.mostFailingNeighbour = of(createDevice("third device", 20));
        when(alarmRepository.findDevice("device")).thenReturn(of(device));
        when(alarmRepository.findDevice("other device")).thenReturn(of(createDevice("other device", 10)));
        // When
        handler.onNetworkInterfaceAlert("device", "other device");
        // Then
        verify(alarmRepository, times(0)).updateMostFailingNeighbour(eq("device"), any());
    }

    private Device createDevice(String name, int alertsCount) {
        Device device = new Device(name);
        device.alertsCount = alertsCount;
        return device;
    }
}
