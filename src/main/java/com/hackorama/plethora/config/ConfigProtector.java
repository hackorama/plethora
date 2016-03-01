package com.hackorama.plethora.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEParameterSpec;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.Util;

/**
 * 
 * Protects selected properties, based on recommendations from OWASP
 * https://www.owasp.org/index.php/How_to_encrypt_a_properties_file
 * 
 * Uses split key from more than one locations.
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public class ConfigProtector implements Protector {

    private static final String CHARSET = "UTF-8";
    private static final String TAG = "<::ANTNA::>";
    private static final byte[] SALT = { (byte) 0xc3, (byte) 0x31, (byte) 0x2a, (byte) 0x42, (byte) 0x7a, (byte) 0xc5,
            (byte) 0xef, (byte) 0x23 };
    private static final int ITERATIONS = 32;
    private static final String PARTIAL_KEY = "x7EC(P!,t$2pki0[[P|2cY1cLAi-A'jg!e=c$=c.:;Pvx=h`Zr!7dYAFf{$$EKC";
    private final Cipher encrypter;
    private final Cipher decrypter;
    private final PBEParameterSpec paramSpec;
    private String key;

    public ConfigProtector() throws ConfigProtectException {
        key = PARTIAL_KEY;
        paramSpec = new javax.crypto.spec.PBEParameterSpec(SALT, ITERATIONS);
        try {
            encrypter = Cipher.getInstance("PBEWithMD5AndDES/CBC/PKCS5Padding");
            decrypter = Cipher.getInstance("PBEWithMD5AndDES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException e) {
            throw new ConfigProtectException("initialising ciphers", e);
        } catch (NoSuchPaddingException e) {
            throw new ConfigProtectException("initialising ciphers", e);
        }
    }

    public ConfigProtector(File file) throws ConfigProtectException, IOException {
        this();
        setKey(file);
    }

    public ConfigProtector(char[] key) throws ConfigProtectException {
        this();
        setKey(new String(key));
    }

    public ConfigProtector(String key) throws ConfigProtectException {
        this();
        setKey(key);
    }

    private String checksum(File file) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            int readBytes = 0;
            byte[] dataBytes = new byte[1024];
            while ((readBytes = stream.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, readBytes);
            }
        } finally {
            Util.close(stream, Log.getLogger());
        }
        byte[] mdBytes = md.digest();
        return seal(new String(mdBytes, Util.getEncoding()));
    }

    private boolean checkTag(String value) {
        return value.startsWith(TAG) && value.endsWith(TAG);
    }

    private SecretKey genSecret(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        return secretKeyFactory.generateSecret(new javax.crypto.spec.PBEKeySpec(key.toCharArray()));
    }

    @Override
    public boolean isProtected(String value) throws ConfigProtectException {
        String unsealedValue;
        try {
            unsealedValue = unSeal(value);
        } catch (UnsupportedEncodingException e) {
            throw new ConfigProtectException("checking, decoding", e);
        } catch (IOException e) {
            throw new ConfigProtectException("checking, decoding", e);
        }
        return checkTag(unsealedValue);
    }

    @Override
    public String plainValue(String value) throws ConfigProtectKeyException, ConfigProtectException {
        String unsealedValue = null;
        try {
            unsealedValue = unSeal(value);
        } catch (UnsupportedEncodingException e) {
            throw new ConfigProtectException("reading, decoding", e);
        } catch (IOException e) {
            throw new ConfigProtectException("reading, decoding", e);
        }
        String untaggedValue = unTag(unsealedValue);
        String plainValue = null;
        try {
            plainValue = unProtect(untaggedValue);
        } catch (InvalidKeyException e) {
            throw new ConfigProtectException("reading, decrypting", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new ConfigProtectException("reading, decrypting", e);
        } catch (NoSuchAlgorithmException e) {
            throw new ConfigProtectException("reading, decrypting", e);
        } catch (InvalidKeySpecException e) {
            throw new ConfigProtectException("reading, decrypting", e);
        } catch (IllegalBlockSizeException e) {
            throw new ConfigProtectException("reading, decrypting", e);
        } catch (BadPaddingException e) {
            throw new ConfigProtectException("reading, decrypting", e);
        } catch (IOException e) {
            throw new ConfigProtectException("reading, decrypting", e);
        }
        if (checkTag(plainValue)) {
            untaggedValue = unTag(plainValue);
            return untaggedValue;
        } else {
            throw new ConfigProtectKeyException();
        }
    }

    private String protect(String value) throws InvalidKeyException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException, IllegalBlockSizeException,
            BadPaddingException {
        return protect(value, key);
    }

    private String protect(String value, String key) throws InvalidKeyException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException, IllegalBlockSizeException,
            BadPaddingException {
        encrypter.init(Cipher.ENCRYPT_MODE, genSecret(key), paramSpec);
        return javax.xml.bind.DatatypeConverter.printBase64Binary(encrypter.doFinal(value.getBytes(CHARSET)));

    }

    @Override
    public String protectedValue(String value) throws ConfigProtectException {
        String taggedValue = tag(value);
        String protectedValue = null;
        try {
            protectedValue = protect(taggedValue);
        } catch (InvalidKeyException e) {
            throw new ConfigProtectException("writing, encrypting", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new ConfigProtectException("writing, encrypting", e);
        } catch (NoSuchAlgorithmException e) {
            throw new ConfigProtectException("writing, encrypting", e);
        } catch (InvalidKeySpecException e) {
            throw new ConfigProtectException("writing, encrypting", e);
        } catch (UnsupportedEncodingException e) {
            throw new ConfigProtectException("writing, encrypting", e);
        } catch (IllegalBlockSizeException e) {
            throw new ConfigProtectException("writing, encrypting", e);
        } catch (BadPaddingException e) {
            throw new ConfigProtectException("writing, encrypting", e);
        }
        taggedValue = tag(protectedValue);
        String sealedValue = null;
        try {
            sealedValue = seal(taggedValue);
        } catch (UnsupportedEncodingException e) {
            throw new ConfigProtectException("writing, encoding", e);
        }
        return sealedValue;
    }

    private String seal(String value) throws UnsupportedEncodingException {
        return javax.xml.bind.DatatypeConverter.printBase64Binary(value.getBytes(CHARSET));
    }

    @Override
    public final boolean setKey(File file) throws ConfigProtectException, IOException {
        try {
            key = checksum(file) + PARTIAL_KEY;
            return true;
        } catch (NoSuchAlgorithmException e) {
            throw new ConfigProtectException("initialising key from file", e);
        }
    }

    @Override
    public final void setKey(String key) {
        this.key = key + PARTIAL_KEY;
    }

    private String tag(String value) {
        return TAG + value + TAG;
    }

    private String unProtect(String value) throws InvalidKeyException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException,
            IOException {
        return unProtect(value, key);
    }

    private String unProtect(String value, String key) throws InvalidKeyException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, InvalidKeySpecException, IOException, IllegalBlockSizeException,
            BadPaddingException {
        decrypter.init(Cipher.DECRYPT_MODE, genSecret(key), paramSpec);
        return new String(decrypter.doFinal(javax.xml.bind.DatatypeConverter.parseBase64Binary(value)), CHARSET);
    }

    private String unSeal(String value) throws UnsupportedEncodingException, IOException {
        return new String(javax.xml.bind.DatatypeConverter.parseBase64Binary(value), CHARSET);
    }

    private String unTag(String value) {
        if (checkTag(value)) {
            return value.substring(TAG.length(), value.length() - TAG.length());
        }
        return value;
    }

}
