package main

type alertHandler struct {
	repository deviceRepositoryPort
}

func (h alertHandler) onNetworkInterfaceAlert(sourceDeviceName string, targetDeviceName string) {
	sourceDevice := h.getDevice(sourceDeviceName)
	targetDevice := h.getDevice(targetDeviceName)
	sourceDevice.alertsCount += 1
	targetDevice.alertsCount += 1
	h.updateDevice(sourceDeviceName, sourceDevice, targetDevice)
	h.updateDevice(targetDeviceName, targetDevice, sourceDevice)
}

func (h alertHandler) getDevice(deviceName string) Device {
	device := h.repository.findDevice(deviceName)
	// Don't know how to deal with 'Optional' results...
	if device.name == "" {
		device.name = deviceName
	}
	return device
}

func (h alertHandler) updateDevice(deviceName string, device Device, neighbour Device) {
	h.repository.updateAlertsCount(deviceName, device.alertsCount)
	if device.mostFailingNeighbour == nil || device.alertsCount < neighbour.alertsCount {
		h.repository.updateMostFailingNeighbour(deviceName, neighbour)
	}
}
