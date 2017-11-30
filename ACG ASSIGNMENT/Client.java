import java.net.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.JOptionPane;

/*
 * The Client that can be run both as a console or a GUI
 */
public class Client  {

	// for I/O
	private ObjectInputStream sInput;		// to read from the socket
	private ObjectOutputStream sOutput;		// to write on the socket
	private Socket socket;
        private boolean j=false;
        private Calendar cal = Calendar.getInstance();
	// if I use a GUI or not
	private ClientGUI cg;

	// the server, the port and the username
	private String server, username,password,email;
	private int port;
        private static boolean hi=true;
	/*
	 *  Constructor called by console mode
	 *  server: the server address
	 *  port: the port number
	 *  username: the username
	 */
	Client(String server, int port, String username,String passwd,String email) {
		// which calls the common constructor with the GUI set to null
		this(server, port, username, null,passwd,email);
	}

	/*
	 * Constructor call when used from a GUI
	 * in console mode the ClienGUI parameter is null
	 */
	Client(String server, int port, String username, ClientGUI cg,String passwd,String email) {
		this.server = server;
		this.port = port;
		this.username = username;
                this.password=passwd;
                this.email=email;
		// save if we are in GUI mode or not
		this.cg = cg;
	}

	/*
	 * To start the dialog
	 */
	public boolean start() throws Exception {
            String yn;
		// try to connect to the server
		try {
			socket = new Socket(server, port);
		}
		// if it failed not much I can so
		catch(Exception ec) {
			display("Error connecting to server:" + ec);
			return false;
		}

		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);

		/* Creating both Data Stream */
		try
		{
                        sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO);
			return false;
		}

		// creates the Thread to listen from the server
		new ListenFromServer().start();
		// Send our username to the server this is the only message that we
		// will send as a String. All other messages will be ChatMessage objects
		try
		{
                        // Get the date today using Calendar object.
                         
                        // Using DateFormat format method we can create a string 
                        // representation of a datge with the defined format.ji
                        //Date today = Calendar.getInstance().getTime();
                        
			sOutput.writeObject(username);
                        String reportDate =DateUtils.getAtomicTime();
                        //Date ttoday=new Date(ttodays);
                        
                        
                        
                        String salt=reportDate+username;
                        sOutput.writeObject(Encryption.get_SHA_512_SecurePassword(password,salt));
                        sOutput.writeObject(email);
                }
		catch (IOException eIO) {
			display("Exception doing login : " + eIO);
			disconnect();
			return false;
		}
		// success we inform the caller that it worked
		return true;
	}

	/*
	 * To send a message to the console or the GUI
	 */
	private void display(String msg) {
		if(cg == null)
			System.out.println(msg);      // println in console mode
		else
			cg.append(msg + "\n");		// append to the ClientGUI JTextArea (or whatever)
	}
        public boolean okay() {
		return j;
	}

	/*
	 * To send a message to the server
	 */
	void sendMessage(ChatMessage msg) {
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			display("Exception writing to server: " + e);
		}
	}

	/*
	 * When something goes wrong
	 * Close the Input/Output streams and disconnect not much to do in the catch clause
	 */
	private void disconnect() {
		try {
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {} // not much else I can do
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {} // not much else I can do
        try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {} // not much else I can do

		// inform the GUI
		if(cg != null)
			cg.connectionFailed();

	}
	/*
	 * To start the Client in console mode use one of the following command
	 * > java Client
	 * > java Client username
	 * > java Client username portNumber
	 * > java Client username portNumber serverAddress
	 * at the console prompt
	 * If the portNumber is not specified 1500 is used
	 * If the serverAddress is not specified "localHost" is used
	 * If the username is not specified "Anonymous" is used
	 * > java Client
	 * is equivalent to
	 * > java Client Anonymous 1500 localhost
	 * are eqquivalent
	 *
	 * In console mode, if an error occurs the program simply stops
	 * when a GUI id used, the GUI is informed of the disconnection
	 */
        //no use this
        public static boolean returns(){
            return hi;
}
	public static void main(String[] args) throws Exception {
		// default values
		int portNumber = 1500;
		String serverAddress = "localhost";
		String userName = "Anonymous";
                String passwd="";
                String email="";
		// depending of the number of arguments provided we fall through
		switch(args.length) {
			// > javac Client username portNumber serverAddr
                        case 4:
				passwd = args[3];
			case 3:
				serverAddress = args[2];
			// > javac Client username portNumber
			case 2:
				try {
					portNumber = Integer.parseInt(args[1]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
					System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress][password]");
					return;
				}
			// > javac Client username
			case 1:
				userName = args[0];
			// > java Client
			case 0:
				break;
			// invalid number of arguments
			default:
				System.out.println("Usage is: > java Client [username] [portNumber] {serverAddress] [password]");
			return;
		}
		// create the Client object
		Client client = new Client(serverAddress, portNumber, userName,passwd,email);
		// test if we can start the connection to the Server
		// if it failed nothing we can do
		if(!client.start())
			return;

		// wait for messages from user
		Scanner scan = new Scanner(System.in);
		// loop forever for message from the user
		while(true) {
			System.out.print("> ");
			// read message from user
			String msg = scan.nextLine();
			// logout if message is LOGOUT
			if(msg.equalsIgnoreCase("LOGOUT")) {
				client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
				// break to do the disconnect
				break;
			}
			// message WhoIsIn
			else if(msg.equalsIgnoreCase("WHOISIN")) {
				client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));
			}
			else {				// default to ordinary message
				client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
			}
		}
		// done disconnect
		client.disconnect();
	}

	/*
	 * a class that waits for the message from the server and append them to the JTextArea
	 * if we have a GUI or simply System.out.println() it in console mode
	 */
	class ListenFromServer extends Thread {

		public void run() {
			while(true) {
                            
				try {
                                        
					String msg = (String) sInput.readObject();
                                        String msg1 = (String) sInput.readObject();
                                        if (msg.equals(msg1)){
                                            cg.appends(false);
                                            if (msg.equals("a")){
                                                msg="User already exists! please try another username!\n";
                                            }if(msg.equals("b")){
                                                msg="Wrong password!\n";
                                            }if (msg.equals("at")){
                                                msg="Username valid!\n";
                                            }if(msg.equals("bt")){
                                                msg="Password valid!\n";
                                            }if (msg.equals("c")){
                                                msg="Email with OTP sent! Please check your email!!\n";
                                                if(cg == null) {
						System.out.println(msg);
						System.out.print("> ");
                                                }
                                                else {
                                                        cg.append(msg);
                                                }
                                                int i=0;
                                                while (i<=3){
                                                String confirms=JOptionPane.showInputDialog(null, "Enter your OTP as sent to ur email \""+email+"\"");
                                                sOutput.writeObject(confirms);
                                                i++;
                                                String confirmed=(String) sInput.readObject();
                                                    System.out.println(confirmed);
                                                if((confirmed).equals("email valid")){
                                                    msg="Email valid!\n";
                                                    cg.appendss();
                                                    break;
                                                }
                                                }
                                            }if(msg.equals("d")){
                                                msg="Email cound not be sent! Please try again!";
                                            }
                                            if(cg == null) {
						System.out.println(msg);
						System.out.print("> ");
					}
					else {
						cg.append(msg);
					}
                                        }else{
                                        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
                                        String reportDate = DateUtils.getAtomicTime();
                                        msg=Encryption.decryptedtext(msg, password+reportDate);
                                        String hashvalue=Encryption.hashvalue(password+reportDate, username, msg);
                                        if(msg1.equals(hashvalue)){
                                            hi=false;
                                        if(cg == null) {
						System.out.println(msg);
						System.out.print("> ");
					}
					else {
						cg.append(msg);
					}
                                        }else{
                                            
                                        }
                                        }
                                }
				catch(IOException e) {
					display("Server has close the connection: " + e);
					if(cg != null)
						cg.connectionFailed();
					break;
                                        
				}
				// can't happen with a String object but need the catch anyhow
				catch(ClassNotFoundException e2) {
				}
                                
			}
		}
	}
       
}
