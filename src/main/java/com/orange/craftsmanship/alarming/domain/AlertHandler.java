package com.orange.craftsmanship.alarming.domain;

public class AlertHandler implements NetworkSupervisionPort {

    private final DeviceRepositoryPort deviceRepositoryPort;

    public AlertHandler(DeviceRepositoryPort deviceRepositoryPort) {
        this.deviceRepositoryPort = deviceRepositoryPort;
    }

    @Override
    public void onNetworkInterfaceAlert(String sourceDeviceName, String targetDeviceName) {
        Device sourceDevice = deviceRepositoryPort.findDevice(sourceDeviceName).orElse(new Device(sourceDeviceName));
        sourceDevice.alertsCount += 1;
        Device targetDevice = deviceRepositoryPort.findDevice(targetDeviceName).orElse(new Device(targetDeviceName));
        targetDevice.alertsCount += 1;
        updateDevice(sourceDevice, targetDevice);
        updateDevice(targetDevice, sourceDevice);
    }

    private void updateDevice(Device device, Device otherDevice) {
        deviceRepositoryPort.updateAlertsCount(device.name(), device.alertsCount);
        if (!device.mostFailingNeighbour.isPresent() || device.mostFailingNeighbour.get().alertsCount < otherDevice.alertsCount) {
            deviceRepositoryPort.updateMostFailingNeighbour(device.name(), otherDevice);
        }
    }
}
