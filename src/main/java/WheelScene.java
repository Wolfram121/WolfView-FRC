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
        wheels[0] = createWheel(-SPACING, SPACING, wheelMaterial);
        wheels[1] = createWheel(-SPACING, -SPACING, wheelMaterial);
        wheels[2] = createWheel(SPACING, -SPACING, wheelMaterial);
        wheels[3] = createWheel(SPACING, SPACING, wheelMaterial);
        root.getChildren().addAll(wheels);
        root.getChildren().addAll(
            createGridPlane("XY", 1000, 100, Color.GRAY),
            createGridPlane("XZ", 1000, 100, Color.LIGHTGRAY),
            createGridPlane("YZ", 1000, 100, Color.LIGHTGRAY)
        );
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000);
        camera.setFieldOfView(50);
        Translate cameraTranslate = new Translate(0, 0, 300); // initial Z = 300
        camera.getTransforms().add(cameraTranslate);
        camera.getTransforms().addAll(rotateX, rotateY, rotateZ);
        Scene scene = new Scene(root, 800, 600, true);
        scene.setFill(Color.LIGHTBLUE);
        scene.setCamera(camera);
        stage.setTitle("Wheel Telemetry Viewer");
        stage.setScene(scene);
        stage.show();
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case Q -> cameraTranslate.setZ(cameraTranslate.getZ() + 10);
                case E -> cameraTranslate.setZ(cameraTranslate.getZ() - 10);
                case W -> cameraTranslate.setY(cameraTranslate.getY() + 10);
                case S -> cameraTranslate.setY(cameraTranslate.getY() - 10);
                case A -> cameraTranslate.setX(cameraTranslate.getX() - 10);
                case D -> cameraTranslate.setX(cameraTranslate.getX() + 10);
                case NUMPAD2 -> {
                    resetCameraTransforms(camera, cameraTranslate);
                    rotateX.setAngle(-112.5);
                    cameraTranslate.setZ(100);
                    cameraTranslate.setY(-300);
                    logTrans(cameraTranslate); 
                }
                case NUMPAD4 -> {
                    resetCameraTransforms(camera, cameraTranslate);
                    rotateY.setAngle(112.5);
                    rotateZ.setAngle(-90);
                    cameraTranslate.setX(-300);
                    cameraTranslate.setZ(100);
                }
                case NUMPAD5 -> {
                    resetCameraTransforms(camera, cameraTranslate);
                    rotateX.setAngle(180);
                    cameraTranslate.setZ(300);
                    logTrans(cameraTranslate);
                }
                case NUMPAD6 -> {
                    resetCameraTransforms(camera, cameraTranslate);
                    rotateY.setAngle(-112.5);
                    rotateZ.setAngle(90);
                    cameraTranslate.setX(300);
                    cameraTranslate.setZ(100);
                }
                case NUMPAD8 -> {
                    resetCameraTransforms(camera, cameraTranslate);
                    rotateX.setAngle(112.5);
                    rotateZ.setAngle(180);
                    cameraTranslate.setZ(100);
                    cameraTranslate.setY(300);
                    logTrans(cameraTranslate);
                }
                default -> {}
            }
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
            wheels[i].getTransforms().removeIf(t -> t instanceof Rotate && ((Rotate) t).getAxis().equals(Rotate.X_AXIS));
            wheels[i].getTransforms().add(new Rotate(angles[i] - 90.0, Rotate.X_AXIS));
        }
    }

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

            line1.setStroke(color);
            line2.setStroke(color);
            line1.setStrokeWidth(0.25);
            line2.setStrokeWidth(0.25);
            grid.getChildren().addAll(line1, line2);
        }
        return grid;
    }

    private void resetCameraTransforms(PerspectiveCamera camera, Translate cameraTranslate) {
        camera.getTransforms().clear();
        camera.getTransforms().addAll(
            cameraTranslate,
            rotateX,
            rotateY,
            rotateZ
        );
        cameraTranslate.setX(0);
        cameraTranslate.setY(0);
        cameraTranslate.setZ(300);

        rotateX.setAngle(0);
        rotateY.setAngle(0);
        rotateZ.setAngle(0);
    }

    private void logTrans(Translate cameraTranslate) {
        System.out.println(rotateX.getAngle() + " " + rotateY.getAngle() + " " + rotateZ.getAngle() + "\n" +
            cameraTranslate.getX() + " " + cameraTranslate.getY() + " " + cameraTranslate.getZ());
    }

    public static void main(String[] args) {
        launch(args);
    }
}