import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import javafx.application.Application;
import javafx.application.Platform;

public class WolfRecieve {
    static JSONArray jsonArray = new JSONArray();
    static long lastWriteTime = System.currentTimeMillis();
    static String FILE_PATH;

    public static void main(String[] args) throws Exception {
        final int TYPE = 2;
        final String INPUT_PATH = "./records/" + "29-07-2025_23-00-18" + ".json";
        
        new Thread(() -> Application.launch(WolfScene.class), "FX-Launcher").start();
        WolfScene.READY.await();

        if (TYPE == 1) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"));
            FILE_PATH = "./records/" + timestamp + ".json";

            NetworkTableInstance inst = NetworkTableInstance.getDefault();
            inst.startClient4("TelemetryClient");
            inst.setServerTeam(9289);
            inst.startDSClient();
            NetworkTable t = inst.getTable("BotTelemetry");
            new WolfSend();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try (FileWriter fw = new FileWriter(FILE_PATH)) {
                    fw.write(jsonArray.toString(4));
                    // System.out.println("Final data written to " + FILE_PATH);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));

            java.util.concurrent.ScheduledExecutorService exec = java.util.concurrent.Executors
                    .newSingleThreadScheduledExecutor();

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

                JSONObject entry = new JSONObject();
                entry.put("angles", new JSONArray(angles));
                entry.put("reds", new JSONArray(reds));
                jsonArray.put(entry);

                // Periodic write to file
                long now = System.currentTimeMillis();
                if (now - lastWriteTime > 1000) {
                    try (FileWriter fw = new FileWriter(FILE_PATH)) {
                        fw.write(jsonArray.toString(4));
                        lastWriteTime = now;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                Platform.runLater(() -> WolfScene.updateWheels(reds, angles));
            }, 0, 20, java.util.concurrent.TimeUnit.MILLISECONDS);
        } else if (TYPE == 2) {
            JSONArray replayArray;
            try {
                String content = java.nio.file.Files.readString(java.nio.file.Paths.get(INPUT_PATH));
                replayArray = new JSONArray(content);
            } catch (Exception e) {
                System.out.println("Failed to parse file: " + e.getMessage());
                return;
            }

            java.util.concurrent.ScheduledExecutorService replayExec = java.util.concurrent.Executors
                    .newSingleThreadScheduledExecutor();

            final int[] index = { 0 };

            replayExec.scheduleAtFixedRate(() -> {
                if (index[0] >= replayArray.length()) {
                    replayExec.shutdown();
                    System.out.println("Replay finished.");
                    return;
                }

                JSONObject entry = replayArray.getJSONObject(index[0]++);
                JSONArray anglesArr = entry.getJSONArray("angles");
                JSONArray redsArr = entry.getJSONArray("reds");

                double[] angles = new double[anglesArr.length()];
                double[] reds = new double[redsArr.length()];

                for (int i = 0; i < angles.length; i++) {
                    angles[i] = anglesArr.getDouble(i);
                }
                for (int i = 0; i < reds.length; i++) {
                    reds[i] = redsArr.getDouble(i);
                }

                Platform.runLater(() -> WolfScene.updateWheels(reds, angles));
            }, 0, 20, java.util.concurrent.TimeUnit.MILLISECONDS);
        }
    }
}