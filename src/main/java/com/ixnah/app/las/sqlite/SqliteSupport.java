package com.ixnah.app.las.sqlite;

public class SqliteSupport {

    private SqliteSupport() {
        throw new UnsupportedOperationException();
    }

    public static void load(String osArch) {
        // TODO: sqlite适配 https://github.com/JetBrains/intellij-community/blob/164e6d70fdf689d4e30735e1c2b8418c31238183/platform/sqlite/src/org/jetbrains/sqlite/sqliteLibLoader.kt#L34
    }
}
