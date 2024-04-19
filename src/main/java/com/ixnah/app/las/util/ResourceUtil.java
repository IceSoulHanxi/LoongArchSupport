package com.ixnah.app.las.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ResourceUtil {

    private ResourceUtil() {
        throw new UnsupportedOperationException();
    }

    public static void extractNative(String osArch, Path nativeDir, String nativeFileName, String nativeMd5, PosixFilePermission... permissions) {
        Path nativePath = nativeDir.resolve(nativeFileName);
        try {
            if (!Files.isRegularFile(nativePath) || !HashUtil.md5Equals(nativePath, nativeMd5)) {
                try (InputStream stream = ResourceUtil.class.getResourceAsStream("/linux/" + osArch + "/" + nativeFileName)) {
                    Files.createDirectories(nativeDir);
                    Files.copy(Objects.requireNonNull(stream, "Can't found " + nativeFileName + " form classpath"), nativePath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            if (permissions != null && permissions.length > 0) {
                Set<PosixFilePermission> currentPerms = Files.getPosixFilePermissions(nativePath);
                Set<PosixFilePermission> needPerms = new HashSet<>(Arrays.asList(permissions));
                if (!currentPerms.containsAll(needPerms)) {
                    needPerms.addAll(currentPerms);
                    Files.setPosixFilePermissions(nativePath, needPerms);
                }
            }
        } catch (IOException e) {
            LogUtil.e(e);
        }
    }

    public static Path extractFile(Path extractFilePath, String dataFilePath, String md5) {
        if (!Files.isRegularFile(extractFilePath) || !HashUtil.md5Equals(extractFilePath, md5)) {
            try (InputStream stream = ResourceUtil.class.getResourceAsStream(dataFilePath)) {
                Files.createDirectories(extractFilePath.getParent());
                Files.copy(Objects.requireNonNull(stream, "Can't found " + dataFilePath + " form classpath"), extractFilePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LogUtil.e(e);
            }
        }
        return extractFilePath;
    }
}
