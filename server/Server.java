//Prajwal Prasad
//1001750483

import java.net.*;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

import java.awt.Font;
import java.awt.Color;
import java.awt.SystemColor;
import javax.swing.JButton;
import javax.swing.UIManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;


/*https://github.com/alexandersteele/Multi-threaded-FTP-Server-Client/blob/master/server/Server.java -Used as reference for multithreaded server
  https://www.geeksforgeeks.org/multi-threaded-chat-application-set-1/ - Used as reference for adding users to vector and handler
  https://github.com/RegisDeVallis/Java_Log_in_JTextArea/blob/master/TextAreaOutputStream.java */

public class Server extends JFrame{
	
	private static String userName;
	public static Vector<ClientHandler> ar = new Vector<>(); 
	// counter for clients  
    private static JLabel lblNewLabel;
    private JTextArea textArea;
    private JPanel contentPane;
    private static JButton btnStopServer;
    private static ServerSocket server = null;
    static  ArrayList<String> userList = new ArrayList<>();
    Preferences prefs = Preferences.userNodeForPackage(ClientHandler.class);
    public static JTextArea textArea_1 = new JTextArea();
    private JPanel panel;
    
    //Server constructor used to setup GUI elements and button listeners
	public Server() throws BackingStoreException {
		setBackground(SystemColor.activeCaption);
		setFont(new Font("Dialog", Font.BOLD, 12));
		setForeground(SystemColor.activeCaption);
		setTitle("SERVER");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 507, 373);
		contentPane = new JPanel();
		contentPane.setBackground(SystemColor.info);
		contentPane.setForeground(SystemColor.activeCaption);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		lblNewLabel = new JLabel("");
		lblNewLabel.setForeground(SystemColor.textHighlight);
		lblNewLabel.setFont(new Font("Lucida Fax", Font.BOLD, 16));
		lblNewLabel.setBounds(114, 24, 136, 24);
        lblNewLabel.setText("Server Started !");
		contentPane.add(lblNewLabel);

		btnStopServer = new JButton("Stop Server");
		btnStopServer.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnStopServer.setBackground(SystemColor.inactiveCaptionBorder);
		btnStopServer.setForeground(Color.RED);
		btnStopServer.setBounds(134, 265, 105, 25);
		btnStopServer.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(!server.isClosed()) {
					System.out.println("Stopping Server!"); //Stop server when disconnect is clicked			
					userList.clear();
					System.exit(1);
				}
			}
		});
		contentPane.add(btnStopServer);
	
		textArea = new JTextArea(30, 58);
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		textArea.setBounds(51, 73, 265, 138);
		DefaultCaret caret = (DefaultCaret)textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		TextAreaOutputStream taos = TextAreaOutputStream.getInstance(textArea); 		
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBounds(21, 73, 265, 138);
		contentPane.add(scrollPane);
		
		panel = new JPanel();
		panel.setBackground(SystemColor.textHighlight);
		//panel.setBorder(new TitledBorder(null, "Clients Status", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setBounds(317, 73, 148, 138);
		contentPane.add(panel);
		panel.setLayout(null);
		textArea_1.setBounds(9, 15, 130, 110);
		panel.add(textArea_1);
		textArea_1.setRows(5);
		textArea_1.setFont(new Font("Segoe UI", Font.BOLD, 13));
		
		textArea_1.setEditable(false);
		textArea_1.setText("No active clients \r\n");
		}
	
	//Start the server socket and listen for incoming client connections
    public static void main(String[] args) throws IOException {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Server frame = new Server();
					frame.setVisible(true);
				  
		
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		});
        ExecutorService service = null;
		// Try to open up the listening port
        try {
        	
        	System.out.println("Starting Server on port 8888!");
            server = new ServerSocket(8888); //Open server on socket specified
        } catch (IOException e) {
            System.err.println("Could not listen on port: 8888.");
            System.exit(-1);
        }

		//service = Executors.newFixedThreadPool(10); // Initialise the executor to 10 fixed threads
		Preferences prefs2 = Preferences.userNodeForPackage(UserVerification.class);
		String def = "Default";
		while( true) // For each new client, submit a new handler to the thread pool.
		{
			Socket client = server.accept();	//accept client connection
			userName=prefs2.get("userName", def);	//Get username from userverification when user logs in, which is stored in preferences	
			if(userList.contains(userName)) {
				userList.remove(userName);	//Remove if user already exists
			}
			else {
				userList.add(userName); //Add username to arraylist if its new.
				DataInputStream dis = new DataInputStream(client.getInputStream()); 
		        DataOutputStream dos = new DataOutputStream(client.getOutputStream()); 
				ClientHandler mtch = new ClientHandler(client,userName,dos,dis); //Add username,data I/O streams, client socket to handler
				//service.submit( mtch ); //Submit executor service request
				ar.add(mtch); //add handler to vector
				Thread t = new Thread(mtch); 				
				t.start();			
				System.out.println(userName+" has connected!"); 
				textArea_1.setText("Active clients: \n");
				for(String ac: userList) {					
					textArea_1.append("* "+ac+"\n");	
				}
			}
		
		}
		
    }
}

