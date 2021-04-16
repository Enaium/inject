package cn.enaium.inject.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Enaium
 */
@Retention(RetentionPolicy.CLASS)
public @interface At {
    /**
     * {@link Type}
     *
     * @return type
     */
    Type type();

    /**
     * return/invoke ordinal
     *
     * @return ordinal
     */
    int ordinal() default -1;

    /**
     * @return invoke target
     */
    String target() default "";

    enum Type {
        HEAD, //Method begin
        TAIL,//Method end
        RETURN,//Method return
        INVOKE,//Method invoke
        OVERWRITE//Overwrite method
    }
}
