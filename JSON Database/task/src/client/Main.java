package client;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import server.Request;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Main extends JFrame {
    @Parameter(names = "-t")
    static String type;

    @Parameter(names = "-k")
    static String key;

    @Parameter(names = "-v")
    static String value;

    @Parameter(names = "-in")
    static String fileName;

    private static final String IPAddress = "127.0.0.1";
    private static final int port = 23456;

    public Main() {
        super("Client");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 800);
        setLocationRelativeTo(null);
        setLayout(null);
        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        Font font = new Font("ABOBA", Font.BOLD, 12);

        JTextArea responseTextArea = new JTextArea();
        responseTextArea.setBounds(20, 200, 545, 540);
        responseTextArea.setFont(font);
        responseTextArea.setFont(responseTextArea.getFont().deriveFont(20f));

        add(responseTextArea);

        JLabel requestLabel = new JLabel("Request:");
        requestLabel.setBounds(220, 5, 400, 50);
        requestLabel.setFont(font);
        requestLabel.setFont(requestLabel.getFont().deriveFont(30f));
        add(requestLabel);

        JTextArea requestTextArea = new JTextArea();
        requestTextArea.setBounds(20, 60, 545, 30);
        requestTextArea.setFont(font);
        requestTextArea.setFont(requestTextArea.getFont().deriveFont(20f));
        add(requestTextArea);

        JButton sendButton = new JButton("Send");
        sendButton.setBackground(Color.WHITE);
        sendButton.setFont(font);
        sendButton.setFont(sendButton.getFont().deriveFont(30f));
        sendButton.setBounds(230, 100, 120, 50);
        sendButton.addActionListener(e -> {
            try {
                Socket socket = new Socket(InetAddress.getByName(IPAddress), port);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                output.writeUTF(requestTextArea.getText());

                String response = input.readUTF();
                responseTextArea.setText("Received: " + response);
                socket.close();
            } catch (IOException ignored) {
            }
        });
        add(sendButton);

        JLabel responseLabel = new JLabel("Response:");
        responseLabel.setBounds(210, 150, 400, 50);
        responseLabel.setFont(font);
        responseLabel.setFont(responseLabel.getFont().deriveFont(30f));
        add(responseLabel);
    }

    public static void main(String[] args) {
        try {
            System.out.println("Client started!");
            Socket socket = new Socket(InetAddress.getByName(IPAddress), port);
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            Main main = new Main();
            JCommander.newBuilder()
                    .addObject(main)
                    .build()
                    .parse(args);

            Request request;
            String JSONRequest;

            if (fileName != null) {
                File JSONFile = new File("C:\\Users\\BotMachine\\IdeaProjects\\JSON Database\\JSON Database\\task\\src\\client\\data\\" + fileName);
                Scanner fileScanner = new Scanner(JSONFile);
                JSONRequest = fileScanner.nextLine();

            } else {
                request = new Request(type, key, value);
                Gson gson = new Gson();
                JSONRequest = gson.toJson(request);
            }

            output.writeUTF(JSONRequest);
            System.out.println("Sent: " + JSONRequest);

            String response = input.readUTF();
            System.out.println("Received: " + response);
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
