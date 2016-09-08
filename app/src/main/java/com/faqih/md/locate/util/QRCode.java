package com.faqih.md.locate.util;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Faqih on 9/4/2016.
 */
public class QRCode {
    private static int bitmapSize = 200;

    public static Bitmap getQRCode (String contain) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix matrix = qrCodeWriter.encode(contain, BarcodeFormat.QR_CODE, bitmapSize, bitmapSize);
        Bitmap bmp = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.RGB_565);
            for (int x = 0; x < bitmapSize; x++){
                for (int y = 0; y < bitmapSize; y++){
                    bmp.setPixel(x, y, matrix.get(x,y) ? Color.BLACK : Color.WHITE);
                }
            }
        return bmp;
    }

    public static void saveQRtoPNG(String filePath, Bitmap bitmap) throws IOException {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, arrayOutputStream);

        FileOutputStream outputStream = new FileOutputStream(filePath);
        arrayOutputStream.writeTo(outputStream);
        arrayOutputStream.flush();
        arrayOutputStream.flush();
        arrayOutputStream.close();
        outputStream.close();
    }
}
