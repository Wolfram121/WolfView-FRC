// JavaFX and 3D graphics imports
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Line;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

public class WolfScene extends Application {
    // Used for synchronizing with other threads if needed
    static final java.util.concurrent.CountDownLatch READY = new java.util.concurrent.CountDownLatch(1);

    // Rotations used to manipulate the camera
    private final Rotate rotateX = new Rotate(180, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);

    // Used for tracking mouse drag deltas (not currently used in this code)
    private final double[] lastMouse = new double[2];

    // Four wheel objects for display
    private static final Cylinder[] wheels = new Cylinder[4];

    // Distance between wheels
    private static final double SPACING = 100;

    @Override
    public void start(Stage stage) {
        // Root node for the scene graph
        Group root = new Group();

        // Material for the wheels (dark gray)
        PhongMaterial wheelMaterial = new PhongMaterial(Color.DARKGRAY);

        // Create 4 wheels and place them in the scene
        wheels[0] = createWheel(-SPACING, SPACING, wheelMaterial);   // Front Left
        wheels[1] = createWheel(-SPACING, -SPACING, wheelMaterial);  // Back Left
        wheels[2] = createWheel(SPACING, -SPACING, wheelMaterial);   // Back Right
        wheels[3] = createWheel(SPACING, SPACING, wheelMaterial);    // Front Right

        // Add the wheels to the scene graph
        root.getChildren().addAll(wheels);

        // Add 3D grid planes to provide visual reference
        root.getChildren().addAll(
            createGridPlane("XY", 1000, 100, Color.GRAY),
            createGridPlane("XZ", 1000, 100, Color.LIGHTGRAY),
            createGridPlane("YZ", 1000, 100, Color.LIGHTGRAY)
        );

        // Setup perspective camera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000);
        camera.setFieldOfView(50);

        // Initial camera position
        Translate cameraTranslate = new Translate(0, 0, 300);
        camera.getTransforms().add(cameraTranslate);
        camera.getTransforms().addAll(rotateX, rotateY, rotateZ);

        // Set up the scene with depth buffer enabled (true)
        Scene scene = new Scene(root, 800, 600, true);
        scene.setFill(Color.LIGHTBLUE); // Background color
        scene.setCamera(camera);

        // Configure the stage (window)
        stage.setTitle("Wheel Telemetry Viewer");
        stage.setScene(scene);
        stage.show();

        // Keyboard controls for moving and rotating the camera
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case Q -> cameraTranslate.setZ(cameraTranslate.getZ() + 10);   // Zoom out
                case E -> cameraTranslate.setZ(cameraTranslate.getZ() - 10);   // Zoom in
                case W -> cameraTranslate.setY(cameraTranslate.getY() + 10);   // Move up
                case S -> cameraTranslate.setY(cameraTranslate.getY() - 10);   // Move down
                case A -> cameraTranslate.setX(cameraTranslate.getX() - 10);   // Move left
                case D -> cameraTranslate.setX(cameraTranslate.getX() + 10);   // Move right

                // NUMPAD2: Front view
                case NUMPAD2 -> {
                    resetCameraTransforms(camera, cameraTranslate);
                    rotateX.setAngle(-112.5);
                    cameraTranslate.setZ(100);
                    cameraTranslate.setY(-300);
                    logTrans(cameraTranslate);
                }

                // NUMPAD4: Left side view
                case NUMPAD4 -> {
                    resetCameraTransforms(camera, cameraTranslate);
                    rotateY.setAngle(112.5);
                    rotateZ.setAngle(-90);
                    cameraTranslate.setX(-300);
                    cameraTranslate.setZ(100);
                }

                // NUMPAD5: Top-down view (default)
                case NUMPAD5 -> {
                    resetCameraTransforms(camera, cameraTranslate);
                    rotateX.setAngle(180);
                    cameraTranslate.setZ(300);
                    logTrans(cameraTranslate);
                }

                // NUMPAD6: Right side view
                case NUMPAD6 -> {
                    resetCameraTransforms(camera, cameraTranslate);
                    rotateY.setAngle(-112.5);
                    rotateZ.setAngle(90);
                    cameraTranslate.setX(300);
                    cameraTranslate.setZ(100);
                }

                // NUMPAD8: Rear view
                case NUMPAD8 -> {
                    resetCameraTransforms(camera, cameraTranslate);
                    rotateX.setAngle(112.5);
                    rotateZ.setAngle(180);
                    cameraTranslate.setZ(100);
                    cameraTranslate.setY(300);
                    logTrans(cameraTranslate);
                }

                default -> {} // Do nothing for other keys
            }
        });

        // Indicate the scene is ready
        READY.countDown();
    }

    // Creates a cylinder to represent a wheel at a given X/Y location
    private Cylinder createWheel(double x, double y, PhongMaterial material) {
        Cylinder wheel = new Cylinder(40, 20); // radius 40, height 20
        wheel.setMaterial(material);
        wheel.getTransforms().add(new Rotate(90, Rotate.Y_AXIS)); // lay wheel flat
        wheel.setTranslateX(x);
        wheel.setTranslateY(y);
        return wheel;
    }

    // Updates the wheels' color (based on red intensity) and rotation (angle)
    public static void updateWheels(double[] reds, double[] angles) {
        for (int i = 0; i < 4; i++) {
            double red = Math.max(0, Math.min(1.0, reds[i])); // clamp red to [0, 1]
            PhongMaterial mat = new PhongMaterial(Color.color(red, 0, 0));
            wheels[i].setMaterial(mat);

            // Remove old X-axis rotations
            wheels[i].getTransforms().removeIf(t -> t instanceof Rotate && ((Rotate) t).getAxis().equals(Rotate.X_AXIS));

            // Add new X-axis rotation for wheel orientation
            wheels[i].getTransforms().add(new Rotate(angles[i] - 90.0, Rotate.X_AXIS));
        }
    }

    // Creates a visual grid plane (XY, XZ, or YZ)
    private Group createGridPlane(String axis, double size, int divisions, Color color) {
        Group grid = new Group();
        double spacing = size / divisions;

        for (int i = -divisions / 2; i <= divisions / 2; i++) {
            Line line1, line2;

            switch (axis) {
                case "XY" -> {
                    line1 = new Line(-size / 2, i * spacing, size / 2, i * spacing);
                    line2 = new Line(i * spacing, -size / 2, i * spacing, size / 2);
                }
                case "XZ" -> {
                    line1 = new Line(-size / 2, 0, size / 2, 0);
                    line2 = new Line(i * spacing, 0, i * spacing, 0);
                    line1.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
                    line2.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
                    line1.setTranslateZ(i * spacing);
                    line2.setTranslateZ(-size / 2 + i * spacing);
                }
                case "YZ" -> {
                    line1 = new Line(-size / 2, 0, size / 2, 0);
                    line2 = new Line(i * spacing, 0, i * spacing, 0);
                    line1.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));
                    line2.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));
                    line1.setTranslateX(i * spacing);
                    line2.setTranslateX(-size / 2 + i * spacing);
                }
                default -> {
                    return grid;
                }
            }

            // Set appearance of grid lines
            line1.setStroke(color);
            line2.setStroke(color);
            line1.setStrokeWidth(0.25);
            line2.setStrokeWidth(0.25);

            grid.getChildren().addAll(line1, line2);
        }
        return grid;
    }

    // Reset all camera transformations
    private void resetCameraTransforms(PerspectiveCamera camera, Translate cameraTranslate) {
        camera.getTransforms().clear();
        camera.getTransforms().addAll(cameraTranslate, rotateX, rotateY, rotateZ);

        // Reset position
        cameraTranslate.setX(0);
        cameraTranslate.setY(0);
        cameraTranslate.setZ(300);

        // Reset rotation
        rotateX.setAngle(0);
        rotateY.setAngle(0);
        rotateZ.setAngle(0);
    }

    // Logs camera rotation and translation (for debugging)
    private void logTrans(Translate cameraTranslate) {
        System.out.println(rotateX.getAngle() + " " + rotateY.getAngle() + " " + rotateZ.getAngle());
        System.out.println(cameraTranslate.getX() + " " + cameraTranslate.getY() + " " + cameraTranslate.getZ());
    }

    public static void main(String[] args) {
        launch(args); // Launch JavaFX application
    }
}
