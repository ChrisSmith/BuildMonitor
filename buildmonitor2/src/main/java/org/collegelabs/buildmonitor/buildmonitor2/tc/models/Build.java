package org.collegelabs.buildmonitor.buildmonitor2.tc.models;

import java.util.Date;

/**
 */
public class Build {

    public int id, percentageComplete;
    public boolean running;
    public String number, status, buildTypeId, href, webUrl;
    public Date startDate;

}

