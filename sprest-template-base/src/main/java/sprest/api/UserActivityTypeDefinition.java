package sprest.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used to mark user activity definitions for notifications
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface UserActivityTypeDefinition {
}
