import javafx.application.Application;
import javafx.application.Platform;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

public class WolfRecieve {
    public static void main(String[] args) throws Exception {
        new Thread(() -> Application.launch(WheelScene.class), "FX-Launcher").start();

        // Wait deterministically, not with Thread.sleep
        WheelScene.READY.await();

        NetworkTableInstance inst = NetworkTableInstance.getDefault();
        inst.startClient4("TelemetryClient");
        inst.setServerTeam(9289);
        inst.startDSClient();

        NetworkTable t = inst.getTable("BotTelemetry");
        new WolfSend(); // <-- make this use getDefault() too

        java.util.concurrent.ScheduledExecutorService exec =
            java.util.concurrent.Executors.newSingleThreadScheduledExecutor();

        exec.scheduleAtFixedRate(() -> {
            double[] angles = {
                t.getEntry("LFD").getDouble(0.0),
                t.getEntry("LBD").getDouble(0.0),
                t.getEntry("RBD").getDouble(0.0),
                t.getEntry("RFD").getDouble(0.0)
            };
            double[] reds = {
                t.getEntry("LFR").getDouble(0.0),
                t.getEntry("LBR").getDouble(0.0),
                t.getEntry("RBR").getDouble(0.0),
                t.getEntry("RFR").getDouble(0.0)
            };

            Platform.runLater(() -> WheelScene.updateWheels(reds, angles));
        }, 0, 20, java.util.concurrent.TimeUnit.MILLISECONDS);

        // Optionally: hook stage close to exec.shutdownNow()
    }
}
