package main

type Device struct {
    name                 string
    alertsCount          int
    mostFailingNeighbour *Device
}

type deviceRepositoryPort interface {
    /**
     * Get information for one Device.
     *
     * @param deviceName the Device name we are looking for
     * @return the alarm information, if any, or an empty Device struct (how to deal with optional results properly?? Using nil??)
     */
    findDevice(deviceName string) Device

    /**
     * Updates devices's alerts count.
     *
     * @param deviceName  the Device to be updated
     * @param alertsCount the number of alerts impacting this Device
     */
    updateAlertsCount(deviceName string, alertsCount int)

    /**
     * Updates devices's most failing neighbour.
     *
     * @param deviceName the Device to be updated
     * @param neighbour  the most failing neighbour Device
     */
    updateMostFailingNeighbour(deviceName string, neighbour Device)
}
