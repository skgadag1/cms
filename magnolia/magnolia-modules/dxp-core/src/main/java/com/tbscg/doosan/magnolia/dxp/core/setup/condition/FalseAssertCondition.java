package com.tbscg.doosan.magnolia.dxp.core.setup.condition;

import info.magnolia.module.InstallContext;

/**
 *  * Condition {@link info.magnolia.module.delta.Condition} that checks
 *  * whether the two provided instances are not equal.
 *
 * @param <T> instance class type to check
 */
public class FalseAssertCondition<T> extends AssertCondition<T> {

    public FalseAssertCondition(String name, String description, T fromValue, T toAssertValue) {
        super(name, description, fromValue, toAssertValue);
    }

    @Override
    public boolean check(InstallContext installContext) {
        return !super.check(installContext);
    }
}
