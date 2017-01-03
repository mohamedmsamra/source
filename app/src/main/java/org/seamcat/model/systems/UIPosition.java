package org.seamcat.model.systems;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UIPosition {

    int row();
    int col();
    int height() default 0;
    int width() default 0;
    String name();
}
