package com.github.fehu.s4nbdtt.native_image_substitutions;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

/** Taken from [[https://github.com/plokhotnyuk/jsoniter-scala/commit/e089f06c2d8b4bdb87a6874e17bf716e8608b117]]. */
@TargetClass(className = "scala.runtime.Statics")
final class Target_scala_runtime_Statics {
  @Substitute
  public static void releaseFence() {
    UnsafeUtils.UNSAFE.storeFence();
  }
}

@TargetClass(className = "scala.reflect.internal.util.StatisticsStatics")
final class Target_scala_reflect_internal_util_StatisticsStatics {
  @Substitute
  public static boolean areSomeColdStatsEnabled() {
    return false;
  }

  @Substitute
  public static boolean areSomeHotStatsEnabled() {
    return false;
  }
}
