import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerManager {

    private Socket socket;
    private Thread thread;
    private final List<AsyncClient> clients = new ArrayList<>();
    private ServerSocket serverSocket;
    private LinkedBlockingQueue<Object> messages;
    private final ExecutorService service = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        ServerManager serverSocketManager = new ServerManager();
        serverSocketManager.start();
    }

    public boolean stopServer() {
        try {
            serverSocket.close();
        } catch (IOException io) {
            return false;
        }
        return true;
    }

    /**
    *  Starting server
    */
    public boolean startServer() {
        try {
            this.serverSocket = new ServerSocket(8131);
            return true;
        } catch (IOException io) {
            return false;
        }
    }

    /**
     *  When we take any message, send all of the clients
     */
    private void sendAllMessage(String message) {
        for (AsyncClient client : clients) {
            client.sendMessage(message);
        }
    }

    /**
     *  Initialization Server
     */
    public void start() {
        messages = new LinkedBlockingQueue<Object>();
        System.out.println("Server starting....");

        thread = new Thread(new Runnable() {


            @Override
            public void run() {

                boolean isStarted = startServer();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (isStarted) {
                    System.out.println("Server started!");
                    sendMessage("Hello");
                    while (true) {
                        try {
                            socket = serverSocket.accept();
                            clients.add(new AsyncClient(socket));
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("Server starting failed!");
                }
            }
        });

        /**
         * Listing all message and send all clients
         */
        thread.start();
        Thread messageHandling = new Thread() {
            public void run() {
                while (true) {
                    try {
                        Object message = messages.take();

                        sendAllMessage((String) message);

                        // Do some handling here...
                        System.out.println("Message Received: " + message);
                    } catch (InterruptedException e) {
                    }
                }
            }
        };

        messageHandling.setDaemon(true);
        messageHandling.start();
    }

    /**
     * Example client connection
     */
    private void sendMessage(final String msg) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    //Replace below IP with the IP of that device in which server socket open.
                    //If you change port then change the port number in the server side code also.
                    Socket s = new Socket("127.0.0.1", 8131);

                    OutputStream out = s.getOutputStream();

                    PrintWriter output = new PrintWriter(out);

                    output.println(msg);
                    output.flush();
                    BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    final String message = input.readLine();
                    System.out.println(message);


                    output.close();
                    out.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    /**
     * Connected client object
     */
    private class AsyncClient {
        private final Socket socket;

        private BufferedReader in;
        private PrintWriter out;

        public AsyncClient(Socket socket) {

            this.socket = socket;
          
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    char[] buffer = new char[500];

                    int length = 0;
                    while (true) {
                        try {
                            length = in.read(buffer);
                            if (length > 0) {
                                String msg = new String(buffer).substring(0, length);
                                System.out.println(msg);
                                messages.put(msg);
                            }

                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                            break;
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            break;
                        }


                    }


                }

            });

            thread.setDaemon(true);
            thread.start();

        }

        /**
         * Send the message to the this client
         * @param message
         */
        public void sendMessage(String message) {
            // TODO Auto-generated method stub

            // TODO Auto-generated method stub
            out.write(message.trim());
            out.flush();
        }


    }

}
