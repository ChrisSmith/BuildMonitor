package org.collegelabs.buildmonitor.buildmonitor2.buildhistory;

import org.collegelabs.buildmonitor.buildmonitor2.builds.BuildStatus;

public interface OnChartSelectedListener {
    void onValueSelected(String date, BuildStatus buildStatus);
    void selectionCleared();
}
