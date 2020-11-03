package com.github.fehu.s4nbdtt.native_image_substitutions;

import java.lang.reflect.Field;

/** Taken from [[https://github.com/plokhotnyuk/jsoniter-scala/commit/e089f06c2d8b4bdb87a6874e17bf716e8608b117]]. */
class UnsafeUtils {
    static final sun.misc.Unsafe UNSAFE;
    static {
        try {
            Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            UNSAFE = (sun.misc.Unsafe) field.get(null);
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }
}
