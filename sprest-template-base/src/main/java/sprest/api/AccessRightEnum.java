package sprest.api;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to mark user right enums contributed by each module
 * @author wulf
 *
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface AccessRightEnum {

}
