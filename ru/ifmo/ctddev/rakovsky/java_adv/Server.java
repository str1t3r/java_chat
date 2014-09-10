package ru.ifmo.ctddev.rakovsky.java_adv;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * @author Rakovsky Alexander
 * @version 1.0
 *
 * Chat server
 *
 * @see java.lang.Runnable
 */
public class Server implements Runnable {

    private ServerSocket server;
    private Thread thread;
    private ArrayList<ServerThread> clients;

    /**
     * Thread for 1 client
     */
    private class ServerThread extends Thread {
        private Server server;
        private Socket socket;
        private int id;
        private DataInputStream dataInputStream;
        private DataOutputStream dataOutputStream;

        /**
         * Constructor initializes the fields in the class
         *
         * @param server chat server
         * @param socket socket
         */
        public ServerThread(Server server, Socket socket) {
            this.server = server;
            this.socket = socket;
            id = socket.getPort();
        }

        /**
         * send message to the client
         * @param st sending message
         */
        public void send(String st) {
            try {
                dataOutputStream.writeUTF(st);
                dataOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
                server.remove(id);
                stop();
            }
        }

        /**
         * return thread's id of this client
         *
         * @return this client id
         */
        public int getIdd() {
            return id;
        }


        /**
         * run thread
         */
        public void run() {
            while (true) {
                try {
                    server.handle(id, dataInputStream.readUTF());
                } catch (IOException e) {
                    e.printStackTrace();
                    server.remove(id);
                    stop();
                }
            }
        }

        /**
         * open streams
         */
        public void open() {
            try {
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * close streams
         */
        public void close() {
            try {
                if (socket != null) {
                    socket.close();
                }
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Constructor which initializes server with given port
     *
     * @param port port for server
     */
    public Server(int port) {
        try {
            this.server = new ServerSocket(port);
            this.clients = new ArrayList<>();
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * wait clients and run their threads
     */
    @Override
    public void run() {
        while (thread != null) {
            try {
                Socket socket = server.accept();
                System.out.println("for test//new client connected");
                clients.add(new ServerThread(this, socket));
                clients.get(clients.size() - 1).open();
                clients.get(clients.size() - 1).start();
            } catch (IOException e) {
                e.printStackTrace();
                stop();
            }
        }
    }

    /**
     * start server thread
     */
    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    /**
     * stop server thread
     */
    public void stop() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
    }

    /**
     * find client with given id
     *
     * @param id given id
     * @return index of client or -1 if client is not connected
     */
    private int findClient(int id) {
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).getIdd() == id) {
                return i;
            }
        }
        return -1;
    }

    /**
     * send message to all clients
     * @param st sending message
     */
    public synchronized void handle(int id, String st) {
        if (st.contains("!exit")) {
            clients.get(findClient(id)).send("!exit");
            remove(id);
        } else {
            for (ServerThread client : clients) {
                System.out.println("for test//message " + st + " sent to client " + client.getName());
                client.send(st);
            }
        }
    }

    /**
     * remove client with given id from server
     *
     * @param id given id
     */
    public synchronized void remove(int id) {
        int index = findClient(id);
        if (index >= 0) {
            ServerThread thread = clients.get(index);
            clients.remove(index);
            thread.close();
            thread.stop();
        }
    }

    /**
     * start server
     * @param args args should contain 1 argument - server port
     */
    public static void main(String[] args) {
        Server server = new Server(Integer.parseInt(args[0]));
    }

}
