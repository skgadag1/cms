package com.tbscg.doosan.magnolia.bobcat.rest.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class CompatibleMachines {

    private final List<Machine> machines;

    public CompatibleMachines(List<Machine> machines) {
        this.machines = machines == null ? Collections.emptyList() : machines;
    }

    @Getter
    @AllArgsConstructor
    public static class Machine {
        private final String name;
    }
}
