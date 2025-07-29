import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import java.util.Random;

public class WolfSend {
    public WolfSend() {
        Thread senderThread = new Thread(() -> {
            NetworkTableInstance inst = NetworkTableInstance.getDefault();
            inst.startClient4("TelemetryClient");
            inst.setServerTeam(9289);
            inst.startDSClient();
            NetworkTable t = inst.getTable("BotTelemetry");
            Random rand = new Random();
            while (true) {
                t.getEntry("LFD").setDouble(0);
                t.getEntry("LBD").setDouble(45);
                t.getEntry("RBD").setDouble(90);
                t.getEntry("RFD").setDouble(rand.nextDouble() * 360);
                t.getEntry("LFR").setDouble(rand.nextDouble());
                t.getEntry("LBR").setDouble(rand.nextDouble());
                t.getEntry("RBR").setDouble(rand.nextDouble());
                t.getEntry("RFR").setDouble(rand.nextDouble());
                t.getEntry("status").setString(rand.nextBoolean() ? "OK" : "WARN");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        senderThread.setDaemon(true);
        senderThread.start();
    }
}
