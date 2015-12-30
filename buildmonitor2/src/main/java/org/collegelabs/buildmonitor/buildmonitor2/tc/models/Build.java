package org.collegelabs.buildmonitor.buildmonitor2.tc.models;

import java.util.Date;

/**
 */
public class Build {

    public int id;
    public int percentageComplete; // removed in ??
    public boolean running; // removed in ??
    public String number;
    public String status;
    public String buildTypeId;
    public String href;
    public String webUrl;
    public Date startDate; // removed in 8.1

}

