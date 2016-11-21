/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.duo.ml.test;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext.Type;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;
import com.jme3.util.Screenshots;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * This test renders a scene to an offscreen framebuffer, then copies
 * the contents to a Swing JFrame. Note that some parts are done inefficently,
 * this is done to make the code more readable.
 */

/**
 *
 * @author duo
 */
public class TestRenderToMemory extends SimpleApplication implements SceneProcessor {

    private Geometry geometry;
    private float angle = 0;

    private FrameBuffer frameBuffer;
    private ViewPort viewPort1;
    private Texture2D texture2D;
    private Camera camera;
    private ImageDisplay imageDisplay;

    private static final int width = 800, height = 600;

    private final ByteBuffer byteBuffer = BufferUtils.createByteBuffer(width * height * 4);
    private final byte[] cpuArray = new byte[width * height * 4];
    private final BufferedImage bufferedImage = new BufferedImage(width, height,
                                            BufferedImage.TYPE_4BYTE_ABGR);

    private class ImageDisplay extends JPanel {

        private long t;
        private long total;
        private int frames;
        private int fps;

        @Override
        public void paintComponent(Graphics gfx) {
            super.paintComponent(gfx);
            Graphics2D g2d = (Graphics2D) gfx;

            if (t == 0)
                t = timer.getTime();

//            g2d.setBackground(Color.BLACK);
//            g2d.clearRect(0,0,width,height);

            synchronized (bufferedImage){
                g2d.drawImage(bufferedImage, null, 0, 0);
            }

            long t2 = timer.getTime();
            long dt = t2 - t;
            total += dt;
            frames ++;
            t = t2;

            if (total > 1000){
                fps = frames;
                total = 0;
                frames = 0;
            }

            g2d.setColor(Color.white);
            g2d.drawString("FPS: "+fps, 0, getHeight() - 100);
        }
    }

    public static void main(String[] args){
        TestRenderToMemory app = new TestRenderToMemory();
        app.setPauseOnLostFocus(false);
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1, 1);
        app.setSettings(settings);
        app.start(Type.OffscreenSurface);
    }

    public void createDisplayFrame(){
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                JFrame frame = new JFrame("Render Display");
                imageDisplay = new ImageDisplay();
                imageDisplay.setPreferredSize(new Dimension(width, height));
                frame.getContentPane().add(imageDisplay);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.addWindowListener(new WindowAdapter(){
                    public void windowClosed(WindowEvent e){
                        stop();
                    }
                });
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setResizable(false);
                frame.setVisible(true);
            }
        });
    }

    public void updateImageContents(){
        byteBuffer.clear();
        renderer.readFrameBuffer(frameBuffer, byteBuffer);

        synchronized (bufferedImage) {
            Screenshots.convertScreenShot(byteBuffer, bufferedImage);    
        }

        if (imageDisplay != null)
            imageDisplay.repaint();
    }

    public void setupOffscreenView(){
        camera = new Camera(width, height);

        // create a pre-view. a view that is rendered before the main view
        viewPort1 = renderManager.createPreView("Offscreen View", camera);
        viewPort1.setBackgroundColor(ColorRGBA.DarkGray);
        viewPort1.setClearFlags(true, true, true);
        
        // this will let us know when the scene has been rendered to the 
        // frame buffer
        viewPort1.addProcessor(this);

        // create offscreen framebuffer
        frameBuffer = new FrameBuffer(width, height, 1);

        //setup framebuffer's cam
        camera.setFrustumPerspective(45f, 1f, 1f, 1000f);
        camera.setLocation(new Vector3f(0f, 0f, -5f));
        camera.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);

        //setup framebuffer's texture
//        offTex = new Texture2D(width, height, Format.RGBA8);

        //setup framebuffer to use renderbuffer
        // this is faster for gpu -> cpu copies
        frameBuffer.setDepthBuffer(Format.Depth);
        frameBuffer.setColorBuffer(Format.RGBA8);
//        offBuffer.setColorTexture(offTex);
        
        //set viewport to render to offscreen framebuffer
        viewPort1.setOutputFrameBuffer(frameBuffer);

        // setup framebuffer's scene
        Box boxMesh = new Box(Vector3f.ZERO, 1,1,1);
        Material material = assetManager.loadMaterial("Interface/Logo/Logo.j3m");
        geometry = new Geometry("box", boxMesh);
        geometry.setMaterial(material);

        // attach the scene to the viewport to be rendered
        viewPort1.attachScene(geometry);
    }

    @Override
    public void simpleInitApp() {
        setupOffscreenView();
        createDisplayFrame();
    }

    @Override
    public void simpleUpdate(float tpf){
        Quaternion q = new Quaternion();
        angle += tpf;
        angle %= FastMath.TWO_PI;
        q.fromAngles(angle, 0, angle);

        geometry.setLocalRotation(q);
        geometry.updateLogicalState(tpf);
        geometry.updateGeometricState();
    }

    public void initialize(RenderManager rm, ViewPort vp) {
    }

    public void reshape(ViewPort vp, int w, int h) {
    }

    public boolean isInitialized() {
        return true;
    }

    public void preFrame(float tpf) {
    }

    public void postQueue(RenderQueue rq) {
    }

    /**
     * Update the CPU image's contents after the scene has
     * been rendered to the framebuffer.
     */
    public void postFrame(FrameBuffer out) {
        updateImageContents();
    }

    public void cleanup() {
    }


}
