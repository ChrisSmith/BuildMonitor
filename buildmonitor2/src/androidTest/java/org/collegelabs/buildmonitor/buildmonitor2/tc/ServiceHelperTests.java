package org.collegelabs.buildmonitor.buildmonitor2.tc;

import android.test.AndroidTestCase;
import junit.framework.Assert;

/**
 */
public class ServiceHelperTests extends AndroidTestCase {

    public void testGetEndpoint_Port() throws Exception {
        Credentials credentials = new Credentials(-1, "https://teamcity.jetbrains.com:9000");
        Assert.assertEquals("https://teamcity.jetbrains.com:9000/guestAuth/app/rest", ServiceHelper.getEndpoint(credentials, true));
    }

    public void testGetEndpoint_Pathprefix() throws Exception {
        Credentials credentials = new Credentials(-1, "https://teamcity.jetbrains.com/someprefix/");
        Assert.assertEquals("https://teamcity.jetbrains.com/someprefix/guestAuth/app/rest", ServiceHelper.getEndpoint(credentials, true));
    }

    public void testGetEndpoint_GuestMode() throws Exception {
        Credentials credentials = new Credentials(-1, "https://teamcity.jetbrains.com");
        Assert.assertEquals("https://teamcity.jetbrains.com/guestAuth/app/rest", ServiceHelper.getEndpoint(credentials, true));
    }

    public void testGetEndpoint_User() throws Exception {
        Credentials credentials = new Credentials(-1, "username", "pass", "https://teamcity.jetbrains.com");
        Assert.assertEquals("https://teamcity.jetbrains.com/app/rest", ServiceHelper.getEndpoint(credentials, true));
    }
}
