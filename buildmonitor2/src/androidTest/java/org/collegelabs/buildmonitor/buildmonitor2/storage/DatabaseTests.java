package org.collegelabs.buildmonitor.buildmonitor2.storage;

import android.test.AndroidTestCase;

import junit.framework.Assert;

import org.collegelabs.buildmonitor.buildmonitor2.tc.Credentials;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;

/**
 */
public class DatabaseTests extends AndroidTestCase {

    private Database _service;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        _service = new Database(getContext(), true);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        _service.Db.close();
    }

    public void testNoCredentials() throws Exception {

        List<Credentials> creds = _service.GetAllCredentials()
                .timeout(1, TimeUnit.SECONDS)
                .toBlocking()
                .first();

        Assert.assertEquals(0, creds.size());
    }


    public void testHasCredentials() throws Exception {
        final String username = "user1";
        final String serverUrl = "serverUrl1";
        final String password = "password1";

        GivenHasSavedCredentials(username, serverUrl, password);

        List<Credentials> list = _service.GetAllCredentials()
                .timeout(1, TimeUnit.SECONDS)
                .toBlocking()
                .first();

        Assert.assertNotNull(list);
        Assert.assertEquals(1, list.size());

        Credentials creds = list.get(0);
        Assert.assertEquals(username, creds.username);
        Assert.assertEquals(serverUrl, creds.server);
        Assert.assertEquals(password, creds.password);
    }

    private void GivenHasSavedCredentials(String username, String serverUrl, String password) {
        _service.InsertCredentials(username, serverUrl, password, false);
    }
}
