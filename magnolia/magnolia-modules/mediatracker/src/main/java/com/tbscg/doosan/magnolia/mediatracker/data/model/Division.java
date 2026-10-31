package com.tbscg.doosan.magnolia.mediatracker.data.model;

import java.util.ArrayList;
import java.util.List;

public enum Division {

    GLOBAL_BOBCAT,
    GLOBAL_PORTABLE_POWER;

    public static Division find(String divisionName) {
        try {
            return valueOf(divisionName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static Division getDefault() {
        return GLOBAL_BOBCAT;
    }

    public static Division[] fromList(String[] divisions) {
        List<Division> results = new ArrayList<>();
        for(String d : divisions) {
            results.add(valueOf(d));
        }
        return results.toArray(new Division[0]);
    }

}
