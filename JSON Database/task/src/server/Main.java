package server;

import com.google.gson.Gson;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Main {

    private static final String IPAddress = "127.0.0.1";
    private static final int port = 23456;

    private static final File JSONDataBase = new File("C:\\Users\\BotMachine\\IdeaProjects\\JSON Database\\JSON Database\\task\\src\\server\\data\\db.json");
    private static final Map<String, String> JSONRecords = new HashMap<>();

    private static ServerSocket server;
    private static DataOutputStream output;

    private static boolean serverIsRunning;

    private static final ReadWriteLock lock = new ReentrantReadWriteLock();
    private static final Lock readLock = lock.readLock();

    private static final Gson gson = new Gson();

    private static final int poolSize = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService executor = Executors.newFixedThreadPool(poolSize);

    enum CommandEnum {
        SET,
        GET,
        DELETE;

        static CommandEnum getCommandByString(String str) {
            switch (str) {
                case "set":
                    return SET;
                case "get":
                    return GET;
                default:
                    return DELETE;
            }
        }
    }


    public static void main(String[] args) {
        try {
            server = new ServerSocket(port, 50, InetAddress.getByName(IPAddress));
            System.out.println("Server started!");

            serverIsRunning = true;
            while (serverIsRunning) {
                Socket socket = server.accept();
                DataInputStream input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());

                String JSONRequest = input.readUTF();


                executor.submit(() -> executeRequest(JSONRequest));
            }

            executor.shutdown();
        } catch (IOException ignored) {
        }
    }

    private static synchronized void executeRequest(String JSONRequest) {
        readLock.lock();
        readRecordsFromFile();
        JSON json = new JSON(JSONRequest);
        try {
            Response response;
            String JSONResponse;
            String type = json.getValueByKeys("type");
            if (type.equals("exit")) {
                response = new Response("OK", null, null);
                JSONResponse = gson.toJson(response);
                output.writeUTF(JSONResponse);
                server.close();
                serverIsRunning = false;
            } else {
                String[] keys = JSON.getValuesFromArray(json.getValueByKeys("key"));
                String[] newKeys = new String[keys.length - 1];
                System.arraycopy(keys, 1, newKeys, 0, keys.length - 1);
                String key = keys[0];
                String value;

                CommandEnum commandEnum = CommandEnum.getCommandByString(type);
                switch (commandEnum) {
                    case GET:
                        if (JSONRecords.containsKey(key)) {
                            json = new JSON(JSONRecords.get(key));
                            json.deleteByKeys("key");
                            if (newKeys.length == 0) {
                                value = json.getValueByKeys("value");
                            } else {
                                value = json.getValueByKeys(newKeys);
                            }
                            JSON json1;
                            if (value.charAt(0) == '{') {
                                json1 = new JSON("{\"response\":\"OK\",\"value\":\"empty\"}");
                                json1.setSingleValueByKeys(new String[]{"value"}, value);
                            } else {
                                json1 = new JSON("{\"response\":\"OK\",\"value\":\"empty\"}");
                                json1.setValuesByKeys(new String[]{"value"}, value);
                            }

                            output.writeUTF(json1.getJson());
                        } else {
                            response = new Response("ERROR", "No such key", null);
                            JSONResponse = gson.toJson(response);
                            output.writeUTF(JSONResponse);
                        }
                        break;
                    case SET:
                        json.deleteByKeys("type");
                        if (JSONRecords.containsKey(key)) {
                            JSON json1 = new JSON(JSONRecords.get(key));
                            value = json.getValueByKeys("value");
                            json1.setValuesByKeys(newKeys, value);
                            JSONRecords.put(key, json1.getJson());
                        } else {
                            JSONRecords.put(key, json.getJson());
                        }
                        response = new Response("OK", null, null);
                        JSONResponse = gson.toJson(response);
                        output.writeUTF(JSONResponse);
                        break;
                    default:
                        if (JSONRecords.containsKey(key) && newKeys.length != 1) {
                            JSON json1 = new JSON(JSONRecords.get(key));
                            json1.deleteByKeys(newKeys);
                            JSONRecords.put(key, json1.getJson());
                            response = new Response("OK", null, null);
                        } else if (newKeys.length == 1) {
                            JSONRecords.remove(key);
                            response = new Response("OK", null, null);
                        } else {
                            response = new Response("ERROR", "No such key", null);
                        }
                        JSONResponse = gson.toJson(response);
                        output.writeUTF(JSONResponse);
                        break;
                }

            }
        } catch (IOException ignored) {
        }
        writeRecordsToFile();
        readLock.unlock();
    }

    private static void writeRecordsToFile() {
        try (PrintWriter printWriter = new PrintWriter(JSONDataBase)) {
            for (var entry :
                    JSONRecords.entrySet()) {
                printWriter.println(entry.getValue());
            }
        } catch (FileNotFoundException ignored) {
        }
    }

    private static void readRecordsFromFile() {
        JSONRecords.clear();
        try (Scanner fileScanner = new Scanner(JSONDataBase)) {
            while (fileScanner.hasNextLine()) {
                String JSONRecord = fileScanner.nextLine();
                JSON json = new JSON(JSONRecord);
                String key = json.getValueByKeys("key");
                JSONRecords.put(key, JSONRecord);
            }
        } catch (FileNotFoundException ignored) {
        }
    }
}
