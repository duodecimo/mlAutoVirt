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
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.duo.ml.util.MlAutoVirtState;

/**
 *
 * @author duo
 */
public class ScreenCaptureAppState
        extends AbstractAppState
        implements SceneProcessor {

    private MlAutoVirtState state;
    private Renderer renderer;
    private int widthOriginal;
    private int heightOriginal;
    private int widthScaled;
    private int heightScaled;
    private long millis;
    private ByteBuffer byteBuffer;
    // original image
    private BufferedImage bufferedImage;
    // store gray shades
    private BufferedImage bigGrayBufferedImage;
    // scaled gray shades
    private BufferedImage grayBufferedImage;
    private MlAutoVirt app;
    // stringBuilder to store all bytes in Octave format
    private StringBuilder stringBuilderBytes;
    private StringBuilder stringBuilderResults;
    private int dataLinesCount;
    private int dataColumnsCount;
    private File fileX, fileY;
    private float scaleFactor;

    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
        System.out.println("ScreenCaptureAppState initialized!");
        this.renderer = rm.getRenderer();
        millis = System.currentTimeMillis();
        widthOriginal = vp.getCamera().getWidth();
        heightOriginal = vp.getCamera().getHeight();
        scaleFactor = 0.025F;
        widthScaled = ((int) (widthOriginal * scaleFactor));
        heightScaled = ((int) (heightOriginal * scaleFactor));
        byteBuffer = BufferUtils.createByteBuffer(widthOriginal *
                heightOriginal * 4);
        // original image
        bufferedImage = new BufferedImage(widthOriginal, 
                heightOriginal,
                BufferedImage.TYPE_4BYTE_ABGR);
        // store gray shades
        bigGrayBufferedImage = new BufferedImage(widthOriginal, 
                heightOriginal,
                BufferedImage.TYPE_BYTE_GRAY);
        // scaled gray shades
        grayBufferedImage = new BufferedImage(widthScaled,
                heightScaled,
                BufferedImage.TYPE_BYTE_GRAY);
        dataLinesCount = 0;
        stringBuilderBytes = new StringBuilder();
        stringBuilderResults = new StringBuilder();
        vp.addProcessor(this);
        // initial state
        state = MlAutoVirtState.IDLE;
        initialized = true;
        System.out.println("original width = " + widthOriginal +
                " height = " + heightOriginal +
                " total = " + widthOriginal * heightOriginal +
                " scaled width = " + widthScaled +
                " height = " + heightScaled +
                " total = " + widthScaled * heightScaled);
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
        int result = app.getResult();
        byteBuffer.clear();
        renderer.readFrameBuffer(out, byteBuffer);
        synchronized (bufferedImage) {
            Screenshots.convertScreenShot(byteBuffer, bufferedImage);
            if (System.currentTimeMillis() - millis > 2000) {
                millis = System.currentTimeMillis();
                //each 2 seconds
                ColorConvertOp op = 
                        new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
                op.filter(bufferedImage, bigGrayBufferedImage);
                AffineTransform affineTransform = 
                        new AffineTransform();
                affineTransform.scale(scaleFactor, scaleFactor);
                AffineTransformOp scaleOp
                        = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BILINEAR);
                scaleOp.filter(bigGrayBufferedImage, grayBufferedImage);
                WritableRaster raster = grayBufferedImage.getRaster();
                DataBufferByte data = 
                        (DataBufferByte) raster.getDataBuffer();
                byte[] rawPixels = data.getData();
                if (state == MlAutoVirtState.LEARNING) {
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
                    stringBuilderResults.append(result).append("\n");
                }
            }
        }
    }

    private void writeToFile() {
        if (dataLinesCount > 0) {
            try {
                fileX = File.createTempFile("mlAutoVirtX", ".txt");
                BufferedWriter writerY;
                StringBuilder stringBuilderHeader;
                try (BufferedWriter writerX = new BufferedWriter(new FileWriter(fileX))) {
                    fileY = File.createTempFile("mlAutoVirtY", ".txt");
                    writerY = new BufferedWriter(new FileWriter(fileY));
                    System.out.println("Temporary files: "
                            + fileX.getAbsoluteFile().toString()
                            + "(" + widthScaled
                            + "x" + heightScaled + ") and "
                            + fileY.getAbsoluteFile().toString());
                    stringBuilderHeader = new StringBuilder();
                    stringBuilderHeader.append("# name: x\n");
                    stringBuilderHeader.append("# type: matrix\n");
                    stringBuilderHeader.append("# rows: ").
                            append(dataLinesCount).append("\n");
                    stringBuilderHeader.append("# columns: ").
                            append(dataColumnsCount).append("\n");
                    writerX.append(stringBuilderHeader);
                    writerX.append(stringBuilderBytes);
                }
                stringBuilderHeader = new StringBuilder();
                stringBuilderHeader.append("# name: y\n");
                stringBuilderHeader.append("# type: matrix\n");
                stringBuilderHeader.append("# rows: ").
                        append(dataLinesCount).append("\n");
                stringBuilderHeader.append("# columns: 1\n");
                writerY.append(stringBuilderHeader);
                writerY.append(stringBuilderResults);
                writerY.close();
            } catch (IOException ex) {
                Logger.getLogger(ScreenCaptureAppState.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void cleanup() {
        writeToFile();
        super.cleanup();
        initialized = false;
    }

    public void setApp(MlAutoVirt app) {
        this.app = app;
    }

    public MlAutoVirtState getState() {
        return state;
    }

    public void setState(MlAutoVirtState state) {
        this.state = state;
    }

}
