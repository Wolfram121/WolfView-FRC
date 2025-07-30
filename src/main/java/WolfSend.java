import java.io.File;
import java.util.List;
import java.util.Random;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

//Config structure
public class Config {
    public String tableName; // maps to "table-name"
    public List<String> angles;
    public List<String> reds;
    public int teamNumber;   // maps to "team-number"
}
public class WolfSend {
    public WolfSend() {

        Thread senderThread = new Thread(() -> {
            // Get the default instance of NetworkTables
            NetworkTableInstance inst = NetworkTableInstance.getDefault();
            
            // Start a NetworkTables client named "TelemetryClient" and server

            //Read config.json
            ObjectMapper mapper = new ObjectMapper();
            Config config = mapper.readValue(new File("config.json"), Config.class);

            inst.startClient4("TelemetryClient");
            inst.setServerTeam(9289);
            inst.startDSClient();
            NetworkTable t = inst.getTable(config.tableName); // Get NT table as configured in config.json

            // Create a random number generator for simulating fake data
            Random rand = new Random();

            // Continuously update NetworkTable entries
            while (true) {
                // Simulate module angles and drive speeds (example keys)
                t.getEntry(config.angles.get(0)).setDouble(0);
                t.getEntry(config.angles.get(1)).setDouble(0);
                t.getEntry(config.angles.get(2)).setDouble(0);
                t.getEntry(config.angles.get(3)).setDouble(rand.nextDouble() * 360);

                // Simulate rotation/velocity values with random data
                t.getEntry(config.reds.get(0)).setDouble(rand.nextDouble());        // Left Front Rotation
                t.getEntry(config.reds.get(1)).setDouble(rand.nextDouble());        // Left Back Rotation
                t.getEntry(config.reds.get(2)).setDouble(rand.nextDouble());        // Right Back Rotation
                t.getEntry(config.reds.get(3)).setDouble(rand.nextDouble());        // Right Front Rotation

                // Randomly toggle between "OK" and "WARN" status strings
                t.getEntry("status").setString(rand.nextBoolean() ? "OK" : "WARN");

                try {
                    // Wait 20 milliseconds before next update (approx. 50 Hz)
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    // If interrupted (e.g., program shutdown), break the loop
                    break;
                }
            }
        });

        // Mark the sender thread as a daemon so it won’t block program shutdown
        senderThread.setDaemon(true);

        // Start the background thread
        senderThread.start();
    }
}
