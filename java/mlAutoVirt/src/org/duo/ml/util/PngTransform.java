/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.duo.ml.util;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author duo
 */
public class PngTransform {

    private ImageIO imageIO;
    private int index = 0;
    private final int FINALWIDTH = 32;
    private final int FINALHEIGHT = 18;

    public PngTransform() {
        try (Stream<Path> paths = Files.walk(Paths.get("/home/duo/Imagens/mlAutoVirt"))) {
            // make sure paths elements are sorted in natural order
            (paths.sorted()).forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    try {
                        System.out.println(filePath);
                        BufferedImage bufferedImage = ImageIO.read(filePath.toFile());
                        System.out.println("bufferedImage: " + bufferedImage.getWidth()
                                + ", " + bufferedImage.getHeight());
                        Image image = bufferedImage.getScaledInstance(FINALWIDTH, FINALHEIGHT, BufferedImage.TYPE_BYTE_GRAY);
                        BufferedImage scaled = new BufferedImage(FINALWIDTH, FINALHEIGHT, BufferedImage.TYPE_BYTE_GRAY);
                        Graphics2D g2 = (Graphics2D) scaled.getGraphics();
                        g2.drawImage(image, 0, 0, null);
                        scaled.flush();
                        // use the show method for debug purposes, maybe changing 
                        // FINALWIDTH to 320 and FINALHEIGHT to 180
                        //show("trans", scaled, index++);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(scaled, "png", baos);
                        baos.flush();
                        byte[] imageInByte = baos.toByteArray();
                        // now save the bytes in Octave format to be processed with machine learning.
                        //imageInByte.length;
                    } catch (IOException ex) {
                        Logger.getLogger(PngTransform.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(PngTransform.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new PngTransform();
    }

    private static void show(String title, final BufferedImage img, int i) {
        JFrame f = new JFrame(title);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setContentPane(new JPanel() {
            @Override
            protected void paintChildren(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.drawImage(img, null, 0, 0);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(img.getWidth(), img.getHeight());
            }
        });
        f.pack();
        f.setLocation(50 + (i * 50), 50 + (i * 50));
        f.setVisible(true);
    }
}
