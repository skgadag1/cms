package com.tbscg.doosan.magnolia.dxp.core.registry;

import java.util.Collection;

/**
 * {@link DefinitionRegistrar} defines an API for registering/unregistering definitions.
 *
 * @param <T> object type to register.
 *
 * @author Alejandro Puig Escobar
 */
public interface DefinitionRegistrar<T> {

    /**
     * Registers a bean.
     *
     * @param bean bean to register.
     */
    void register(T bean);

    /**
     * Unregisters a bean.
     *
     * @param bean bean to unregister.
     */
    void  unregister(T bean);

    /**
     * Registers a collection.
     *
     * @param beanCollection bean collection to register.
     */
    default void registerCollection(Collection<T> beanCollection) {
        beanCollection.forEach(this::register);
    }

    /**
     * Unregisters a collection.
     *
     * @param beanCollection bean collection to unregister.
     */
    default void unregisterCollection(Collection<T> beanCollection) {
        beanCollection.forEach(this::unregister);
    }

}
