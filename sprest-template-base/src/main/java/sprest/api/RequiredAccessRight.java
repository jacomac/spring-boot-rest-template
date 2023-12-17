package sprest.api;

import sprest.user.AllAccessRights;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * annotation to restrict access to a controller
 * to users that have one of the set of values
 * contained in {@link AllAccessRights#getValues()}
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiredAccessRight {
    String[] value();
}