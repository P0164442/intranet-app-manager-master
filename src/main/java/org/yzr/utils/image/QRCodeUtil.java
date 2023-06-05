package org.yzr.utils.image;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class QRCodeUtil {

    private String text;

    private Integer width = 200;

    private Integer height = 200;

    private static final float PERCENT = 0.2F;

    private static final int CORNER_RADIUS = 5;

    private File icon;

    private String format = "png";

    public static QRCodeUtil encode(String text) {
        QRCodeUtil util = new QRCodeUtil();
        util.text = text;
        return util;
    }

    public QRCodeUtil withSize(Integer width, Integer height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public QRCodeUtil withIcon(File icon) {
        this.icon = icon;
        return this;
    }

    public boolean writeTo(OutputStream outputStream) {
        try {
            BitMatrix bitMatrix = getBitMatrix();
            if (this.icon != null) {
                MatrixToImageConfig matrixToImageConfig = new MatrixToImageConfig(0xFF000001, 0xFFFFFFFF);
                BufferedImage bufferedImage = drawIconInMatrix(MatrixToImageWriter.toBufferedImage(bitMatrix,matrixToImageConfig), this.icon);
                ImageIO.write(bufferedImage, format, outputStream);
            } else {
                MatrixToImageWriter.writeToStream(bitMatrix, format, outputStream);
            }
            return true;
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean writeTo(File file) {
        try {
            BitMatrix bitMatrix = getBitMatrix();
            if (this.icon != null) {
                MatrixToImageConfig matrixToImageConfig = new MatrixToImageConfig(0xFF000001, 0xFFFFFFFF);
                BufferedImage bufferedImage = drawIconInMatrix(MatrixToImageWriter.toBufferedImage(bitMatrix,matrixToImageConfig), this.icon);
                ImageIO.write(bufferedImage, format, file);
            } else {
                MatrixToImageWriter.writeToPath(bitMatrix, format, file.toPath());
            }
            return true;
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private BitMatrix getBitMatrix() throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        //
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        //
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        // ，
        hints.put(EncodeHintType.MARGIN, 1);
        return new MultiFormatWriter().encode(this.text, BarcodeFormat.QR_CODE, width, height, hints);
    }


    /**
     *
     * @param file
     * @return
     */
    public static String parseCode(File file)  {
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parseCode(bufferedImage);
    }

    /**
     *
     * @param bufferedImage
     * @return
     */
    public static String parseCode(BufferedImage bufferedImage)  {
        String content = null;
        try {
            //
            MultiFormatReader formatReader = new MultiFormatReader();
            BinaryBitmap binaryBitmap= new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(bufferedImage)));
            //
            Map hints= new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            Result result = formatReader.decode(binaryBitmap, hints);

            content = result.getText();
            if (bufferedImage != null) {
                bufferedImage.flush();
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     *
     * @param inputStream
     * @return
     */
    public static String parseCode(InputStream inputStream) {
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parseCode(bufferedImage);
    }

    /**
     *
     * @param matrixImage
     * @param logoFile
     * @return
     */
    private BufferedImage drawIconInMatrix(BufferedImage matrixImage, File logoFile) throws IOException{

        Graphics2D g2 = matrixImage.createGraphics();
        int matrixWidth = matrixImage.getWidth();
        int matrixHeight = matrixImage.getHeight();


        BufferedImage logo = ImageIO.read(logoFile);

        //
        g2.drawImage(logo,(int)(matrixWidth * PERCENT*2),(int)(matrixHeight * PERCENT*2), (int)(matrixWidth * PERCENT), (int)(matrixHeight * PERCENT), null);//绘制
        BasicStroke stroke = new BasicStroke(5,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
        g2.setStroke(stroke);//
        //
        RoundRectangle2D.Float round = new RoundRectangle2D.Float(matrixWidth * PERCENT*2, matrixHeight * PERCENT*2, matrixWidth * PERCENT, matrixHeight * PERCENT,CORNER_RADIUS,CORNER_RADIUS);
        g2.setColor(Color.white);
        g2.draw(round);//

        //
        BasicStroke stroke2 = new BasicStroke(1,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
        g2.setStroke(stroke2);//
        RoundRectangle2D.Float round2 = new RoundRectangle2D.Float(matrixWidth * PERCENT*2+2, matrixHeight * PERCENT*2+2, matrixWidth * PERCENT-4, matrixHeight * PERCENT-4,CORNER_RADIUS,CORNER_RADIUS);
        g2.setColor(new Color(128,128,128));
        g2.draw(round2);//

        g2.dispose();
        matrixImage.flush() ;
        return matrixImage ;
    }
}

