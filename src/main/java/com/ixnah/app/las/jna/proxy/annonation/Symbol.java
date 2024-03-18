package com.ixnah.app.las.jna.proxy.annonation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Symbol {
    String value();
    boolean allowObject() default false;
}
