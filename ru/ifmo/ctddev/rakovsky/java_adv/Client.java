package ru.ifmo.ctddev.rakovsky.java_adv;


import java.io.*;
import java.net.Socket;

/**
 * @author Rakovsky Alexander
 * @version 1.0
 *
 * client for chat
 */
public class Client implements Runnable {

    private Socket socket;
    private Thread thread;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private ClientThread client;
    private String username;

    /**
     * thread for chat client
     */
    private class ClientThread extends Thread {
        private Socket socket;
        private Client client;
        private DataInputStream dataInputStream;

        /**
         * constructor which binds thread to client and socket
         *
         * @param client our client
         * @param socket socket
         */
        public ClientThread(Client client, Socket socket) {
            this.client = client;
            this.socket = socket;
            open();
            start();
        }

        /**
         * opening input stream
         */
        public void open() {
            try {
                dataInputStream = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                client.stop();
            }
        }

        /**
         * closing input stream
         */
        public void close() {
            try {
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * printing input messages
         */
         public void run() {
            while (true) {
                try {
                    client.handle(dataInputStream.readUTF());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * constructor which initializes socket and starting connection
     *
     * @param server server address
     * @param port server port
     */
    public Client(String server, int port, String username) {
        try {
            this.username = username;
            socket = new Socket(server, port);
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * method which reads messages from client console, sends it to the server and then it is translated to all connected clients
     */
    @Override
    public void run() {
        while (thread != null) {
            try {
                dataOutputStream.writeUTF(username + ": " + dataInputStream.readLine());
                dataOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
                stop();
            }
        }
    }

    /**
     * check message for exit command or print received message
     *
     * @param st received message
     */
    public void handle(String st) {
        if (st.contains("!exit")) {
            stop();
        } else {
            System.out.println(st);
        }
    }

    /**
     * start new client thread and open its streams
     */
    public void start() {
        try {
            dataInputStream = new DataInputStream(System.in);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            if (thread == null) {
                client = new ClientThread(this, socket);
                thread = new Thread(this);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * stop this client and disconnect it from server
     */
    public void stop() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
        try {
            if (dataInputStream != null) {
                dataInputStream.close();
            }
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        client.close();
        client.stop();
    }

    public static void main(String[] args) {
        Client client = new Client(args[0], Integer.parseInt(args[1]), args[2]);
    }
}
