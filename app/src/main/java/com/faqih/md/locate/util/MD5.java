package com.faqih.md.locate.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Faqih on 9/4/2016.
 */
public class MD5 {
    public static String generateMD5(String contain) throws NoSuchAlgorithmException {
        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
        digest.update(contain.getBytes());
        byte messageDigest[] = digest.digest();

        StringBuilder hexString = new StringBuilder();
        for (byte aMessageDigest : messageDigest){
            hexString.append(Integer.toHexString(0xFF & aMessageDigest));
        }
        return hexString.toString();
    }
}
