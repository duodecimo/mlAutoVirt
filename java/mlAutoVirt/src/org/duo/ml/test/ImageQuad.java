/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.duo.ml.test;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;

/**
 *
 * @author duo
 */
public class ImageQuad {
   public void display(Image image, SimpleApplication app) {
        Texture tex = new Texture2D(image);
        tex.setMagFilter(Texture.MagFilter.Nearest);
        tex.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        tex.setAnisotropicFilter(16);
        Material mat = new Material(app.getAssetManager(), 
                "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", tex);

        Quad q = new Quad(5, 5);
        Geometry g = new Geometry("quad", q);
        g.setLocalTranslation(0.0f, 500.0f - 5, -0.0001f);
        g.setMaterial(mat);
        app.getRootNode().attachChild(g);
   }
}
