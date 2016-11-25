/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.duo.ml.autovirt;

import com.jme3.app.state.AbstractAppState;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import com.jme3.util.BufferUtils;
import com.jme3.util.Screenshots;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author duo
 */
public class ScreenCaptureAppState
        extends AbstractAppState
        implements SceneProcessor {

    private Renderer renderer;
    private int width;
    private int height;
    private long millis;
    private ByteBuffer byteBuffer;
    // original image
    private BufferedImage bufferedImage;
    // store gray shades
    private BufferedImage bigGrayBufferedImage;
    // scaled gray shades
    private BufferedImage grayBufferedImage;
    // stringBuilder to store all bytes in Octave format
    private StringBuilder stringBuilderBytes;
    private int dataLinesCount;
    private int dataColumnsCount;
    private File file;
    private float scaleFactor;

    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
        System.out.println("ScreenCaptureAppState initialized!");
        this.renderer = rm.getRenderer();
        millis = System.currentTimeMillis();
        width = vp.getCamera().getWidth();
        height = vp.getCamera().getHeight();
        scaleFactor = 0.025F;
        byteBuffer = BufferUtils.createByteBuffer(width * height * 4);
        // original image
        bufferedImage = new BufferedImage(width, height,
                BufferedImage.TYPE_4BYTE_ABGR);
        // store gray shades
        bigGrayBufferedImage = new BufferedImage(width, height,
                BufferedImage.TYPE_BYTE_GRAY);
        // scaled gray shades
        grayBufferedImage = new BufferedImage((int)(width * scaleFactor), (int)(height * scaleFactor),
                BufferedImage.TYPE_BYTE_GRAY);
        dataLinesCount = 0;
        stringBuilderBytes = new StringBuilder();
        vp.addProcessor(this);
        initialized = true;
        System.out.println("original width = " + width
                + " height = " + height);
    }

    @Override
    public void reshape(ViewPort vp, int w, int h) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void preFrame(float tpf) {
    }

    @Override
    public void postQueue(RenderQueue rq) {
    }

    @Override
    public void postFrame(FrameBuffer out) {
        byteBuffer.clear();
        renderer.readFrameBuffer(out, byteBuffer);
        synchronized (bufferedImage) {
            Screenshots.convertScreenShot(byteBuffer, bufferedImage);
            if (System.currentTimeMillis() - millis > 2000) {
                millis = System.currentTimeMillis();
                //each 2 seconds
                ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
                op.filter(bufferedImage, bigGrayBufferedImage);
                AffineTransform affineTransform = new AffineTransform();
                affineTransform.scale(scaleFactor, scaleFactor);
                AffineTransformOp scaleOp
                        = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BILINEAR);
                scaleOp.filter(bigGrayBufferedImage, grayBufferedImage);
                WritableRaster raster = grayBufferedImage.getRaster();
                DataBufferByte data = (DataBufferByte) raster.getDataBuffer();
                byte[] rawPixels = data.getData();
                dataColumnsCount = rawPixels.length;
                dataLinesCount++;
                int b;
                for (int k = 0; k < rawPixels.length; k++) {
                    b = rawPixels[k] & 0xFF;
                    stringBuilderBytes.append(b);
                    if (k < rawPixels.length - 1) {
                        stringBuilderBytes.append(" ");
                    } else {
                        stringBuilderBytes.append("\n");
                    }
                }
            }
        }
    }

    private void writeToFile() {
        try {
            file = File.createTempFile("mlAutoVirt", ".txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            System.out.println("Temporary file: "
                    + file.getAbsoluteFile().toString()
                    + "(" + (width * scaleFactor)
                    + "x" + (height * scaleFactor) + ")");

            StringBuilder stringBuilderHeader = new StringBuilder();
            stringBuilderHeader.append("# name: x\n");
            stringBuilderHeader.append("# type: matrix\n");
            stringBuilderHeader.append("# rows: "
                    + dataLinesCount + "\n");
            stringBuilderHeader.append("# columns: "
                    + dataColumnsCount + "\n");
            writer.append(stringBuilderHeader);
            writer.append(stringBuilderBytes);
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(ScreenCaptureAppState.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void cleanup() {
        writeToFile();
        super.cleanup();
        initialized = false;
    }
}
