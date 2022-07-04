package discord.mainServer;

import discord.client.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainServer {
    // Fields:
    private static Map<String, Model> users = Collections.synchronizedMap(new HashMap<>());
    // maps the users' username to their Model object
    private static Map<Integer, Server> servers = Collections.synchronizedMap(new HashMap<>());
    // maps the servers' unicode to their Server object
    private final ServerSocket serverSocket;
    private final ExecutorService executorService;

    // Constructors:
    public MainServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        users = readUsers();
        servers = readServers();
        executorService = Executors.newCachedThreadPool();
    }

    // Methods:
    private static HashMap<String, Model> readUsers() {
        makeDirectory("assets");
        makeDirectory("assets\\users");
        HashMap<String, Model> clients = new HashMap<>();
        File folder = new File("assets\\users");
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null)
            for (File file : listOfFiles) {
                Model user = read(file);
                if (user != null)
                    clients.put(user.getUsername(), user);
                else System.err.println("null user was read!");
            }
        return clients;
    }

    private static void makeDirectory(String path) {
        if (new File(path).exists()) return;
        if (!new File(path).mkdir()) {
            System.err.println("Could not create the " + path + " directory!");
            throw new RuntimeException();
        }
    }

    private static <Type extends Asset> Type read(File file) {
        FileInputStream fileIn = null;
        ObjectInputStream in = null;
        try {
            fileIn = new FileInputStream(file);
            in = new ObjectInputStream(fileIn);
            return (Type) in.readObject();
        } catch (FileNotFoundException e) {
            System.err.println("file not found while iterating over the users!");
        } catch (IOException e) {
            System.err.println("I/O exception occurred while iterating over the users");
        } catch (ClassNotFoundException e) {
            System.err.println("class not found exception occurred while iterating over the users");
        } finally {
            //handleClosingInputs(fileIn, in);
            if (handleClosingStreams(fileIn, in)) {
                System.err.println("(input)");
            }
        }
        return null;
    }

    private static HashMap<Integer, Server> readServers() {
        makeDirectory("assets\\servers");
        HashMap<Integer, Server> servers = new HashMap<>();
        File folder = new File("assets\\servers");
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null)
            for (File file : listOfFiles) {
                Server server = read(file);
                if (server != null)
                    servers.put(server.getUnicode(), server);
                else System.err.println("null server was read!");
            }
        return servers;
    }

    // Getters:
    public static Map<String, Model> getUsers() {
        return users;
    }

    public static Map<Integer, Server> getServers() {
        return servers;
    }

    // Other Methods:
    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected");
                ClientHandler clientHandler = new ClientHandler(socket);
                executorService.execute(clientHandler);
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean signUpUser(Model newUser) {
        users.put(newUser.getUsername(), newUser);
        return updateDatabase(newUser);
    }

    public static <Type extends Asset> boolean updateDatabase(Type asset) {
        FileOutputStream fileOut = null;
        ObjectOutputStream out = null;
        try {
            String path = "assets\\";
            String id = "";
            if (asset instanceof Model) {
                id = ((Model) asset).getUsername();
                path = path.concat("users");
            }
            if (asset instanceof Server) {
                id = ((Server) asset).getUnicode() + "";
                path = path.concat("servers");
            }
            fileOut = new FileOutputStream(path + "\\" + id.concat(".bin"));
            out = new ObjectOutputStream(fileOut);
            out.writeObject(asset);
            return true;
        } catch (FileNotFoundException e) {
            System.err.println("Could not find this file!");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("I/O error occurred!");
            e.printStackTrace();
        } finally {
            //handleClosingOutputs(fileOut, out);
            if (handleClosingStreams(fileOut, out)) {
                System.err.println("(output)");
            }
        }
        return false;
    }

    /*private static void handleClosingOutputs(FileOutputStream fileOut, ObjectOutputStream out) {
        if (fileOut != null) try {
            fileOut.close();
        } catch (IOException e) {
            System.out.println("I/O error occurred while closing the stream of fileOut!");
        }
        if (out != null) try {
            out.close();
        } catch (IOException e) {
            System.out.println("I/O error occurred while closing the stream of out!");
        }
    }

    private static void handleClosingInputs(FileInputStream fileIn, ObjectInputStream in) {
        if (fileIn != null) try {
            fileIn.close();
        } catch (IOException e) {
            System.out.println("I/O error occurred while closing the stream of fileOut!");
        }
        if (in != null) try {
            in.close();
        } catch (IOException e) {
            System.out.println("I/O error occurred while closing the stream of out!");
        }
    }*/


    private static <FileStream extends Closeable, ObjectStream extends Closeable> boolean handleClosingStreams(
            FileStream fileStream, ObjectStream objectStream) {
        if (fileStream != null) try {
            fileStream.close();
        } catch (Exception e) {
            System.err.println("Exception occurred while closing the fileStream!");
        }
        if (objectStream != null) try {
            objectStream.close();
        } catch (Exception e) {
            System.err.println("Exception occurred while closing the objectStream!");
            return true;
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        new MainServer(new ServerSocket(6000)).startServer();
    }
}
