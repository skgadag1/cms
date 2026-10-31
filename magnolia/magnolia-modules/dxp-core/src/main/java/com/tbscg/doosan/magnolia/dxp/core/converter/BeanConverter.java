package com.tbscg.doosan.magnolia.dxp.core.converter;

/**
 * {@link BeanConverter} converts an object type into another.
 *
 * @param <F> object type to convert from.
 * @param <T> object type to convert to.
 *
 * @author Alejandro Puig Escobar
 */
public interface BeanConverter<F,T> {

    /**
     * Converts a bean of type F into a bean of type T.
     *
     * @param fromBean bean of type F.
     * @return bean of type T.
     */
    T toBean(F fromBean) throws BeanConversionException;

}
