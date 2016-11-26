/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.duo.ml.autovirt;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;

/**
 *
 * @author duo
 */
public class InputAppState 
    extends AbstractAppState 
    implements ActionListener, AnalogListener {
    private MlAutoVirt app;
    private InputManager inputManager;
    private float speed;
    private float angle;
    private final float MAXSPEED = 6.0f;
    private final float INCSPEED = 2.0f;
    private final float MAXANGLE = 0.09f;
    private final float INCANGLE = 0.03f;
    private int angleIndex;
    private float[] angleValues;
    
    public enum InputMapping {
        RotateLeft, RotateRight, MoveFoward, MoveBackward, Print;
    }

    private void addInputMappings() {
        inputManager.addMapping(InputMapping.RotateLeft.name(), 
                new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping(InputMapping.RotateRight.name(), 
                new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping(InputMapping.MoveFoward.name(), 
                new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping(InputMapping.MoveBackward.name(), 
                new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping(InputMapping.Print.name(),
                new KeyTrigger(KeyInput.KEY_P));
        for(InputMapping i : InputMapping.values()) {
            inputManager.addListener(this, i.name());
        }
    }

    @Override
    public void initialize(AppStateManager appStateManager, 
            Application app) {
        this.inputManager = app.getInputManager();
        addInputMappings();
        speed = 0.0f;
        angle = 0.0f;
        this.app = (MlAutoVirt) app;
        angleIndex = 4;
        // table for angle value lookup
        angleValues = new float[7];
        angleValues[0] = 0.09f;
        angleValues[1] = 0.06f;
        angleValues[2] = 0.03f;
        angleValues[3] = 0.0f;
        angleValues[4] = -0.03f;
        angleValues[5] = -0.06f;
        angleValues[6] = -0.09f;
    }
    
    @Override
    public void onAnalog(String name, float value, float tpf) {
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (isPressed) {
            if (name.equals(InputMapping.MoveFoward.name())) {
                speed += INCSPEED;
                // set max speed
                if (speed > MAXSPEED) {
                    speed = MAXSPEED;
                }
            } else if (name.equals(InputMapping.MoveBackward.name())) {
                speed -= INCSPEED;
                // no back moving
                if (speed < 0.0f) {
                    speed = 0.0f;
                }
            } else if (name.equals(InputMapping.RotateLeft.name())) {
                angleIndex--;
                if(angleIndex<1) {
                    angleIndex = 1;
                }
                angle = angleValues[angleIndex-1];
            } else if (name.equals(InputMapping.RotateRight.name())) {
                angleIndex++;
                if(angleIndex>7) {
                    angleIndex = 7;
                }
                angle = angleValues[angleIndex-1];
            } else if (name.equals(InputMapping.Print.name())) {
                if (isPressed) {
                //((Main) app).getScreenshotAppState().takeScreenshot();
                    //System.out.println("Screenshot taken!");
                }
            }
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        for(InputMapping i : InputMapping.values()) {
            if(inputManager.hasMapping(i.name())) {
                inputManager.deleteMapping(i.name());
            }
            inputManager.removeListener(this);
        }
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public int getAngleIndex() {
        return angleIndex;
    }

    public float getAngle() {
        return angle;
    }

}
