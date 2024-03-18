package com.ixnah.app.las.fsnotifier;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.vfs.impl.local.NativeFileWatcherImpl;
import com.intellij.openapi.vfs.local.PluggableFileWatcher;
import com.ixnah.app.las.transform.TransformSupport;
import com.ixnah.app.las.util.LogUtil;
import com.ixnah.app.las.util.ResourceUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;

import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;

public class FileWatcherSupport {

    private FileWatcherSupport() {
        throw new UnsupportedOperationException();
    }

    public static void load(String osArch) {
        Path parent = Path.of(PathManager.getPluginsPath(), "linux/" + osArch);
        String fileWatcherName = "fsnotifier";
        Path fileWatcherPath = parent.resolve(fileWatcherName);
        LogUtil.d("fileWatcherPath: " + fileWatcherPath);
        ResourceUtil.extractNative(osArch, parent, fileWatcherName, "4a36ae3dfcb69643ea9af4fc575b31ac", OWNER_EXECUTE, OWNER_READ);
        System.setProperty("idea.filewatcher.executable.path", fileWatcherPath.toString());
        TransformSupport.getTransformPipeHandler().add(new FileWatcherTransformer());
        reloadNativeFileWatcher();
    }

    private static void reloadNativeFileWatcher() {
        Method startupProcess = null;
        for (PluggableFileWatcher watcher : PluggableFileWatcher.EP_NAME.getIterable()) {
            if (watcher instanceof NativeFileWatcherImpl w && w.isOperational()) {
                LogUtil.d(watcher + " process restarting!");
                try {
                    if (startupProcess == null) {
                        startupProcess = watcher.getClass().getDeclaredMethod("startupProcess", boolean.class);
                        startupProcess.setAccessible(true);
                    }
                    startupProcess.invoke(watcher, true);
                    LogUtil.d(watcher + " process restarted!");
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    LogUtil.e(e);
                }
            }
        }
    }
}
