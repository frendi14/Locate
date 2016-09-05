package com.faqih.md.locate.util;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

/**
 * Created by Faqih on 9/4/2016.
 */
public class Base64 {
    public static String base64fromBitmap(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);
    }
}
