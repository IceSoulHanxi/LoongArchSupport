package com.ixnah.app.las.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class HashUtil {

    private HashUtil() {
        throw new UnsupportedOperationException();
    }

    public static String getSha1(Path path) {
        return getHash(path, "SHA1");
    }

    public static boolean sha1Equals(Path path, String sha1) {
        return sha1 == null || sha1.isEmpty() || sha1.equalsIgnoreCase(getSha1(path));
    }

    public static String getMd5(Path path) {
        return getHash(path, "MD5");
    }

    public static boolean md5Equals(Path path, String md5) {
        return md5 == null || md5.isEmpty() || md5.equalsIgnoreCase(getMd5(path));
    }

    public static String getHash(Path path, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            try (DigestInputStream stream = new DigestInputStream(Files.newInputStream(path), md)) {
                byte[] buffer = new byte[8192];
                while (true) {
                    if (stream.read(buffer) <= 0) break;
                }
            }
            return toHex(md.digest());
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toHex(byte[] bytes) {
        return HexFormat.of().formatHex(bytes);
    }
}
