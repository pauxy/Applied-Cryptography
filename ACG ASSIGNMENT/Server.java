import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * The server that can be run both as a console application or a GUI
 */
public class Server {

    // a unique ID for each connection
    private Calendar cal = Calendar.getInstance();
    private static int uniqueId;

    private boolean ji;
    // an ArrayList to keep the list of the Client
    private ArrayList<ClientThread> al;
    // if I am in a GUI
    private ServerGUI sg;
    // to display time
    private SimpleDateFormat sdf;
    // the port number to listen for connection
    private int port;
    private String password;
    // the boolean that will be turned of to stop the server
    private boolean keepGoing;
    private static String servmsg;
    private static boolean mess = false;


    /*
	 *  server constructor that receive the port to listen to for connection as parameter
	 *  in console
     */
    public Server(int port, String password) {
        this(port, password, null);
    }

    public Server(int port, String password, ServerGUI sg) {
        // GUI or not
        this.sg = sg;
        // the port
        this.port = port;
        this.password = password;
        // to display hh:mm:ss
        sdf = new SimpleDateFormat("HH:mm:ss");
        // ArrayList for the Client list
        al = new ArrayList<ClientThread>();
    }

    public void start() throws Exception {
        keepGoing = true;

        /* create socket server and wait for connection requests */
        try {
            ServerSocket serverSocket = new ServerSocket(port);

            // infinite loop to wait for connections
            while (keepGoing) {

// format message saying we are waiting
                display("Server waiting for Clients on port " + port + ".");

                Socket socket = serverSocket.accept();  	// accept connection
                // if I was asked to stop
                if (!keepGoing) {
                    break;
                }
                ClientThread t = new ClientThread(socket);  // make a thread of it
                al.add(t);// save it in the ArrayList
                if (ji == true) {
                    kick(t.username);
                }
                t.start();

            }
            // I was asked to stop
            try {
                serverSocket.close();
                for (int i = 0; i < al.size(); ++i) {
                    ClientThread tc = al.get(i);
                    try {
                        tc.sInput.close();
                        tc.sOutput.close();
                        tc.socket.close();
                    } catch (IOException ioE) {
                        // not much I can do
                    }
                }
            } catch (Exception e) {
                display("Exception closing the server and clients: " + e);
            }
        } // something went bad
        catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
            display(msg);
        }
    }

    /*
     * For the GUI to stop the server
     */
    protected void stop() {
        keepGoing = false;
        // connect to myself as Client to exit statement
        // Socket socket = serverSocket.accept();
        try {
            new Socket("localhost", port);
        } catch (Exception e) {
            // nothing I can really do
        }
    }

    /*
	 * Display an event (not a message) to the console or the GUI
     */
    private void display(String msg) {
        String time = sdf.format(new Date()) + " " + msg;
        if (sg == null) {
            System.out.println(time);
        } else {
            sg.appendEvent(time + "\n");
        }
    }

    /*
	 *  to broadcast a message to all Clients
     */
    public synchronized void broadcast(String message, boolean yes) throws Exception {
        // add HH:mm:ss and \n to the message
        String time = sdf.format(new Date());

        String messageLf;
        if (yes == true) {
            messageLf = time + " " + message + "\n";
        } else {
            messageLf = "*** Server message: " + message + " ***\n";
        }
        // display message on console or GUI

        if (sg == null) {
            System.out.print(messageLf);
        } else {
            sg.appendRoom(messageLf);     // append in the room window
        }
        // we loop in reverse order in case we would have to remove a Client
        // because it has disconnected
        for (int i = al.size(); --i >= 0;) {
            ClientThread ct = al.get(i);
            // try to write to the Client if it fails remove it from the list
            if (!ct.writeMsg(messageLf)) {
                al.remove(i);
                display("Disconnected Client " + ct.username + " removed from list.");
            }
        }
    }

    // for a client who logoff using the LOGOUT message
    synchronized void remove(int id) {
        // scan the array list until we found the Id
        for (int i = 0; i < al.size(); ++i) {
            ClientThread ct = al.get(i);
            // found it
            if (ct.id == id) {
                al.remove(i);
                return;
            }
        }
    }

    public void kick(String name) throws Exception {
        boolean avail = false;
        for (int i = al.size(); --i >= 0;) {
            ClientThread ct = al.get(i);
            if (name.equals(ct.username)) {
                avail = true;
                ct.writeMsg("You have been kicked from the chat by the server\n");
                ct.socket.close();
                al.remove(i);
                display("User kicked: " + ct.username);
                return;
            }
        }
        if (avail) {
        } else {
            display("No such user " + "\"" + name + "\"");
        }
    }

    public void ban(String name) throws Exception {
        boolean avail = false;
        BufferedWriter bw = null;
        FileWriter fw = null;
        String email = "";
        for (int i = al.size(); --i >= 0;) {
            ClientThread ct = al.get(i);
            if (name.equals(ct.username)) {
                System.out.println(ct.email);
                email = ct.email;
            }
        }
        if (avail == true) {
            display("No such user " + "\"" + name + "\"");

        } else {
            try {

                File file = new File("C:\\Users\\chuny\\Desktop\\Documents\\NetBeansProjects\\ACG\\src\\javaapplication19\\password.txt");

                // true = append file
                fw = new FileWriter(file.getAbsoluteFile(), true);
                bw = new BufferedWriter(fw);
                bw.write(email);
                bw.newLine();
            } catch (Exception e) {

                System.out.println(e);

            } finally {

                try {

                    if (bw != null) {
                        bw.close();
                    }

                    if (fw != null) {
                        fw.close();
                    }

                } catch (Exception ex) {

                    ex.printStackTrace();

                }
            }
            kick(name);
        }
    }

    /*
	 *  To run as a console application just open a console window and:
	 * > java Server
	 * > java Server portNumber
	 * If the port number is not specified 1500 is used
     */
    public static void main(String[] args) throws Exception {
        // start server on port 1500 unless a PortNumber is specified
        int portNumber = 1500;
        String passwd = "";
        switch (args.length) {
            case 2:
                passwd = args[1];
            case 1:
                try {
                    portNumber = Integer.parseInt(args[0]);
                } catch (Exception e) {
                    System.out.println("Invalid port number.");
                    System.out.println("Usage is: > java Server [portNumber]");
                    return;
                }
            case 0:
                break;
            default:
                System.out.println("Usage is: > java Server [portNumber]");
                return;

        }
        // create a server object and start it
        Server server = new Server(portNumber, passwd);
        server.start();
    }

    /**
     * One instance of this thread will run for each client
     */
    class ClientThread extends Thread {
        // the socket where to listen/talk

        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        // my unique id (easier for deconnection)
        int id;
        // the Username of the Client
        String username;
        String email;
        String passwd;
        // the only type of message a will receive
        ChatMessage cm;
        ChatMessage cm1;
        // the date I connect
        String date;

        // Constructore
        ClientThread(Socket socket) throws Exception {
            // a unique id
            id = ++uniqueId;
            this.socket = socket;
            /* Creating both Data Stream */
            System.out.println("Thread trying to create Object Input/Output Streams");
            try {
                DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");

                // Get the date today using Calendar object.
                String reportDate = DateUtils.getAtomicTime();
                // Using DateFormat format method we can create a string 
                // representation of a date with the defined format.
                // create output first
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                // read the username

                ji = false;
                username = (String) sInput.readObject();
                passwd = (String) sInput.readObject();
                email = (String) sInput.readObject();
                String salt = reportDate + username;

                for (int i = al.size(); --i >= 0;) {
                    ClientThread ct = al.get(i);
                    // try to write to the Client if it fails remove it from the list
                    if (username.equals(ct.username)) {
                        writeMsg1("a");//User already exists! please try another username!\n
                        display("User tried to use username " + username);
                        close();
                        return;
                    }
                }
                writeMsg1("at");
                if (passwd.equals(Encryption.get_SHA_512_SecurePassword(password, salt)) == false) {
                    writeMsg1("b");//Wrong password!\n
                    display("Someone inputed the wrong password ");
                    //kick(username);
                    //close();
                    ji = true;
                    System.out.println("password");
                    return;

                }
                writeMsg1("bt");
                String confirm = SendMail.generateString();
                if (check(email)){
                boolean h = SendMail.send(email, confirm);
                int l = 0;
                if (h == true) {
                    writeMsg1("c");//email sent!\n
                    display("Email with OTP sent , OTP is " + confirm);
                    String confirmed;
                    boolean d = false;
                    while (!(confirmed = (String) sInput.readObject()).equals("") && l < 3) {
                        System.out.println(confirmed);
                        System.out.println(confirm);
                        if (confirmed.equals(confirm)) {
                            sOutput.writeObject("email valid");
                            display("correct");
                            d = true;
                            ji = false;
                            System.out.println("email correct");
                            break;
                        } else {
                            sOutput.writeObject("no");
                            d = false;
                            System.out.println("email wrong");
                        }
                        l++;

                    }
                    if (d == true) {
                    } else {
                        //kick(username);
                        ji = true;
                        //close();
                        System.out.println("emailwrongwrong");
                        return;
                    }
                } else {
                    writeMsg1("d");//email not sent! please try again later!\n
                    ji = true;
                    System.out.println("email no end");
                }
                }else{
                ji=true;
                writeMsg("You've been banned by the server\n");
                return;
                }
                writeMsg("---------- SUCCESSFUL LOGIN, " + username + " ----------\n");
                broadcast(username + " logged in", true);
                display(username + " just connected.");
            } catch (IOException e) {
                display("Exception creating new Input/output Streams: " + e);
                return;
            } // have to catch ClassNotFoundException
            // but I read a String, I am sure it will work
            catch (ClassNotFoundException e) {
            }
            date = new Date().toString() + "\n";
        }

        // what will run forever
        public void run() {
            // to loop until LOGOUT
            boolean keepGoing = true;
            while (keepGoing) {
                int x = 0;
                int y = 10;
                if (mess == true) {
                    try {
                        broadcast(servmsg, false);
                    } catch (Exception ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    mess = false;
                }
                // read a String (which is an object)
                long endTime = System.currentTimeMillis() + 5000;
                while (System.currentTimeMillis() < endTime || x > y) {
                    try {
                        cm = (ChatMessage) sInput.readObject();
                    } catch (IOException e) {
                        display(username + " Exception reading Streams: " + e);
                        close();
                        return;
                    } catch (ClassNotFoundException e2) {
                        break;
                    }
                    // the messaage part of the ChatMessage
                    String message = cm.getMessage();

                    //loop
                    // Switch on the type of message receive
                    switch (cm.getType()) {

                        case ChatMessage.MESSAGE: {
                            x++;
                            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
                            String reportDate = DateUtils.getAtomicTime();

                            try {
                                cm1 = (ChatMessage) sInput.readObject();
                            } catch (IOException e) {
                                display(username + " Exception reading Streams: " + e);
                                close();
                                return;
                            } catch (ClassNotFoundException e2) {
                                break;
                            }
                            String sum = cm1.getMessage();
                            String msg = Encryption.decryptedtext(message, password + reportDate);
                            String hash = Encryption.hashvalue(password + reportDate, username, msg);

                            if (hash.equals(sum)) {
                                try {
                                    broadcast(username + ": " + msg, true);
                                } catch (Exception ex) {
                                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            } else {
                                try {
                                    writeMsg("Sum things wrong!\n");
                                } catch (Exception ex) {
                                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                display("Someone inputed different sum \n");
                                close();
                                return;
                            }

                        }
                        break;
                        case ChatMessage.LOGOUT:
                            x++;
                            display(username + " disconnected with a LOGOUT message.");
                            keepGoing = false;
                            break;
                        case ChatMessage.WHOISIN:
                            x++;

                            try {
                                writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
                            } catch (Exception ex) {
                                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            // scan al the users connected
                            for (int i = 0; i < al.size(); ++i) {
                                ClientThread ct = al.get(i);
                                try {
                                    writeMsg((i + 1) + ") " + ct.username + " since " + ct.date);
                                } catch (Exception ex) {
                                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            break;

                    }
                    if (x > y) {
                        try {
                            writeMsg("Stop spamming");
                        } catch (Exception ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        try {
                            broadcast(username + " was disconnected for spamming too much", false);
                        } catch (Exception ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        x = 0;
                        try {
                            kick(username);
                            close();
                            return;

                        } catch (Exception ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        close();

                    }
                }
            }
            // remove myself from the arrayList containing the list of the
            // connected Clients
            remove(id);
            close();
        }

        /*public void kick(String name){
                    boolean avail=false;
                    for(int i = al.size(); --i >= 0;) {
                    ClientThread ct = al.get(i);
                                // try to write to the Client if it fails remove it from the list
                                if(name.equals(ct.username)) {
                                    avail=true;
                                    writeMsg("You have been kicked from the chat by the server\n");
                                    display("User kicked: "+username);
                                    close();
                                    display("User kicked: "+username);
                                    avail=true;
                                    return;
                                }
                                }
                    if (avail){
                    }else{
                        display("No such user "+"\""+name+"\"");
                    }
                }*/
        // try to close everything
        private void close() {
            // try to close the connection
            try {
                if (sOutput != null) {
                    sOutput.close();
                }
            } catch (Exception e) {
            }
            try {
                if (sInput != null) {
                    sInput.close();
                }
            } catch (Exception e) {
            };
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception e) {
            }
        }

        /*
		 * Write a String to the Client output stream
         */
        private boolean writeMsg(String msg) throws Exception {
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            String reportDate = DateUtils.getAtomicTime();

            String msg2 = Encryption.hashvalue(password + reportDate, username, msg);
            String msg1 = Encryption.encryptedtext(msg, password + reportDate);
            // if Client is still connected send the message to it
            if (!socket.isConnected()) {
                close();
                return false;
            }
            // write the message to the stream
            try {

                sOutput.writeObject(msg1);
                sOutput.writeObject(msg2);
            } // if an error occurs, do not abort just inform the user
            catch (IOException e) {
                display("Error sending message to " + username);
                display(e.toString());
            }
            return true;
        }

        private boolean writeMsg1(Object a) throws Exception {
            if (!socket.isConnected()) {
                close();
                return false;
            }
            // write the message to the stream
            try {

                sOutput.writeObject(a);
                sOutput.writeObject(a);
            } // if an error occurs, do not abort just inform the user
            catch (IOException e) {
                display("Error sending message to " + username);
                display(e.toString());
            }
            return true;
        }

        private boolean check(String mail) throws IOException {
            BufferedWriter bw = null;
            boolean tru = true;
            String line1 = "";
            try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\chuny\\Desktop\\Documents\\NetBeansProjects\\ACG\\src\\javaapplication19\\password.txt"))) {
                while ((line1 = br.readLine()) != null) {
                    if (line1.equals(mail)) {
                        tru = false;
                    }
                }

            }
            return tru;
        }

    }

}
