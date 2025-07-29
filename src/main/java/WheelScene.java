import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class WheelScene extends Application {
    static final java.util.concurrent.CountDownLatch READY = new java.util.concurrent.CountDownLatch(1);
    private final Rotate rotateX = new Rotate(180, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);
    private final double[] lastMouse = new double[2];
    private static final Cylinder[] wheels = new Cylinder[4];
    private static final double SPACING = 100;

    @Override
    public void start(Stage stage) {
        Group root = new Group();
        PhongMaterial wheelMaterial = new PhongMaterial(Color.DARKGRAY);
        wheels[0] = createWheel(SPACING, SPACING, wheelMaterial);
        wheels[1] = createWheel(-SPACING, SPACING, wheelMaterial);
        wheels[2] = createWheel(-SPACING, -SPACING, wheelMaterial);
        wheels[3] = createWheel(SPACING, -SPACING, wheelMaterial);
        root.getChildren().addAll(wheels);
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000);
        camera.setFieldOfView(50);
        camera.setTranslateZ(300);
        camera.getTransforms().addAll(rotateX, rotateY, rotateZ);
        Scene scene = new Scene(root, 800, 600, true);
        scene.setFill(Color.LIGHTBLUE);
        scene.setCamera(camera);
        stage.setTitle("Wheel Telemetry Viewer");
        stage.setScene(scene);
        stage.show();
        scene.setOnMousePressed(e -> {
            lastMouse[0] = e.getSceneX();
            lastMouse[1] = e.getSceneY();
        });
        scene.setOnMouseDragged(e -> {
            double dx = e.getSceneX() - lastMouse[0];
            double dy = e.getSceneY() - lastMouse[1];
            if (e.isPrimaryButtonDown()) {
                camera.setTranslateX(camera.getTranslateX() - dx * 0.5);
                camera.setTranslateY(camera.getTranslateY() - dy * 0.5);
            } else if (e.isMiddleButtonDown()) {
                rotateY.setAngle(rotateY.getAngle() + dx * 0.2);
                rotateX.setAngle(rotateX.getAngle() - dy * 0.2);
            }
            lastMouse[0] = e.getSceneX();
            lastMouse[1] = e.getSceneY();
        });
        scene.setOnScroll(e -> {
            camera.setTranslateZ(camera.getTranslateZ() + e.getDeltaY() * 0.5);
        });
        READY.countDown();
    }

    private Cylinder createWheel(double x, double y, PhongMaterial material) {
        Cylinder wheel = new Cylinder(40, 20);
        wheel.setMaterial(material);
        wheel.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));
        wheel.setTranslateX(x);
        wheel.setTranslateY(y);
        return wheel;
    }

    public static void updateWheels(double[] reds, double[] angles) {
        for (int i = 0; i < 4; i++) {
            double red = Math.max(0, Math.min(1.0, reds[i]));
            PhongMaterial mat = new PhongMaterial(Color.color(red, 0, 0));
            wheels[i].setMaterial(mat);
            wheels[i].getTransforms().removeIf(t -> t instanceof Rotate && ((Rotate)t).getAxis().equals(Rotate.X_AXIS));
            wheels[i].getTransforms().add(new Rotate(angles[i] - 90.0, Rotate.X_AXIS));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
