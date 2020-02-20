package com.orange.craftsmanship.alarming.domain;

import java.util.Optional;

public class Device {
    private String name;
    int alertsCount = 0;
    Optional<Device> mostFailingNeighbour;

    public Device(String name) {
        this.name = name;
        mostFailingNeighbour = Optional.empty();
    }

    public String name() {
        return name;
    }
}
