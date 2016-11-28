package org.duo.ml.autovirt;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import org.duo.ml.util.MlAutoVirtState;

/**
 * test
 *
 * @author normenhansen
 */
public class MlAutoVirt extends SimpleApplication {

    private Node carNode;
    private Spatial car;
    private Spatial volante;
    private CameraNode cameraNode;
    private float carSpeed;
    private InputAppState inputAppState;
    private ScreenCaptureAppState screenCaptureAppState;
    public long shtIndex = 0L;

    private TerrainQuad terrainQuad;
    //terrain common
    private Node terrain;
    //Materials
    private Material matRock;
    private BitmapText hudText;
    //Result
    private int result;

    public MlAutoVirt() {
        super(new ConfigAppState());
    }

    public static void main(String[] args) {
        MlAutoVirt app = new MlAutoVirt();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        createTerrain();
        rootNode.addLight(new AmbientLight());
        inputAppState = new InputAppState();
        stateManager.attach(inputAppState);
        screenCaptureAppState = new ScreenCaptureAppState();
        stateManager.attach(screenCaptureAppState);
        car = assetManager.loadModel("Models/Carroblend.j3o");
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
        car.setMaterial(mat);
        car.setCullHint(Spatial.CullHint.Never);
        car.scale(2.0f);
        Quaternion quaternion = new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y);
        car.rotate(quaternion);
        car.setLocalTranslation(0.0f, 2.0f, 0.0f);
        ((Node) car).getChildren().stream().forEach((child) -> {
            ((Node) child).getChildren().stream().filter((child2) -> (child2.getName().equals("Volante"))).forEach((child2) -> {
                volante = child2;
                volante.addControl(new SteeringWheelControl(this));
            }); //System.out.println(child2.getName());
        });
        carNode = new Node("carNode");
        carNode.attachChild(car);
        carNode.setLocalTranslation(0.0f, 0.8f, 0.0f);
        carNode.addControl(new TerrainTrackControl());
        carNode.addControl(new MovementControl(this));
        cameraNode = new CameraNode("cameraNode", cam);
        cameraNode.setLocalTranslation(new Vector3f(-3.5f, 3.0f, 0.0f));
        Vector3f lookAtVector = carNode.getLocalTranslation();
        lookAtVector.x = lookAtVector.x + 10;
        cameraNode.lookAt(lookAtVector, Vector3f.UNIT_Y);
        cameraNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
        Vector3f camLeft = cam.getDirection().negate();
        carNode.attachChild(cameraNode);
        carSpeed = 0.0f;
        rootNode.attachChild(carNode);
        carNode.setLocalTranslation(120.449646f, -38.18f, 116.01076f);
        //volante.rotate(new Quaternion().fromAngleAxis(-90*FastMath.DEG_TO_RAD, Vector3f.UNIT_Z));
        hudText = new BitmapText(guiFont, false);
        hudText.setSize(guiFont.getCharSet().getRenderedSize());      // font size
        hudText.setColor(ColorRGBA.Blue);                             // font color
        hudText.setText("ML AutoVirt");          // the text
        hudText.setLocalTranslation(300, hudText.getLineHeight(), 0); // position
        guiNode.attachChild(hudText);
        screenCaptureAppState.initialize(renderManager, viewPort);
        screenCaptureAppState.setApp(this);
    }

    @Override
    public void simpleUpdate(float tpf) {
        super.simpleUpdate(tpf);
        hudText.setText("Speed: " + getInputAppState().getSpeed()
                + " angle: " + getInputAppState().getAngleIndex()
                + " pos: (" + carNode.getLocalTranslation().x + ", "
                + carNode.getLocalTranslation().y + ", "
                + carNode.getLocalTranslation().z + ")");
    }

    private void createTerrain() {
        // load sky
        Spatial sky = SkyFactory.createSky(assetManager, "Textures/ColorRamp/cloudy.png", SkyFactory.EnvMapType.SphereMap);
        //Spatial sky = SkyFactory.createSky(assetManager, "Textures/Sky/St Peters/StPeters.jpg", SkyFactory.EnvMapType.SphereMap);
        rootNode.attachChild(sky);
        //rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false));
        matRock = new Material(assetManager, "Common/MatDefs/Terrain/TerrainLighting.j3md");
        matRock.setBoolean("useTriPlanarMapping", false);
        matRock.setBoolean("WardIso", true);
        matRock.setTexture("AlphaMap", assetManager.loadTexture("Textures/Terrain/Alpha/alphastreet.png"));
        Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");
        Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
        grass.setWrap(Texture.WrapMode.Repeat);
        matRock.setTexture("DiffuseMap", grass);
        matRock.setFloat("DiffuseMap_0_scale", 64);
        Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(Texture.WrapMode.Repeat);
        matRock.setTexture("DiffuseMap_1", dirt);
        matRock.setFloat("DiffuseMap_1_scale", 16);
        Texture rock = assetManager.loadTexture("Textures/Terrain/Alpha/street.png");
        rock.setWrap(Texture.WrapMode.Repeat);
        matRock.setTexture("DiffuseMap_2", rock);
        matRock.setFloat("DiffuseMap_2_scale", 128);
        Texture normalMap0 = assetManager.loadTexture("Textures/Terrain/splat/grass_normal.jpg");
        normalMap0.setWrap(Texture.WrapMode.Repeat);
        Texture normalMap1 = assetManager.loadTexture("Textures/Terrain/splat/dirt_normal.png");
        normalMap1.setWrap(Texture.WrapMode.Repeat);
        Texture normalMap2 = assetManager.loadTexture("Textures/Terrain/splat/road_normal.png");
        normalMap2.setWrap(Texture.WrapMode.Repeat);
        matRock.setTexture("NormalMap", normalMap0);
        matRock.setTexture("NormalMap_1", normalMap2);
        matRock.setTexture("NormalMap_2", normalMap2);

        try {
            AbstractHeightMap heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), 0.02f);
            heightmap.load();
            terrainQuad = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());
            terrainQuad.setMaterial(matRock);
            terrainQuad.setLocalScale(new Vector3f(2, 2, 2));
            terrainQuad.setLocalTranslation(-100, -40, 150);
            terrain = new Node();
            terrain.attachChild(terrainQuad);

            rootNode.attachChild(terrain);

        } catch (Exception e) {
        }

    }

    public CameraNode getCameraNode() {
        return cameraNode;
    }

    public float getCarSpeed() {
        return carSpeed;
    }

    public InputAppState getInputAppState() {
        return inputAppState;
    }

    public Spatial getCar() {
        return car;
    }

    public Node getCarNode() {
        return carNode;
    }

    public int getResult() {
        result = inputAppState.getAngleIndex();
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public void setState(MlAutoVirtState state) {
        this.screenCaptureAppState.setState(state);
    }

    public Spatial getVolante() {
        return volante;
    }

    public void setVolante(Spatial volante) {
        this.volante = volante;
    }

    public ScreenCaptureAppState getScreenCaptureAppState() {
        return screenCaptureAppState;
    }

}
