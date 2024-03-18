package com.ixnah.app.las.jna.proxy;

public interface VmStructEntry {

    String getDeclaringType();

    String getName();

    String getType();

    boolean isStatic();

    long getOffset();

    long getAddress();
}
