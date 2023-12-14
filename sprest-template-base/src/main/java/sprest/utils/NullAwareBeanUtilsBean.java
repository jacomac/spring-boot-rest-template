package sprest.utils;

import org.apache.commons.beanutils.BeanUtilsBean;

import java.lang.reflect.InvocationTargetException;

/**
 * BeanUtils able to copy only not null properties of an object
 */
public class NullAwareBeanUtilsBean extends BeanUtilsBean {

    @Override
    public void copyProperty(Object dest, String name, Object value)
        throws IllegalAccessException, InvocationTargetException
    {
        if (value == null){
            return;
        }
        super.copyProperty(dest, name, value);
    }
}
