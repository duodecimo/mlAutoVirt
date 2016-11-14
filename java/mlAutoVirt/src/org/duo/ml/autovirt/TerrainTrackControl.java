/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.duo.ml.autovirt;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;
import java.util.Iterator;

/**
 *
 * @author normenhansen
 */
public class TerrainTrackControl extends AbstractControl{
    //Any local variables should be encapsulated by getters/setters so they
    //appear in the SDK properties window and can be edited.
    //Right-click a local variable to encapsulate it with getters and setters.

    private final Ray ray = new Ray(Vector3f.ZERO.clone(), new Vector3f(0, -1, 0));
    private final Vector3f up = new Vector3f(0, 50, 0);
    private final CollisionResults results = new CollisionResults();
    private final float offset = 0.5f;
    private Spatial terrain;

    public TerrainTrackControl() {
    }

    @Override
    protected void controlUpdate(float tpf) {
        terrain = spatial.getParent();
        if (terrain != null) {
            ray.setOrigin(spatial.getWorldTranslation().add(up));
            ray.setLimit(100);
            results.clear();
            terrain.collideWith(ray, results);
            for (Iterator<CollisionResult> it = results.iterator(); it.hasNext();) {
                CollisionResult collisionResult = it.next();
                if (isTerrain(collisionResult.getGeometry())) {
                    Vector3f loc = collisionResult.getContactPoint();
                    spatial.setLocalTranslation(spatial.getLocalTranslation().setY(loc.getY() + offset));
                    return;
                }
            }
        }
    }

    private boolean isTerrain(Spatial spat) {
        while (true) {
            if (spat == null) {
                return false;
            } else if ("terrain".equals(spat.getName())) {
                return true;
            }
            spat = spat.getParent();
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //Only needed for rendering-related operations,
        //not called when spatial is culled.
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        TerrainTrackControl control = new TerrainTrackControl();
        //TODO: copy parameters to new Control
        return control;
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule in = im.getCapsule(this);
        //TODO: load properties of this Control, e.g.
        //this.value = in.readFloat("name", defaultValue);
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule out = ex.getCapsule(this);
        //TODO: save properties of this Control, e.g.
        //out.write(this.value, "name", defaultValue);
    }
}
