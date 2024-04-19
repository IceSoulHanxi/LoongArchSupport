package com.ixnah.app.las.pty4j;

import com.ixnah.app.las.transform.TransformSupport;
import com.ixnah.app.las.util.ResourceUtil;

import java.nio.file.Path;

public class Pty4jSupport {

    private Pty4jSupport() {
        throw new UnsupportedOperationException();
    }

    public static void load(String osArch) {
        String pty4jNativeFolderStr = System.getProperty("pty4j.preferred.native.folder");
        Path nativeDir = Path.of(pty4jNativeFolderStr).resolve("linux/" + osArch);
        ResourceUtil.extractNative(osArch, nativeDir, "libpty.so", ""/*TODO: md5*/);
        TransformSupport.getTransformPipeHandler().add(new Pty4jTransformer());
    }
}
