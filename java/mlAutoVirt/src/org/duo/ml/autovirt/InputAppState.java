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
            }
            if (name.equals(InputMapping.RotateLeft.name())) {
                angle += INCANGLE;
                if (angle > MAXANGLE) {
                    angle = MAXANGLE;
                }
            } else if (name.equals(InputMapping.RotateRight.name())) {
                angle -= INCANGLE;
                if (angle < -MAXANGLE) {
                    angle = -MAXANGLE;
                }
            }
            if (angle == 0.09f) {
                app.setDirectionEnum(MlAutoVirt.DirectionEnum.LEFTMOST);
            } else if (angle == 0.06f) {
                app.setDirectionEnum(MlAutoVirt.DirectionEnum.LEFTMID);
            } else if (angle == 0.03f) {
                app.setDirectionEnum(MlAutoVirt.DirectionEnum.LEFTMIN);
            } else if (angle == 0.0f) {
                app.setDirectionEnum(MlAutoVirt.DirectionEnum.CENTER);
            } else if (angle == -0.03f) {
                app.setDirectionEnum(MlAutoVirt.DirectionEnum.RIGHTMIN);
            } else if (angle == -0.06f) {
                app.setDirectionEnum(MlAutoVirt.DirectionEnum.RIGHTMID);
            } else if (angle == -0.09f) {
                app.setDirectionEnum(MlAutoVirt.DirectionEnum.RIGHTMOST);
            }
            if (name.equals(InputMapping.Print.name())) {
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

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

}
