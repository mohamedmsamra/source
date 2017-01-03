package org.seamcat.model.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Vertical {
    /* If used on functions it means the function will be in (Degree, dBm)
     * in range [-90 - 90]
     */
}
