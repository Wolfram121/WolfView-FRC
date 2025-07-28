import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

public class WolfRecieve {
    public static void main(String[] args) throws InterruptedException {
        NetworkTableInstance inst = NetworkTableInstance.create();
        inst.startClient4("TelemetryClient");
        inst.setServerTeam(9289); // Replace with your FRC team number
        inst.startDSClient();

        NetworkTable t = inst.getTable("BotTelemetry");

        double[] modules = new double[8];
        WheelScene.launch(args);

        while (true) {
            modules[0] = t.getEntry("LFD").getDouble(0.0);
            modules[1] = t.getEntry("LBD").getDouble(0.0);
            modules[2] = t.getEntry("RBD").getDouble(0.0);
            modules[3] = t.getEntry("RFD").getDouble(0.0);
            modules[4] = t.getEntry("LFR").getDouble(0.0);
            modules[5] = t.getEntry("LBR").getDouble(0.0);
            modules[6] = t.getEntry("RBR").getDouble(0.0);
            modules[7] = t.getEntry("RFR").getDouble(0.0);
            String status = t.getEntry("status").getString("N/A");
            WheelScene.updateWheels(
                modules[0], modules[1], modules[2], modules[3],
                modules[4], modules[5], modules[6], modules[7]
            );
            Thread.sleep(10);
        }
    }
}