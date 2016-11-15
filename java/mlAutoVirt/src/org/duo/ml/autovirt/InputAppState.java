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
    private InputManager inputManager;
    private float speed;
    private float angle;

    public enum InputMapping {
        RotateLeft, RotateRight, MoveFoward, MoveBackward;
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
    }
    
    @Override
    public void onAnalog(String name, float value, float tpf) {
        if(name.equals(InputMapping.MoveFoward.name())) {
            speed += 0.5f;
            // set max speed
            if(speed>80.0f) speed = 80.0f;
        } else if(name.equals(InputMapping.MoveBackward.name())) {
            speed -= 1.0f;
            // no back moving
            if(speed<0.0f) speed = 0.0f;
        }
        else if(name.equals(InputMapping.RotateLeft.name())) {
            angle += 0.005f;
            // when close to straight, make direction straight
            if(angle<=0.0f && angle > -1.0f) angle = 0.0f;
            // max positive angle
            if(angle>5.0f) angle = 5.0f;
        }
        else if(name.equals(InputMapping.RotateRight.name())) {
            angle -= 0.005f;
            // when close to straight, make direction straight
            if(angle>=0.0f && angle < 1.0f) angle = 0.0f;
            // max negative angle
            if(angle<-5.0f) angle = -5.0f;
        }

    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
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
