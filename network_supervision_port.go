package main

type networkSupervisionPort interface {
    /**
     * Supervision agents on devices check connectivity between them, and
     * may raise alerts when a network interface link with another Device seems to be down.
     * This callback will be called whenever this occurs.
     *
     * @param sourceDeviceName the Device on which the agent is running
     * @param targetDeviceName the Device that the agent failed to reach
     */
    onNetworkInterfaceAlert(sourceDeviceName string, targetDeviceName string)
}
