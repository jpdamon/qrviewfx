package com.jeffdamon.qrviewfx;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/** Scans image files for QR codes and decodes them **/
public class QRCodeScanner {
    private HashMap<DecodeHintType, Object> hintMapSpeed = new HashMap<>();
    private HashMap<DecodeHintType, Object> hintMapAccuracy = new HashMap<>();
    private static final List<BarcodeFormat> formats = Collections.singletonList(BarcodeFormat.QR_CODE);

    public QRCodeScanner() {
        hintMapSpeed.put(DecodeHintType.POSSIBLE_FORMATS, formats);
        hintMapAccuracy.put(DecodeHintType.POSSIBLE_FORMATS, formats);
        hintMapAccuracy.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
    }

    /**
     * Decode QR code in an image
     *
     * @param filepath     path to image file
     * @param moreAccurate try harder to find a qr code in image, at expense of speed
     * @return decoded text from QR code
     * @throws NotFoundException if no QR code found in file
     * @throws IOException       if error reading image file
     */
    public String decode(String filepath, boolean moreAccurate) throws NotFoundException, IOException {
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                new BufferedImageLuminanceSource(
                        ImageIO.read(new FileInputStream(filepath)))));
        Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap,
                moreAccurate ? hintMapAccuracy : hintMapSpeed);
        return qrCodeResult.getText();
    }
}
