import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONArray;
import org.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import javafx.application.Application;
import javafx.application.Platform;

import java.util.List;

//Config structure
public class Config {
    public String tableName; // maps to "table-name"
    public List<String> angles;
    public List<String> reds;
    public int teamNumber;   // maps to "team-number"
}


public class WolfRecieve {
    static JSONArray jsonArray = new JSONArray();
    static long lastWriteTime = System.currentTimeMillis();
    static String FILE_PATH;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: run <TYPE: 1=live, 2=replay> <TYPE2: 1=send, 0=nosend>");
            return;
        }
        final int TYPE = Integer.parseInt(args[0]);
        final int TYPE2 = Integer.parseInt(args[1]);
        
        if (TYPE != 3) {
            new Thread(() -> Application.launch(WolfScene.class), "FX-Launcher").start();
            WolfScene.READY.await();
        }

        if (TYPE == 1) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"));
            FILE_PATH = "./records/" + timestamp + ".json";

            NetworkTableInstance inst = NetworkTableInstance.getDefault();

            
            //Read config.json
            ObjectMapper mapper = new ObjectMapper();
            Config config = mapper.readValue(new File("config.json"), Config.class);



            inst.startClient4("TelemetryClient");
            inst.setServerTeam(9289);
            inst.startDSClient();
            NetworkTable t = inst.getTable(config.tableName); //Get NT table as configured in config.json
            if (TYPE2 == 1) {
                new WolfSend();
            }

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
                        t.getEntry(config.angles.get(0)).getDouble(0.0),
                        t.getEntry(config.angles.get(1)).getDouble(0.0),
                        t.getEntry(config.angles.get(2)).getDouble(0.0),
                        t.getEntry(config.angles.get(3)).getDouble(0.0)
                };
                double[] reds = {
                        t.getEntry(config.reds.get(0)).getDouble(0.0),
                        t.getEntry(config.reds.get(1)).getDouble(0.0),
                        t.getEntry(config.reds.get(2)).getDouble(0.0),
                        t.getEntry(config.reds.get(3)).getDouble(0.0)
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
            final String INPUT_PATH = "./records/" + args[2] + ".json";
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
        } else if (TYPE == 3) {
            File recordsDir = new File("./records");
            if (!recordsDir.exists() || !recordsDir.isDirectory()) {
                System.out.println("Records directory does not exist or is not a directory.");
                return;
            }

            String[] files = recordsDir.list((dir, name) -> name.endsWith(".json"));
            if (files == null || files.length == 0) {
                System.out.println("No JSON files found in records directory.");
                return;
            }

            System.out.println("Available record files:");
            for (String file : files) {
                System.out.println(" - " + file);
            }
        }
    }
}