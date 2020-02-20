package main

import (
	"fmt"
	"sync"
	"testing"
	"time"
)
import "github.com/stretchr/testify/assert"

func Test_RaceCondition(t *testing.T) {
	// Given
	repository := deviceRepositorySynchTestAdapter{make(map[string]Device), &sync.Mutex{}}
	handler := alertHandler{repository}
	messages := make(chan int)
	// When
	go func() {
		handler.onNetworkInterfaceAlert("device", "other device")
		messages <- 1
	}()
	go func() {
		handler.onNetworkInterfaceAlert("device", "other device")
		messages <- 2
	}()
	for i := 0; i < 2; i++ {
		fmt.Println(<-messages)
	}
	assert.Equal(t, 2, repository.findDevice("device").alertsCount)
	assert.Equal(t, 2, repository.findDevice("other device").alertsCount)
}

func Test_Deadlock(t *testing.T) {
	// Given
	repository := deviceRepositorySynchTestAdapter{make(map[string]Device), &sync.Mutex{}}
	handler := alertHandler{repository}
	messages := make(chan int)
	// When
	go func() {
		for i := 0; i < 10; i++ {
			handler.onNetworkInterfaceAlert("device", "other device")
		}
		messages <- 1
	}()
	go func() {
		for i := 0; i < 10; i++ {
			handler.onNetworkInterfaceAlert("other device", "device")
		}
		messages <- 2
	}()
	for i := 0; i < 2; i++ {
		fmt.Println(<-messages)
	}
}

type deviceRepositorySynchTestAdapter struct {
	devices map[string]Device
	mutex *sync.Mutex
}

func (r deviceRepositorySynchTestAdapter) findDevice(deviceName string) Device {
	r.mutex.Lock()
	device := Device{r.devices[deviceName].name, r.devices[deviceName].alertsCount, r.devices[deviceName].mostFailingNeighbour}
	time.Sleep(time.Millisecond * 30)
	r.mutex.Unlock()
	return device
}

func (r deviceRepositorySynchTestAdapter) updateAlertsCount(deviceName string, alertsCount int) {
	r.mutex.Lock()
	device := r.devices[deviceName]
	device.alertsCount = alertsCount
	r.devices[deviceName] = device
	r.mutex.Unlock()
}

func (r deviceRepositorySynchTestAdapter) updateMostFailingNeighbour(deviceName string, neighbour Device) {
	r.mutex.Lock()
	device := r.devices[deviceName]
	device.mostFailingNeighbour = &neighbour
	r.devices[deviceName] = device
	r.mutex.Unlock()
}
