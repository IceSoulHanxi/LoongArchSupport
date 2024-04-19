package com.ixnah.app.las.gateway;

import com.ixnah.app.las.transform.TransformSupport;

public class GatewaySupport {

    private GatewaySupport() {
        throw new UnsupportedOperationException();
    }

    public static void load() {
        TransformSupport.getTransformPipeHandler().add(new FixArchTransformer());
    }
}
