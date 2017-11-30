

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;


/*
 * The Client with its GUI
 */
public class ClientGUI extends JFrame implements ActionListener {

    private boolean jik;
    private SecretKey key;
    private static final long serialVersionUID = 1L;
    // will first hold "Username:", later on "Enter message"
    private JLabel label;
    // to hold the Username and later on the messages
    private JTextField tf;
    // to hold the server address an the port number
    private JTextField tfServer, tfPort, password, emails, code;
    // to Logout and get the list of the users
    private JButton login, logout, whoIsIn;
    // for the chat room
    private JTextArea ta;
    // if it is for connection
    private boolean connected;
    // the Client object
    private Client client;
    // the default port number
    private int defaultPort;
    private String defaultHost;
    private String username, email;

    // Constructor connection receiving a socket number
    ClientGUI(String host, int port) {
        super("Chat Client");
        defaultPort = port;
        defaultHost = host;
        JPanel passwd1 = new JPanel(new GridLayout(0, 1, 0, 0));
        JPanel passwd = new JPanel();
        passwd.add(new JLabel("Password :   "));
        password = new JTextField(10);
        passwd.add(password);
        add(passwd, BorderLayout.EAST);

        JPanel email = new JPanel();
        email.add(new JLabel("Email Address :   "));
        emails = new JTextField(10);
        email.add(emails);
        //JPanel conf = new JPanel();
        //conf.add(new JLabel("Confirmation code :   "));

        //code = new JTextField("xxx-xxx-xxx-xxx-xxxx-xx");
        //conf.add(code);
        passwd1.add(passwd);
        passwd1.add(email);
        //passwd1.add(conf);
        add(passwd1, BorderLayout.EAST);

        // The NorthPanel with:
        JPanel northPanel = new JPanel(new GridLayout(3, 1));
        // the server name anmd the port number
        JPanel serverAndPort = new JPanel(new GridLayout(1, 5, 1, 3));
        // the two JTextField with default value for server address and port number
        tfServer = new JTextField(host);
        tfPort = new JTextField("" + port);
        tfPort.setHorizontalAlignment(SwingConstants.RIGHT);

        serverAndPort.add(new JLabel("Server Address:  "));
        serverAndPort.add(tfServer);
        serverAndPort.add(new JLabel("Port Number:  "));
        serverAndPort.add(tfPort);
        serverAndPort.add(new JLabel(""));
        // adds the Server an port field to the GUI
        northPanel.add(serverAndPort);

        // the Label and the TextField
        label = new JLabel("Enter your username below", SwingConstants.CENTER);
        northPanel.add(label);
        tf = new JTextField(username);
        tf.setBackground(Color.WHITE);
        northPanel.add(tf);
        add(northPanel, BorderLayout.NORTH);

        // The CenterPanel which is the chat room
        ta = new JTextArea("Welcome to the Chat room\n", 80, 80);
        JPanel centerPanel = new JPanel(new GridLayout(1, 1));
        centerPanel.add(new JScrollPane(ta));
        ta.setEditable(false);
        add(centerPanel, BorderLayout.CENTER);

        // the 3 buttons
        login = new JButton("Login");
        login.addActionListener(this);
        logout = new JButton("Logout");
        logout.addActionListener(this);
        logout.setEnabled(false);		// you have to login before being able to logout
        whoIsIn = new JButton("Who is in");
        whoIsIn.addActionListener(this);
        whoIsIn.setEnabled(false);		// you have to login before being able to Who is in

        JPanel southPanel = new JPanel();
        southPanel.add(login);
        southPanel.add(logout);
        southPanel.add(whoIsIn);
        add(southPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 600);
        setVisible(true);
        tf.requestFocus();

    }

    // called by the Client to append text in the TextArea
    void append(String str) {
        ta.append(str);
        ta.setCaretPosition(ta.getText().length() - 1);
    }
    void appendss(){
    login.setEnabled(false);
            // enable the 2 buttons
            logout.setEnabled(true);
            whoIsIn.setEnabled(true);
            // disable the Server and Port JTextField
            tfServer.setEditable(false);
            tfPort.setEditable(false);
            password.setEditable(false);
            emails.setEditable(false);
            // Action listener for when the user enter a message
            //while client.get!=true
            //nth
            tf.setEditable(true);
           
    }
    void appends(boolean g) {
            login.setEnabled(g);
            // enable the 2 buttons
            logout.setEnabled(g);
            whoIsIn.setEnabled(g);
            // disable the Server and Port JTextField
            tfServer.setEditable(g);
            emails.setEditable(g);
            tfPort.setEditable(g);
            password.setEditable(g);
            // Action listener for when the user enter a message
            //while client.get!=true
            //nth
            tf.setEditable(g);
            //tf.addActionListener(this);
    }

    // called by the GUI is the connection failed
    // we reset our buttons, label, textfield
    void connectionFailed() {
        login.setEnabled(true);
        logout.setEnabled(false);
        whoIsIn.setEnabled(false);
        label.setText("Enter your username below");
        tf.setText(username);
        // reset port number and host name as a construction time
        tfPort.setText("" + defaultPort);
        tfServer.setText(defaultHost);
        // let the user change them
        tfServer.setEditable(true);
        tfPort.setEditable(true);
        emails.setEditable(true);
        password.setEditable(true);
        //tf.setEditable(true);

        // don't react to a <CR> after the username
        tf.removeActionListener(this);
        connected = false;
    }

    /*
	* Button or JTextField clicked
     */
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        // if it is the Logout button
        if (o == logout) {
            client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
            return;
        }
        // if it the who is in button
        if (o == whoIsIn) {
            client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));
            return;
        }

        // ok it is coming from the JTextField
        if (connected) {
            //DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            System.out.println("sendesesnd");
            String reportDate = DateUtils.getAtomicTime();
            String passwordd = password.getText().trim();
            String message = tf.getText();
            String pattern = "\\S*www\\.\\S+";
            if (message.matches(pattern)) {
                JOptionPane.showMessageDialog(null, "Please do not send links!", "Error", JOptionPane.ERROR_MESSAGE);
                tf.setText("");
                return;
            } else {
             
            String salt = passwordd + reportDate;
            String hashvalue = Encryption.hashvalue(passwordd + reportDate, username, message);
            String encrypted = "";
            try {
                encrypted = Encryption.encryptedtext(message, passwordd + reportDate);
            } catch (Exception ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                // just have to send the message
                client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, encrypted));
                client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, hashvalue));
            } catch (Exception ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            tf.setText("");
            return;
            }
        }
        if (o == login) {
            // ok it is a connection request
            email = emails.getText().trim();
            username = tf.getText().trim();
            emails.setEditable(true);
            String passwor = password.getText().trim();
            // empty username ignore it
            if (username.length() == 0) {
                JOptionPane.showMessageDialog(null, "Username cannot be null", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // empty serverAddress ignore it
            String server = tfServer.getText().trim();
            if (server.length() == 0) {
                JOptionPane.showMessageDialog(null, "Server address cannot be null", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // empty or invalid port numer, ignore it
            String portNumber = tfPort.getText().trim();
            if (portNumber.length() == 0) {
                JOptionPane.showMessageDialog(null, "Port number cannot be null", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (email.length() == 0) {
                JOptionPane.showMessageDialog(null, "Email cannot be null", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int port = 0;
            try {
                port = Integer.parseInt(portNumber);
            } catch (Exception en) {
                return;   // nothing I can do if port number is not valid
            }

            // try creating a new Client with GUI
            client = new Client(server, port, username, this, passwor, email);
            tf.setEditable(false);
            try {
                if (!client.start()) {
                    return;
                }
            } catch (Exception ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            tf.setBackground(Color.WHITE);

            tf.setText("");
            label.setText("Enter your message below");
            connected = true;
           
            // disable login buttonhallo
            
            
            // disable login buttonhallo
            login.setEnabled(false);
            // enable the 2 buttons
            logout.setEnabled(true);
            whoIsIn.setEnabled(true);
            // disable the Server and Port JTextField
            tfServer.setEditable(false);
            tfPort.setEditable(false);
            password.setEditable(false);
            // Action listener for when the user enter a message
            //while client.get!=true
            //nth
            tf.setEditable(true);
            tf.addActionListener(this);
        }

    }

    // to start the whole thing the server
    public static void main(String[] args) throws IOException {
        // String usernamee =JOptionPane.showInputDialog(null, "Username :");

        /*try {
            
            boolean tru = true;
            while (tru == true) {
                JLabel label_login = new JLabel("Username:");
                JTextField login = new JTextField();

                JLabel label_password = new JLabel("Password:");
                JPasswordField password = new JPasswordField();
                String[] options1 = {"OK", "CANCEL", "MAKE NEW ACCOUNT"};
                Object[] array = {label_login, login, label_password, password};
                String response = "";
                int res = JOptionPane.showOptionDialog(null,
                        array,
                        "Login",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        options1,
                        null);

                if (res == JOptionPane.YES_OPTION) {
                    
                    Socket echoSocket = new Socket("localhost", 420);//specify ip of password server here
            PrintWriter out
                    = new PrintWriter(echoSocket.getOutputStream(), true);
            BufferedReader in
                    = new BufferedReader(
                            new InputStreamReader(echoSocket.getInputStream()));
                    out.println("login");
                    usernamee = login.getText().trim();
                    String passsword=(new String(password.getPassword()));
                    String encrypt=(Encryption.encryptedtext(usernamee,passsword));
                    String send=Encryption.get_SHA_512_SecurePassword(encrypt,encrypt);
                    out.println(send);
                    response = in.readLine();
                System.out.println(response);

                }
                if (res == JOptionPane.NO_OPTION) {
                    tru = false;
                }
                if (res == JOptionPane.CANCEL_OPTION) {
                    
                    boolean t = true;
                    while (t == true) {
                        
                        Socket echoSocket = new Socket("localhost", 420);//specify ip of password server here
            PrintWriter out
                    = new PrintWriter(echoSocket.getOutputStream(), true);
            BufferedReader in
                    = new BufferedReader(
                            new InputStreamReader(echoSocket.getInputStream()));
                        label_login = new JLabel("Username:");
                        login = new JTextField();
                        label_password = new JLabel("Password:");
                        password = new JPasswordField();
                        JLabel label_email = new JLabel("Email:");
                        JTextField email = new JTextField();
                        Object[] arrays = {label_login, login, label_password, password,label_email,email};
                        int ress = JOptionPane.showConfirmDialog(null,
                                arrays,
                                "Create account",
                                JOptionPane.OK_OPTION,
                                JOptionPane.PLAIN_MESSAGE
                        );
                        String emaill="";
                        if (ress == JOptionPane.OK_OPTION) {

                            System.out.println(login.getText().trim());
                            System.out.println(new String(password.getPassword()));
                            out.println("make");
                            usernamee =(login.getText().trim());
                            String passsword=(new String(password.getPassword()));
                            String encrypt=(Encryption.encryptedtext(usernamee,passsword));
                            String send=Encryption.get_SHA_512_SecurePassword(encrypt,encrypt);
                            out.println(Encryption.get_SHA_512_SecurePassword(usernamee,usernamee));
                            out.println(send);
                            emaill=email.getText().trim();
                            out.println(email.getText().trim());
                            out.println("");
                            response = in.readLine();
                            System.out.println(response);
                        }
                        if (response.equals("same")) {
                            JOptionPane.showMessageDialog(null, "Username already used", "Error", JOptionPane.ERROR_MESSAGE);
                        } else if (response.equals("done")) {
                            String confirms=JOptionPane.showInputDialog(null, "Enter your OTP as sent to ur email \""+emaill+"\"");
                            out.println(confirms);
                            response = in.readLine();
                            if (response.equals("notequal")){
                                JOptionPane.showMessageDialog(null, "Wrong OTP", "Error", JOptionPane.ERROR_MESSAGE);
                        System.exit(0);
                            }else if(response.equals("yes")){
                                JOptionPane.showMessageDialog(null, "Account successfully created","", JOptionPane.INFORMATION_MESSAGE);
                        System.exit(0);
                                t=false;
                            }
                        }else if (response.equals("not")) {
                            JOptionPane.showMessageDialog(null, "invalid email", "Error", JOptionPane.ERROR_MESSAGE);
                        }else if (response.equals("email")) {
                            JOptionPane.showMessageDialog(null, "Someone has already used this email!\n please use another", "Error", JOptionPane.ERROR_MESSAGE);
                        }else{
                            t = false;
                        }
                    }
                }

                if (response.equals("yes") && tru == true) {
         *///new ClientGUI("localhost", 1500, usernamee);
        JOptionPane.showMessageDialog(null, "Remember to set your system time to automatic!", "Note!", JOptionPane.INFORMATION_MESSAGE);
        new ClientGUI("localhost", 1500);
        /*tru = false;
                } else {
                    JOptionPane.showMessageDialog(null, "Wrong Password", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }*/

    }
}
