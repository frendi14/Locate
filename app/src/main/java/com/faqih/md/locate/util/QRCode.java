package com.faqih.md.locate.util;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Created by Faqih on 9/4/2016.
 */
public class QRCode {
    private static int bitmapSize = 150;

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
}
