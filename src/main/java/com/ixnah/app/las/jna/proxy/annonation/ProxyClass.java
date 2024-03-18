package com.ixnah.app.las.jna.proxy.annonation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ProxyClass {
    String value();
}
