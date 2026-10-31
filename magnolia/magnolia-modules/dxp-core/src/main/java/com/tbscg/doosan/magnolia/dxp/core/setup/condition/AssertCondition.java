package com.tbscg.doosan.magnolia.dxp.core.setup.condition;

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractCondition;

/**
 * Condition {@link info.magnolia.module.delta.Condition} that checks
 * whether the two provided instances are equal.
 *
 * @param <T> instance class type to check
 */
public class AssertCondition<T> extends AbstractCondition {

    private final T fromValue;
    private final T toAssertValue;

    public AssertCondition(String name, String description, T fromValue, T toAssertValue) {
        super(name, description);
        this.fromValue = fromValue;
        this.toAssertValue = toAssertValue;
    }

    @Override
    public boolean check(InstallContext installContext) {
        return fromValue.equals(toAssertValue);
    }
}
