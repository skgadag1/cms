package com.tbscg.doosan.magnolia.dxp.testing.setup;

import java.util.ArrayList;
import java.util.List;

public class PatternConfigs {

    private List<String> patterns;

    public PatternConfigs() {
        this.patterns = new ArrayList<>();
    }

    public PatternConfigs(List<String> patterns) {
        this.patterns = patterns;
    }

    public List<String> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<String> patterns) {
        this.patterns = patterns;
    }

    public void addPattern(String pattern){
        if (this.patterns != null && pattern != null) {
            this.patterns.add(pattern);
        }
    }
}
