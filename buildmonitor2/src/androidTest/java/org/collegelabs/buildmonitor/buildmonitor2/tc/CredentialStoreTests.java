package org.collegelabs.buildmonitor.buildmonitor2.tc;

import android.test.AndroidTestCase;
import junit.framework.Assert;

/**
 */
public class CredentialStoreTests extends AndroidTestCase {

    private CredentialStore _service;

    @Override
    protected void setUp() throws Exception {
        _service = new CredentialStore();
    }

    public void testEncryptionDecryption() throws Exception {
        String plaintext = "foo.bar";
        String encrypted = _service.encrypt(plaintext);
        Assert.assertNotSame(plaintext, encrypted);

        String decrypted = _service.decrypt(encrypted);
        Assert.assertEquals(plaintext, decrypted);
    }
}
