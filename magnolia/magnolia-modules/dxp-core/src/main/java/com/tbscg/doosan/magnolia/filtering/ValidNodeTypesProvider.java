package com.tbscg.doosan.magnolia.filtering;

import java.util.Set;

public interface ValidNodeTypesProvider {
    Set<String> getMatchedNodesByText(String searchingText);
}
