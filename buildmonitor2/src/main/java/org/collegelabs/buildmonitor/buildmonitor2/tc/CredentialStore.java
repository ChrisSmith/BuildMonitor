package org.collegelabs.buildmonitor.buildmonitor2.tc;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import timber.log.Timber;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.CertificateException;

/**
 */
public class CredentialStore {

    private static final String KEY_ALIAS = "tc";
    private static final String cipherSuite = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    public String encrypt(String input) throws CredentialException {
        byte[] encrypted = Encrypt(input.getBytes());
        return new String(Base64.encode(encrypted, Base64.DEFAULT), Charset.defaultCharset());
    }

    public String decrypt(String input) throws CredentialException {
        byte[] decoded = Base64.decode(input, Base64.DEFAULT);
        byte[] decrypted = Decrypt(decoded);
        return new String(decrypted, Charset.defaultCharset());
    }

    public byte[] Decrypt(byte[] blob) throws CredentialException {

        KeyPair key = getKeyPair();

        try {
            Cipher cipher = Cipher.getInstance(cipherSuite);
            cipher.init(Cipher.DECRYPT_MODE, key.getPrivate());
            return cipher.doFinal(blob);

        } catch (NoSuchPaddingException
                | NoSuchAlgorithmException
                | BadPaddingException
                | IllegalBlockSizeException
                | InvalidKeyException
                e
                ) {
            throw new CredentialException("Failed to decrypt data", e);
        }
    }

    public byte[] Encrypt(byte[] blob) throws CredentialException {

        KeyPair key = getKeyPair();

        try {
            Cipher cipher = Cipher.getInstance(cipherSuite);
            cipher.init(Cipher.ENCRYPT_MODE, key.getPublic());
            return cipher.doFinal(blob);

        } catch (NoSuchPaddingException
                | NoSuchAlgorithmException
                | BadPaddingException
                | IllegalBlockSizeException
                | InvalidKeyException
                e
                ) {
            throw new CredentialException("Failed to encrypt data", e);
        }
    }

    private KeyPair getKeyPair() throws CredentialException {

        KeyStore ks = getKeyStore();

        try {
            if(!ks.containsAlias(KEY_ALIAS)){
                generateKeypair();
            }
        } catch (KeyStoreException e) {
            throw new CredentialException("Failed to check for alias, assuming it exists", e);
        }

        KeyStore.Entry entry = null;
        try {
            entry = ks.getEntry(KEY_ALIAS, null);

        } catch (KeyStoreException | NoSuchAlgorithmException e) {
            throw new CredentialException("Failed to retrieve key", e);

        } catch (UnrecoverableEntryException e) {
            Timber.e("Unrecoverable key. Generating new one", e);
            generateKeypair();

            try {
                entry = ks.getEntry(KEY_ALIAS, null);
            } catch (KeyStoreException | UnrecoverableEntryException | NoSuchAlgorithmException e1) {
                throw new CredentialException("Failed to get newly generated key");
            }
        }

        if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
            throw new CredentialException("Key is not a private key");
        }

        KeyStore.PrivateKeyEntry privateKeyEntry = ((KeyStore.PrivateKeyEntry) entry);

        return new KeyPair(privateKeyEntry.getCertificate().getPublicKey(), privateKeyEntry.getPrivateKey()) ;
    }

    private void generateKeypair() throws CredentialException {

        try {
            KeyStore ks = getKeyStore();

            if(ks.containsAlias(KEY_ALIAS)){
                Timber.i("Key exists, deleting from keystore");
                ks.deleteEntry(KEY_ALIAS);
            }
        } catch (KeyStoreException e) {
            throw new CredentialException("Failed to remove existing key for alias", e);
        }

        KeyPairGenerator kpg = null;

        try {
            kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new CredentialException("Failed to get android keystore", e);
        }

        try {
            kpg.initialize(new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                    .setUserAuthenticationRequired(false)
                    .build());

            KeyPair keyPair = kpg.generateKeyPair();

        } catch (InvalidAlgorithmParameterException e) {
            throw new CredentialException("Failed create keypair", e);
        }
    }

    private KeyStore getKeyStore() throws CredentialException {
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            throw new CredentialException("Failed to get keystore instance");
        }

        try {
            ks.load(null);
            return ks;
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new CredentialException("Unable to load keystore", e);
        }
    }

}
