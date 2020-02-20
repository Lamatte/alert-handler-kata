package com.orange.craftsmanship.alarming.domain;

public interface NetworkSupervisionPort {
    /**
     * Supervision agents on devices check connectivity between them, and
     * may raise alerts when a network interface link with another device seems to be down.
     * This callback will be called whenever this occurs.
     *
     * @param sourceDeviceName the device on which the agent is running
     * @param targetDeviceName the device that the agent failed to reach
     */
    void onNetworkInterfaceAlert(String sourceDeviceName, String targetDeviceName);
}
