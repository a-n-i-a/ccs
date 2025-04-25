import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;

public class CCS {
    private int connectedClients = 0;
    private int computedRequests = 0;
    private int incorrectOperations = 0;
    private int sumOfComputedValues = 0;
    private ConcurrentHashMap<String, Integer> operationCounts = new ConcurrentHashMap<>();
    ServerSocket serverSocket = null;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Proper format: java CCS <port>");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
            if (port < 1024) {
                System.out.println("Port numbers below 1024 are reserved. Use a port number > 1024.");
                return;
            }
        } catch (NumberFormatException e) {
            System.err.println("Provided port is not a valid number.");
            return;
        }

        CCS ccs = new CCS();
        ccs.start(port);
    }

    public void start(int port) {
        try {
            DatagramSocket udpSocket = new DatagramSocket(port);
            System.out.println("UDP socket created for service discovery.");


            new Thread(() -> serviceDiscovery(udpSocket)).start();

            try {
                serverSocket = new ServerSocket(port);
                System.out.println("Socket created");
            } catch (IOException e) {
                System.out.println(e);
            }

            startStatisticsReporter();


            while(true) {
                Socket client = serverSocket.accept();
                synchronized (this) {
                    connectedClients++;
                }
                new Thread(() -> handleClient(client)).start();
            }


        } catch (IOException e) {
            System.err.println("Error starting the CCS server.");
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Received request: " + line);
                String response = processRequest(line);
                out.println(response);
            }
        } catch (IOException e) {
            System.out.println("Client " + clientSocket.getRemoteSocketAddress() + " disconnected");
        } finally {
            synchronized (this) {
                connectedClients--;
            }
        }
    }

    private String processRequest(String line) {
        String[] parts = line.split(" ");
        if (parts.length != 3) {
            synchronized (this) {
                incorrectOperations++;
            }
            return "ERROR";
        }

        String operation = parts[0];
        try {
            int arg1 = Integer.parseInt(parts[1]);
            int arg2 = Integer.parseInt(parts[2]);
            String result = handleArithmetic(operation, arg1, arg2);
            updateStatisticsReport(operation, result);
            return result;
        } catch (NumberFormatException e) {
            synchronized (this) {
                incorrectOperations++;
            }
            return "ERROR";
        }
    }

    private void updateStatisticsReport(String operation, String result) {
        synchronized (this) {
            if (!result.equals("ERROR")) {
                computedRequests++;
                sumOfComputedValues += Integer.parseInt(result);
                operationCounts.put(operation, operationCounts.getOrDefault(operation, 0) + 1);
            } else {
                incorrectOperations++;
            }
        }
    }

    public void serviceDiscovery(DatagramSocket socket) {
        try {
            System.out.println("Listening for service discovery on UDP port...");
            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String receivedData = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received UDP message: " + receivedData);

                if (receivedData.startsWith("CCS DISCOVER")) {
                    byte[] response = "CCS FOUND".getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(response, response.length, packet.getAddress(), packet.getPort());
                    socket.send(responsePacket);
                    System.out.println("Sent response: CCS FOUND");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startStatisticsReporter() {
        // TODO: Implement periodic statistics reporting
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Statistics Report:");
            System.out.println("Connected Clients: " + connectedClients);
            System.out.println("Total Requests: " + computedRequests);
            System.out.println("Invalid Requests: " + incorrectOperations);
            System.out.println("Sum of Results: " + sumOfComputedValues);
            operationCounts.forEach((key, value) -> System.out.println("Operation " + key + ": " + value));
        }, 10, 10, TimeUnit.SECONDS);
    }

    public String handleArithmetic(String operation, int arg1, int arg2) {
        try {
            switch (operation) {
                case "ADD":
                    return String.valueOf(arg1 + arg2);
                case "SUB":
                    return String.valueOf(arg1 - arg2);
                case "MUL":
                    return String.valueOf(arg1 * arg2);
                case "DIV":
                    if (arg2 == 0) return "ERROR";
                    return String.valueOf(arg1 / arg2);
                default:
                    return "ERROR";
            }
        } catch (Exception e) {
            return "ERROR";
        }
    }
}
