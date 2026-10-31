package com.tbscg.doosan.magnolia.dxp.core.util;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.NoSuchComponentException;
import lombok.extern.slf4j.Slf4j;

/**
 * DXP utility class.
 *
 * @author Alejandro Puig Escobar
 */
@Slf4j
public class DxpUtil {

    /**
     * Provides with guice component instance by class name.
     *
     * @param className class name of type T.
     * @param <T> component instance type to return.
     * @return component instance registered on guice, <code>null</code> if no instance exists.
     */
    public static <T> T getComponent(String className) {
        try {
            return (T) Components.getComponent(Class.forName(className));
        } catch (ClassNotFoundException cnfe) {
            log.error("Unable to locate class {}", className, cnfe);
        } catch (NoSuchComponentException nce) {
            //stack trace is already logged
        }
        return null;
    }

    /**
     * Checks whether running instance is author or not.
     *
     * @return if running instance is author.
     */
    public static boolean isAuthor() {
        //ServerConfiguration will always exist in guice context as its part of core module
        return Components.getComponent(ServerConfiguration.class).isAdmin();
    }

}
