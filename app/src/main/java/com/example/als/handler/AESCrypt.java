package com.example.als.handler;

import android.util.Base64;

import com.example.als.object.Variable;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESCrypt {

    //encrypt method
    public static String encrypt(String value) throws Exception
    {
        //create a key
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(Variable.ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte [] encryptedByteValue = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(encryptedByteValue, Base64.DEFAULT);
    }

    //decrypt method
    public static String decrypt(String value) throws Exception
    {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(Variable.ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedValue64 = Base64.decode(value, Base64.DEFAULT);
        byte [] decryptedByteValue = cipher.doFinal(decryptedValue64);
        return new String(decryptedByteValue, StandardCharsets.UTF_8);

    }

    //static method
    private static Key generateKey() throws Exception
    {
        return new SecretKeySpec(Variable.encryptionKey, Variable.ALGORITHM);
    }
}
