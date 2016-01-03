package org.collegelabs.buildmonitor.buildmonitor2.tc;

import org.collegelabs.buildmonitor.buildmonitor2.builds.BuildStatus;

public class TcUtil {
    public static BuildStatus getBuildStatus(String status){
        return status.equalsIgnoreCase("SUCCESS")
                ? BuildStatus.Success
                : BuildStatus.Failure;
    }
}
