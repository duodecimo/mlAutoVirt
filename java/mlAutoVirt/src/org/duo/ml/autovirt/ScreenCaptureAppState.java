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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.duo.ml.test.TestRenderToMemory;

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
    private final String SCREENSHOTFILEPATH = 
            "/home/duo/Imagens/mlAutoVirt/captured.txt";

    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
        System.out.println("ScreenCaptureAppState initialized!");
        this.renderer = rm.getRenderer();
        millis = System.currentTimeMillis();
        width = vp.getCamera().getWidth();
        height = vp.getCamera().getHeight();
        byteBuffer = BufferUtils.createByteBuffer(width * height * 4);
        // original image
        bufferedImage = new BufferedImage(width, height,
                                            BufferedImage.TYPE_4BYTE_ABGR);
        // store gray shades
        bigGrayBufferedImage = new BufferedImage(width, height,
                                            BufferedImage.TYPE_BYTE_GRAY);
        // scaled gray shades
        grayBufferedImage = new BufferedImage(width/4, height/4,
                                            BufferedImage.TYPE_BYTE_GRAY);
        vp.addProcessor(this);
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
        //System.out.println("ScreenCaptureAppState postFrame called!");
        byteBuffer.clear();
        renderer.readFrameBuffer(out, byteBuffer);

        synchronized (bufferedImage) {
            Screenshots.convertScreenShot(byteBuffer, bufferedImage);
            if(System.currentTimeMillis()-millis > 4000) {
                OutputStream outputStream = null;
                try {
                    millis = System.currentTimeMillis();
                    System.out.println("Converting to grayscale at " + (millis/1000));
                    //each 4 seconds
                    ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
                    op.filter(bufferedImage, bigGrayBufferedImage);
                    AffineTransform affineTransform = new AffineTransform();
                    affineTransform.scale(0.25, 0.25);
                    AffineTransformOp scaleOp =
                            new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BILINEAR);
                    scaleOp.filter(bigGrayBufferedImage, grayBufferedImage);
                    //show("Gray scale: " + (millis/1000), grayBufferedImage, 4);
                    WritableRaster raster = grayBufferedImage.getRaster();
                    DataBufferByte data = (DataBufferByte) raster.getDataBuffer();
                    byte[] rawPixels = data.getData();
                    int b;
                    StringBuilder sb = new StringBuilder();
                    for(int k=0; k<rawPixels.length; k++) {
                        b = rawPixels[k] & 0xFF;
                        sb.append(b);
                        if(k<rawPixels.length-1) {
                            sb.append(" ");
                        } else {
                            sb.append("\n");
                        }
                    }
                    System.out.println("bytes: (" +
                            + rawPixels.length + ") " +
                            sb.substring(22000, 22500));
                    outputStream = new FileOutputStream(SCREENSHOTFILEPATH, true);
                    
                    //for(int i=0; i< rawPixels.length; i++) {
                    //    out.write((int) rawPixels[i] & 0xFF);
                    //}
                    //out.write(rawPixels, 0, rawPixels.length);
                    //out.write(10);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(TestRenderToMemory.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        outputStream.close();
                    } catch (IOException ex) {
                        Logger.getLogger(TestRenderToMemory.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }
}
