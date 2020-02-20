package com.orange.craftsmanship.alarming.domain;

import java.util.Optional;

public interface DeviceRepositoryPort {
    /**
     * Get information for one device.
     *
     * @param deviceName the device name we are looking for
     * @return the alarm information, if any
     */
    Optional<Device> findDevice(String deviceName);

    /**
     * Updates devices's alerts count.
     *
     * @param deviceName  the device to be updated
     * @param alertsCount the number of alerts impacting this device
     */
    void updateAlertsCount(String deviceName, int alertsCount);

    /**
     * Updates devices's most failing neighbour.
     *
     * @param deviceName the device to be updated
     * @param neighbour  the most failing neighbour device
     */
    void updateMostFailingNeighbour(String deviceName, Device neighbour);
}
