package main

import "testing"
import "github.com/stretchr/testify/assert"

func Test_UnknownDevices(t *testing.T) {
	// Given
	repository := deviceRepositoryTestAdapter{make(map[string]Device)}
	handler := alertHandler{repository}
	// When
	handler.onNetworkInterfaceAlert("device", "other device")
	assert.Equal(t, 1, repository.findDevice("device").alertsCount)
	assert.Equal(t, 1, repository.findDevice("other device").alertsCount)
}

func Test_KnownDevices(t *testing.T) {
	// Given
	repository := deviceRepositoryTestAdapter{make(map[string]Device)}
	repository.devices["device"] = Device{alertsCount:22, name:"device"}
	handler := alertHandler{repository}
	// When
	handler.onNetworkInterfaceAlert("device", "other device")
	assert.Equal(t, 23, repository.findDevice("device").alertsCount)
}

func Test_UpdateNeighbour(t *testing.T) {
	// Given
	repository := deviceRepositoryTestAdapter{make(map[string]Device)}
	repository.devices["device"] = Device{alertsCount:22, name:"device"}
	handler := alertHandler{repository}
	// When
	handler.onNetworkInterfaceAlert("device", "other device")
	assert.Equal(t, "other device", repository.findDevice("device").mostFailingNeighbour.name)
	assert.Equal(t, 1, repository.findDevice("device").mostFailingNeighbour.alertsCount)
	assert.Equal(t, "device", repository.findDevice("other device").mostFailingNeighbour.name)
	assert.Equal(t, 23, repository.findDevice("other device").mostFailingNeighbour.alertsCount)
}

func Test_NotUpdateUpdateNeighbour(t *testing.T) {
	// Given
	repository := deviceRepositoryTestAdapter{make(map[string]Device)}
	thirdDevice := Device{alertsCount: 10, name: "third device"}
	repository.devices["third device"] = thirdDevice
	repository.devices["device"] = Device{alertsCount:22, name:"device", mostFailingNeighbour:&thirdDevice}
	handler := alertHandler{repository}
	// When
	handler.onNetworkInterfaceAlert("device", "other device")
	assert.Equal(t, "third device", repository.findDevice("device").mostFailingNeighbour.name)
	assert.Equal(t, 10, repository.findDevice("device").mostFailingNeighbour.alertsCount)
}

type deviceRepositoryTestAdapter struct {
	devices map[string]Device
}

func (r deviceRepositoryTestAdapter) findDevice(deviceName string) Device {
	return r.devices[deviceName]
}

func (r deviceRepositoryTestAdapter) updateAlertsCount(deviceName string, alertsCount int) {
	device := r.devices[deviceName]
	device.alertsCount = alertsCount
	r.devices[deviceName] = device
}

func (r deviceRepositoryTestAdapter) updateMostFailingNeighbour(deviceName string, neighbour Device) {
	device := r.devices[deviceName]
	device.mostFailingNeighbour = &neighbour
	r.devices[deviceName] = device
}
