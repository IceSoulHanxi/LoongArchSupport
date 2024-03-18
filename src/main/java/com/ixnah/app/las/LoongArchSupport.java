package com.ixnah.app.las;

import com.ixnah.app.las.clangd.ClangdSupport;
import com.ixnah.app.las.fsnotifier.FileWatcherSupport;
import com.ixnah.app.las.gateway.GatewaySupport;
import com.ixnah.app.las.jna.JnaSupport;
import com.ixnah.app.las.profiler.AsyncProfilerSupport;
import com.ixnah.app.las.pty4j.Pty4jSupport;
import com.ixnah.app.las.skiko.SkikoSupport;
import com.ixnah.app.las.sqlite.SqliteSupport;
import com.ixnah.app.las.transform.TransformSupport;
import com.ixnah.app.las.util.LogUtil;
import com.ixnah.app.las.util.ThrowUtil;

import static java.util.Arrays.asList;

public class LoongArchSupport {

    public LoongArchSupport() throws Throwable {
        LogUtil.i("start");
        TransformSupport.load();
        GatewaySupport.load();
        loadLoongArchSupport();
    }

    static void loadLoongArchSupport() {
        String osArch = System.getProperty("os.arch");
        if (!"loongarch64".equals(osArch)) return;
        for (Class<?> c : asList(FileWatcherSupport.class, JnaSupport.class, ClangdSupport.class,
                AsyncProfilerSupport.class, Pty4jSupport.class, SkikoSupport.class, SqliteSupport.class)) {
            ThrowUtil.runPrinting(() -> c.getMethod("load", String.class).invoke(null, osArch));
        }
    }
}
