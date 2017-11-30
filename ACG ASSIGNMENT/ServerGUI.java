import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/*
 * The server as a GUI
 */
public class ServerGUI extends JFrame implements ActionListener, WindowListener {
        public static String PASSWORD;
	private static final long serialVersionUID = 1L;
	// the stop and start buttons
	private JButton stopStart,send,kick,bann;
        // JTextArea for the chat room and the events
	private JTextArea chat, event,Broadcastmsg;
	// The port number f
	private JTextField tPortNumber;
        private JTextField PASSWDFIELD,user,bans;
	// my server
	private Server server;
        

	// server constructor that receive the port to listen to for connection as parameter
	ServerGUI(int port) {
		super("Chat Server");
		server = null;
		// in the NorthPanel the PortNumber the Start and Stop buttons
		JPanel north = new JPanel();
		north.add(new JLabel("Port number: "));
                JPanel password = new JPanel();
                JPanel BAN = new JPanel();
                JPanel broadcast = new JPanel();
                JPanel ban = new JPanel();
                JPanel passwd = new JPanel(new GridLayout(0,1,0,0));
                bans = new JTextField(10);
                BAN.add(new JLabel("Permanantly ban: "));
                
                BAN.add(bans);
                
		password.add(new JLabel("Set password: "));
                tPortNumber = new JTextField("  " + port);
                PASSWDFIELD = new JTextField(10);
                north.add(tPortNumber);
                password.add(PASSWDFIELD);
                broadcast.add(new JLabel("Broadcast message"));
                Broadcastmsg = new JTextArea(20,20);
                Broadcastmsg.setLineWrap(true);
                Broadcastmsg.setWrapStyleWord(true);
                send = new JButton("Send");
                send.addActionListener(this);
                
                broadcast.add(Broadcastmsg);
                broadcast.add(send);
		// to stop or start the server, we start with "Start"
                
                
		stopStart = new JButton("Start");
                stopStart.addActionListener(this);
                north.add(stopStart);
                broadcast.setLayout(new BoxLayout(broadcast, BoxLayout.Y_AXIS));
                ban.add(new JLabel("Kick User: "));
                String[] users = new String[] {};
                kick = new JButton("Kick");
                kick.addActionListener(this);
                bann = new JButton("Ban");
                bann.addActionListener(this);
                BAN.add(bann);
                user = new JTextField(10);
                ban.add(user);
                ban.add(kick);
                passwd.add(password);
                passwd.add(ban);
                passwd.add(BAN);
                passwd.add(broadcast);
                add(north, BorderLayout.NORTH);
                add(passwd, BorderLayout.EAST);
		// the event and chat room
                
		JPanel center = new JPanel(new GridLayout(2,1));
		chat = new JTextArea(80,80);
		chat.setEditable(false);
		appendRoom("Chat room.\n");
		center.add(new JScrollPane(chat));
		event = new JTextArea(80,80);
		event.setEditable(false);
		appendEvent("Events log.\n");
		center.add(new JScrollPane(event));
		add(center);

		// need to be informed when the user click the close button on the frame
		addWindowListener(this);
		setSize(600, 600);
		setVisible(true);
                Broadcastmsg.setEditable(false);
                send.setEnabled(false);
                user.setEditable(false);
                kick.setEnabled(false);
                bans.setEditable(false);
                bann.setEnabled(false);
	}

	// append message to the two JTextArea
	// position at the end
	void appendRoom(String str) {
		chat.append(str);
		chat.setCaretPosition(chat.getText().length() - 1);
	}
	void appendEvent(String str) {
		event.append(str);
		event.setCaretPosition(chat.getText().length() - 1);

	}

	// start or stop where clicked
	public void actionPerformed(ActionEvent e) {
		// if running we have to stop
                Object o = e.getSource();
		if (o==stopStart){
		if(server != null) {
			server.stop();
			server = null;
			tPortNumber.setEditable(true);
                        PASSWDFIELD.setEditable(true);
                        Broadcastmsg.setEditable(false);
                        send.setEnabled(false);
                         user.setEditable(false);
                        kick.setEnabled(false);
                        bans.setEditable(false);
                bann.setEnabled(false);
			stopStart.setText("Start");
			return;
		}
                
                
      	// OK start the server
		int port;
		try {
			port = Integer.parseInt(tPortNumber.getText().trim());
		}
		catch(Exception er) {
			appendEvent("Invalid port number");
			return;
		}
                String passwd;
		passwd = PASSWDFIELD.getText().trim();
                    System.out.println(passwd);
                String pattern1 =".*[a-zA-Z]+.*";
                String pattern2=".*\\d+.*";
                String pattern3="^.{10,}";
                    System.out.println(passwd.matches(pattern1));
                    System.out.println(passwd.matches(pattern2));
                    System.out.println(passwd.matches(pattern3));
                if(passwd.equals("")){
                    JOptionPane.showMessageDialog(null, "Password cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!passwd.matches(pattern1)){
                    JOptionPane.showMessageDialog(null, "Password must contain at least one alphabetic character", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                } if (!passwd.matches(pattern2)){
                     JOptionPane.showMessageDialog(null, "Password must contain at least one digit", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                } if (!passwd.matches(pattern3)){
                     JOptionPane.showMessageDialog(null, "Password must be at least 10 characters long", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                
		// ceate a new Server
		server = new Server(port,passwd,this);
		// and start it as a thread
		new ServerRunning().start();
		stopStart.setText("Stop");
		tPortNumber.setEditable(false);
                PASSWDFIELD.setEditable(false);
                Broadcastmsg.setEditable(true);
                bans.setEditable(true);
                bann.setEnabled(true);
                send.setEnabled(true);
                user.setEditable(true);
                kick.setEnabled(true);
            }
                
                if(o==send){
                    try {
                        server.broadcast(Broadcastmsg.getText().trim(),false);
                    } catch (Exception ex) {
                        Logger.getLogger(ServerGUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Broadcastmsg.setText("");
			
                }
                if (o==kick){
                    try {
                        server.kick(user.getText().trim());
                    } catch (Exception ex) {
                        Logger.getLogger(ServerGUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if(o==bann){
                    try {
                        server.ban(bans.getText().trim());
                    } catch (Exception ex) {
                        Logger.getLogger(ServerGUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
}

	// entry point to start the Server
	public static void main(String[] arg) {
		// start server default port 1500
		new ServerGUI(1500);
	}

	/*
	 * If the user click the X button to close the application
	 * I need to close the connection with the server to free the port
	 */
	public void windowClosing(WindowEvent e) {
		// if my Server exist
		if(server != null) {
			try {
				server.stop();			// ask the server to close the conection
			}
			catch(Exception eClose) {
			}
			server = null;
		}
		// dispose the frame
		dispose();
		System.exit(0);
	}
	// I can ignore the other WindowListener method
	public void windowClosed(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}

	/*
	 * A thread to run the Server
	 */
	class ServerRunning extends Thread {
		public void run() {
                    try {
                        server.start();         // should execute until if fails
                    } catch (Exception ex) {
                        Logger.getLogger(ServerGUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
			// the server failed
			stopStart.setText("Start");
			tPortNumber.setEditable(true);
			appendEvent("Server crashed\n");
			server = null;
		}
	}

}
