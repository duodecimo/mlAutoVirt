/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.duo.ml.autovirt;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author duo
 */
public class SteeringWheelControl extends AbstractControl {

    private final MlAutoVirt app;
    private final Quaternion rollLeft;
    private final Quaternion rollRight;
    private final Quaternion steering;
    private final float[] rotation;

    public SteeringWheelControl(MlAutoVirt app) {
        this.app = app;
        rollLeft = new Quaternion();
        rollLeft.fromAngleAxis(80 * FastMath.DEG_TO_RAD,
                Vector3f.UNIT_Z);
        rollRight = new Quaternion();
        rollRight.fromAngleAxis(-15 * FastMath.DEG_TO_RAD,
                Vector3f.UNIT_Z);
        steering = new Quaternion();
        /*        rotation = new float[7];
        rotation[0] = 0.0f;
        rotation[1] = 0.15f;
        rotation[2] = 0.35f;
        rotation[3] = 0.5f;
        rotation[4] = 0.65f;
        rotation[5] = 0.85f;
        rotation[6] = 1.0f;*/
        rotation = new float[3];
        rotation[0] = 0.0f;
        rotation[1] = 0.5f;
        rotation[2] = 1.0f;
    }

    @Override
    protected void controlUpdate(float tpf) {
        steering.slerp(rollLeft, rollRight, rotation[app.getInputAppState().getAngleIndex()-1]);
        app.getVolante().setLocalRotation(steering);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
    
}
