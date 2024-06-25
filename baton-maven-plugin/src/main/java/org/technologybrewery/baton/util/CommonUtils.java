package org.technologybrewery.baton.util;
import com.vdurmont.semver4j.Semver;

public class CommonUtils {
    /**
     * Checks if version1 is less than version2 using the Semver4j library.
     *
     * @param version1
     * @param version2
     * @return isLessThanVersion - if true, version1 is less than version2.
     */
    public static boolean isLessThanVersion(String version1, String version2) {
        Semver sem = new Semver(version1);
        return sem.isLowerThan(version2);
    }
}
