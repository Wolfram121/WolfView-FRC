import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

public class WheelScene extends Application {

    private double cameraAngleX = 180; // initial downward tilt
    private double cameraAngleY = 0; // initial sideways pan
    private double camerAngleZ = 0;
    private final Rotate rotateX = new Rotate(cameraAngleX, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(cameraAngleY, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(camerAngleZ, Rotate.Z_AXIS);

    private final double[] lastMousePosition = new double[2];

    private static Cylinder[] wheels = new Cylinder[4];

    @Override
    public void start(Stage primaryStage) {
        Group root = new Group();

        // Create material for the wheels
        PhongMaterial wheelMaterial = new PhongMaterial(Color.DARKGRAY);

        // Create four wheels and position them in a square
        double spacing = 100;
        wheels[0] = createWheel(-spacing, -spacing, wheelMaterial);
        wheels[1] = createWheel(spacing, -spacing, wheelMaterial);
        wheels[2] = createWheel(-spacing, spacing, wheelMaterial);
        wheels[3] = createWheel(spacing, spacing, wheelMaterial);

        root.getChildren().addAll(wheels);

        // Camera setup
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(50);
        camera.setTranslateZ(300);
        camera.getTransforms().addAll(rotateX, rotateY, rotateZ);

        Scene scene = new Scene(root, 800, 600, true);
        scene.setFill(Color.LIGHTBLUE);
        scene.setCamera(camera);

        primaryStage.setTitle("Wheel Square Simulation");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Animation for rotating wheels
        // new AnimationTimer() {
        // @Override
        // public void handle(long now) {
        // for (Cylinder wheel : wheels) {
        // wheel.setRotate(wheel.getRotate() + 1);
        // }
        // }
        // }.start();
        final double[] lastMousePosition = new double[2];

        scene.setOnMousePressed(e -> {
            lastMousePosition[0] = e.getSceneX();
            lastMousePosition[1] = e.getSceneY();
        });
        scene.setOnMouseDragged(e -> {
            double dx = e.getSceneX() - lastMousePosition[0];
            double dy = e.getSceneY() - lastMousePosition[1];

            if (e.isPrimaryButtonDown()) {
                // Left mouse: pan
                camera.setTranslateX(camera.getTranslateX() - dx * 0.5);
                camera.setTranslateY(camera.getTranslateY() - dy * 0.5);
            } else if (e.isMiddleButtonDown()) {
                // Middle mouse: rotate
                cameraAngleY += dx * 0.2;
                cameraAngleX -= dy * 0.2;

                rotateX.setAngle(cameraAngleX);
                rotateY.setAngle(cameraAngleY);
            }

            lastMousePosition[0] = e.getSceneX();
            lastMousePosition[1] = e.getSceneY();
        });
        scene.setOnScroll(e -> {
            double zoomFactor = 20; // tweak sensitivity here
            double delta = e.getDeltaY();

            camera.setTranslateZ(camera.getTranslateZ() + delta * zoomFactor / 40);
        });
    }

    private Cylinder createWheel(double x, double y, PhongMaterial material) {
        Cylinder wheel = new Cylinder(40, 20);
        wheel.setMaterial(material);
        wheel.getTransforms().addAll(
                new Translate(x, y, 0)
        // new Rotate(90, Rotate.X_AXIS) // Rotated to lie flat
        );
        return wheel;
    }

    public static void updateWheels(double r1, double r2, double r3, double r4, double angle1, double angle2, double angle3, double angle4) {
        double[] redValues = {r1, r2, r3, r4};
        double[] angles = {angle1, angle2, angle3, angle4};

        for (int i = 0; i < 4; i++) {
            // Clamp red color between 0 and 1
            double red = Math.max(0, Math.min(1.0, redValues[i]));
            PhongMaterial mat = new PhongMaterial();
            mat.setDiffuseColor(Color.color(red, 0, 0));

            wheels[i].setMaterial(mat);

            // Apply rotation around Z axis
            wheels[i].setRotationAxis(Rotate.Z_AXIS);
            wheels[i].setRotate(angles[i]);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}