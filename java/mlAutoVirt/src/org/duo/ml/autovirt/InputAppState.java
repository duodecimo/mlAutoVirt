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
import org.duo.ml.util.MlAutoVirtState;

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
    private final float MAXSPEED = 10.0f;
    private final float INCSPEED = 2.0f;
    private int angleIndex;
    private float[] angleValues;
    
    public enum InputMapping {
        RotateLeft, RotateRight, MoveFoward, MoveBackward, 
        Print, Idle, Learning, AutoDriving;
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
        inputManager.addMapping(InputMapping.Idle.name(),
                new KeyTrigger(KeyInput.KEY_I));
        inputManager.addMapping(InputMapping.Learning.name(),
                new KeyTrigger(KeyInput.KEY_C));
        inputManager.addMapping(InputMapping.AutoDriving.name(),
                new KeyTrigger(KeyInput.KEY_A));
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
        /*        angleIndex = 4;
        // table for angle value lookup
        angleValues = new float[7];
        angleValues[0] = 0.12f;
        angleValues[1] = 0.08f;
        angleValues[2] = 0.04f;
        angleValues[3] = 0.0f;
        angleValues[4] = -0.04f;
        angleValues[5] = -0.08f;
        angleValues[6] = -0.12f;*/
        // lets try with only 3 positions
        // shortening the possible results
        angleIndex = 2;
        // table for angle value lookup
        angleValues = new float[3];
        angleValues[0] = 0.12f;
        angleValues[1] = 0.0f;
        angleValues[2] = -0.12f;
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
                switch(angleIndex) {
                    case 2:
                        angleIndex = 1;
                        break;
                    case 3:
                        angleIndex = 2;
                        break;
                    case 1:
                        angleIndex = 1;
                }
                //angleIndex--;
                //if(angleIndex<1) {
                //    angleIndex = 1;
                //}
                setAngle(angleValues[angleIndex-1]);
            } else if (name.equals(InputMapping.RotateRight.name())) {
                switch(angleIndex) {
                    case 2:
                        angleIndex = 3;
                        break;
                    case 3:
                        angleIndex = 3;
                        break;
                    case 1:
                        angleIndex = 2;
                }
                //angleIndex++;
                //if(angleIndex>7) {
                //    angleIndex = 7;
                //}
                setAngle(angleValues[angleIndex-1]);
            } else if (name.equals(InputMapping.Print.name())) {
                if (isPressed) {
                //((Main) app).getScreenshotAppState().takeScreenshot();
                    //System.out.println("Screenshot taken!");
                }
            } else if (name.equals(InputMapping.Idle.name())) {
                app.setState(MlAutoVirtState.IDLE);
            } else if (name.equals(InputMapping.Learning.name())) {
                app.setState(MlAutoVirtState.CAPTURING);
            } else if (name.equals(InputMapping.AutoDriving.name())) {
                app.setState(MlAutoVirtState.AUTODRIVING);
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

    public void setAngleIndex(int angleIndex) {
        this.angleIndex = angleIndex;
    }

    public float getAngle() {
        return angleValues[angleIndex-1];
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

}
