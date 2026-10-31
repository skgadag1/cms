package com.tbscg.doosan.magnolia.dealernet.field.clonableNamed;

/**
 * Needs to be extended by field definition in order to be handled by TagSwitchableFormView
 */
public interface CloneableNamedField<T extends CloneableNamedField> extends Cloneable {
    void setName(String name);
    T clone();
}
