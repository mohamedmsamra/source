package org.seamcat.model.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Config {

    String name() default "";

    String unit() default "";

    // used by string selector
    String values() default "";

    String defineGroup() default "";
    String group() default "";
    String invertedGroup() default "";

    int order();

    String information() default "";

    String rangeUnit() default "";

    String toolTip() default "";

    boolean embed() default false;

    boolean downLink() default true;
}
