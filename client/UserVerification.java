//Prajwal Prasad
//1001750483

import java.awt.EventQueue;
import java.awt.Window;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;


import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.prefs.Preferences;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.Color;
import java.awt.SystemColor;

//https://www.youtube.com/watch?v=EcdbEax46bA- Used as reference to build GUI
public class UserVerification extends JFrame{

	private JFrame frame;
	private JTextField textField;
	private JLabel lblUserAlreadyExits;
	public static String userName;
    public volatile static HashSet<String> uname = new HashSet<String>(); 
	Preferences prefs = Preferences.userNodeForPackage(UserVerification.class); 
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UserVerification window = new UserVerification();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public UserVerification() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().setBackground(SystemColor.info);
		frame.setBackground(SystemColor.activeCaption);
		frame.setForeground(SystemColor.info);
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblEnterAUsername = new JLabel("Enter a username");
		lblEnterAUsername.setFont(new Font("Verdana", Font.PLAIN, 13));
		lblEnterAUsername.setBounds(34, 66, 125, 26);
		frame.getContentPane().add(lblEnterAUsername);
		textField = new JTextField();
		textField.setBounds(178, 68, 116, 22);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		JButton btnRegister = new JButton("Register!");
		btnRegister.setBackground(new Color(245, 255, 250));
		btnRegister.setForeground(new Color(30, 144, 255));
		btnRegister.setFont(new Font("Verdana", Font.PLAIN, 13));
		btnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//String user = textField.getText();
				userName = textField.getText();
				if(!userName.isEmpty()) {	//get usernames from prefs
					for(int j=0;j<4;j++) {
						uname.add(prefs.get(String.valueOf(j),null));
						uname.remove(null);
					}
					if(uname.contains(userName)) {	//if user already exists
						lblUserAlreadyExits.setText("User Already Exists! Please enter a new name.");
						textField.setText("");
					}
					else {
					uname.add(userName);
					lblUserAlreadyExits.setText("");
					textField.setText("");
			        //String fileName = "E:\\ClientServerArchitecture\\Client-Server Architecture\\client\\"+user;
					String fileName = System.getProperty("user.dir") +"\\client\\"+userName; //present working directory of user.
			        setUsername(uname);
			        Path path = Paths.get(fileName);
			        if (!Files.exists(path)) {
			            
			            try {
							Files.createDirectory(path);	//Create a new directory for the user
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			            System.out.println("Directory created");
			        } else {
			            
			            System.out.println("Directory already exists");
			        }
			        int i=0;
			        //https://stackoverflow.com/questions/4017137/how-do-i-save-preference-user-settings-in-java  -Preferences storage 	         
			        for(String users:uname) {
			        	 prefs.put(String.valueOf(i), users); //Store username in preference object for persistent storage
			        	 i++;
			        }
			         prefs.put("userName", userName);
			         frame.dispose();	//dispose userverification fram  and start FTPclient frame
			         FTPClient demo = new FTPClient();		         
			         demo.setVisible(true);	
					}			
				}
							
			else {
					lblUserAlreadyExits.setText("Please enter a valid name!");
				}
			}
				
		});
		btnRegister.setBounds(178, 138, 97, 25);
		frame.getContentPane().add(btnRegister);		
		lblUserAlreadyExits = new JLabel("");
		lblUserAlreadyExits.setForeground(Color.RED);
		lblUserAlreadyExits.setFont(new Font("Verdana", Font.PLAIN, 12));
		lblUserAlreadyExits.setBounds(68, 103, 352, 22);
		frame.getContentPane().add(lblUserAlreadyExits);
	}
	
    public HashSet<String> getUsernames() {
    	 System.out.println(UserVerification.userName);
        return UserVerification.uname;    
    }
 
    public void setUsername(HashSet<String> hset) {
        UserVerification.uname = hset;
        System.out.println(UserVerification.userName);
    }
	public String getTextField() {
		return textField.getText();
	}
}
