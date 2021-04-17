package cn.enaium.inject.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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
     * <b>return</b> or <b>invoke</b> ordinal
     *
     * @return ordinal
     */
    int ordinal() default -1;

    /**
     * @return invoke target
     */
    String target() default "";

    enum Type {
        /**
         * Method begin
         */
        HEAD,
        /**
         * Method end
         */
        TAIL,
        /**
         * Method return
         */
        RETURN,
        /**
         * Method invoke
         */
        INVOKE,
        /**
         * Overwrite method
         */
        OVERWRITE
    }
}
