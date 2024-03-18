package com.ixnah.app.las.jna.proxy;

import com.ixnah.app.las.jna.proxy.annonation.Symbol;

public interface InternalJvm extends Wrapper {

    @Symbol(value = "_ZN10JNIHandles22resolve_external_guardEP8_jobject", allowObject = true)
    Pointer resolveExternalGuard(Object jobject);

    @Symbol("_ZN21java_lang_ClassLoader27non_reflection_class_loaderEP7oopDesc")
    Pointer nonReflectionClassLoader(Pointer oop);

    @Symbol("_ZN21java_lang_ClassLoader19loader_data_acquireEP7oopDesc")
    Pointer loaderDataAcquire(Pointer oop);

    @Symbol("_ZN7oopDesc14load_klass_rawEPS_")
    Pointer loadKlassRaw(Pointer oop);

    @Symbol("_ZNK9HashtableIP5KlassL8MEMFLAGS1EE12compute_hashEPK6Symbol")
    int classHashtableComputeHash(Pointer dictionary, Pointer symbol);

    @Symbol("_ZNK9HashtableIP6SymbolL8MEMFLAGS1EE9index_forEPKS0_")
    int classHashtableIndexFor(Pointer dictionary, Pointer symbol);

    @Symbol("_ZN10Dictionary9get_entryEijP6Symbol")
    Pointer getDictionaryEntry(Pointer dictionary, int index, int hash, Pointer symbol);

    @Symbol("_ZN10Dictionary10free_entryEP15DictionaryEntry")
    void freeDictionaryEntry(Pointer dictionary, Pointer dictionaryEntry);

    @Symbol("_ZN13InstanceKlass12unload_classEPS_")
    void unloadClass(Pointer instanceKlass);

    @Symbol("_ZN13InstanceKlass32clean_initialization_error_tableEv")
    void cleanInitErrorTable();

    @Symbol("_ZN13InstanceKlass14set_init_stateENS_10ClassStateE")
    void setInitState(Pointer instanceKlass, int state);
}
