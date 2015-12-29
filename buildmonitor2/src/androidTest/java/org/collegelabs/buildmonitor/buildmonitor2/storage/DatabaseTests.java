package org.collegelabs.buildmonitor.buildmonitor2.storage;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import junit.framework.Assert;
import org.collegelabs.buildmonitor.buildmonitor2.tc.Credentials;
import rx.Observable;

import java.util.concurrent.TimeUnit;

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
        Credentials emptyCreds = new Credentials(-1, null, null, null);

        Credentials creds = _service.GetCredentials()
                .timeout(10, TimeUnit.MILLISECONDS, Observable.just(emptyCreds))
                .toBlocking()
                .single()
                ;

        Assert.assertEquals(emptyCreds, creds);
    }


    public void testHasCredentials() throws Exception {
        final String username = "user1";
        final String serverUrl = "serverUrl1";
        final String password = "password1";

        GivenHasSavedCredentials(username, serverUrl, password);

        Credentials creds = _service.GetCredentials()
                .toBlocking()
                .firstOrDefault(null);

        Assert.assertNotNull(creds);
        Assert.assertEquals(username, creds.username);
        Assert.assertEquals(serverUrl, creds.server);
        Assert.assertEquals(password, creds.password);
    }

    private void GivenHasSavedCredentials(String username, String serverUrl, String password) {
        _service.InsertCredentials(username, serverUrl, password, false);
    }
}
