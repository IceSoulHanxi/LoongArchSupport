package com.ixnah.app.las.util;

import com.ixnah.app.las.LoongArchSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {

    private LogUtil() {
        throw new UnsupportedOperationException();
    }

    static Logger LOG = LoggerFactory.getLogger(LoongArchSupport.class);

    public static void i(String message) {
        LOG.info(message);
    }

    public static void w(String message) {
        LOG.warn(message);
    }

    public static void e(String message) {
        LOG.error(message);
    }

    public static void e(Throwable e) {
        LOG.error("", e);
    }

    public static void d(String message) {
        LOG.info(message);
    }

    public static Logger getLogger() {
        return LOG;
    }
}
