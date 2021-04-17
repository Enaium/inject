package cn.enaium.inject.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Enaium
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Inject {
    /**
     * @return target class
     *
     * such as <b>Target.class</b>
     */
    Class<?> value() default Inject.class;

    /**
     * @return target class name
     * <p>
     * such as <b>cn.enaium.inject.Target</b>
     */
    String target() default "";
}
