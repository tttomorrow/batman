package org.opengauss.batman.modules.utils;

import java.io.File;

public class PathUtils {

    public static String buildJobPath(String basePath, long jobId) {
        StringBuilder sb = new StringBuilder();
        sb.append(basePath).append(File.separator)
            .append(jobId).append(File.separator);
        return sb.toString();
    }
}
