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
    private Application app;
    private InputManager inputManager;
    private float speed;
    private float angle;
    private final float MAXSPEED = 80.0f;
    private final float INCSPEED = 1.0f;
    private final float MAXANGLE = 5.0f;
    private final float INCANGLE = 0.005f;
    
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
        this.app = app;
    }
    
    @Override
    public void onAnalog(String name, float value, float tpf) {
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
            angle += INCANGLE;
        } else if (name.equals(InputMapping.RotateRight.name())) {
            angle -= INCANGLE;
        }
        // when close to straight, make direction straight
        if (angle <= 0.0f && angle > -0.003f) {
            angle = 0.0f;
        }
        // max positive angle
        if (angle > MAXANGLE) {
            angle = MAXANGLE;
        }
        // when close to straight, make direction straight
        if (angle >= 0.0f && angle < 0.003f) {
            angle = 0.0f;
        }
        // max negative angle
        if (angle < -MAXANGLE) {
            angle = -MAXANGLE;
        }
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if(name.equals(InputMapping.Print.name())){
            if(isPressed) {
                //((Main) app).getScreenshotAppState().takeScreenshot();
                //System.out.println("Screenshot taken!");
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
