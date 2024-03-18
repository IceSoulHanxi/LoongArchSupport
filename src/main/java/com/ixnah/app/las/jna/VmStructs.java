package com.ixnah.app.las.jna;

import com.ixnah.app.las.jna.proxy.NativeLibrary;
import com.ixnah.app.las.jna.proxy.Pointer;
import com.ixnah.app.las.util.UnsafeUtil;

import java.util.Objects;
import java.util.stream.Stream;

public class VmStructs {
    private static long typeNameOffset;
    private static long fieldNameOffset;
    private static long typeStringOffset;
    private static long isStaticOffset;
    private static long offsetOffset;
    private static long addressOffset;
    private static long arrayStride;

    public static Stream<VmStructEntry> getEntryStream(NativeLibrary jvm) {
        if (arrayStride == 0) {
            typeNameOffset = jvm.getGlobalVariableAddress("gHotSpotVMStructEntryTypeNameOffset").getLong(0);
            fieldNameOffset = jvm.getGlobalVariableAddress("gHotSpotVMStructEntryFieldNameOffset").getLong(0);
            typeStringOffset = jvm.getGlobalVariableAddress("gHotSpotVMStructEntryTypeStringOffset").getLong(0);
            isStaticOffset = jvm.getGlobalVariableAddress("gHotSpotVMStructEntryIsStaticOffset").getLong(0);
            offsetOffset = jvm.getGlobalVariableAddress("gHotSpotVMStructEntryOffsetOffset").getLong(0);
            addressOffset = jvm.getGlobalVariableAddress("gHotSpotVMStructEntryAddressOffset").getLong(0);
            arrayStride = jvm.getGlobalVariableAddress("gHotSpotVMStructEntryArrayStride").getLong(0);
        }
        Pointer gHotSpotVMStructs = jvm.getGlobalVariableAddress("gHotSpotVMStructs");
        return Stream.iterate(new VmStructEntry(gHotSpotVMStructs.getPointer(0)), Objects::nonNull, entry -> {
            Pointer nextPtr = entry.entryPtr.share(arrayStride);
            Pointer fieldNameOffsetPtr = nextPtr.getPointer(fieldNameOffset);
            return fieldNameOffsetPtr != null ? new VmStructEntry(nextPtr) : null;
        });
    }

    public static class VmStructEntry {
        private final Pointer entryPtr;

        private VmStructEntry(Pointer entryPtr) {
            this.entryPtr = entryPtr;
        }

        public String getDeclaringType() {
            return readString(entryPtr.getPointer(typeNameOffset));
        }

        public String getName() {
            return readString(entryPtr.getPointer(fieldNameOffset));
        }

        public String getType() {
            Pointer pointer = entryPtr.getPointer(typeStringOffset);
            return pointer != null ? readString(pointer) : null;
        }

        public boolean isStatic() {
            return entryPtr.getInt(isStaticOffset) != 0;
        }

        public long getOffset() {
            return entryPtr.getLong(offsetOffset);
        }

        public long getAddress() {
            return entryPtr.getLong(addressOffset);
        }
    }

    public static String readString(Pointer pointer) {
        long handle = Pointer.nativeValue(pointer), len = 0;
        while (UnsafeUtil.getByte(handle + len) != 0) len++;
        return new String(pointer.getByteArray(0, (int) len));
    }
}
