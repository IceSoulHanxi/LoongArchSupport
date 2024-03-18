package com.ixnah.app.las.skiko;

public class SkikoSupport {

    private SkikoSupport() {
        throw new UnsupportedOperationException();
    }

    public static void load(String osArch) {
         String skikoLibraryPathStr = System.getProperty("skiko.library.path");
         // TODO: Skiko 适配
    }
}
