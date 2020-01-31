//Prajwal Prasad
//1001750483

import java.awt.Desktop;
import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import java.awt.SystemColor;
import javax.swing.ImageIcon;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class FTPClient extends JFrame {
	private static Socket clSocket = null;
    private PrintWriter socketOutput = null;
    private BufferedReader socketInput = null;
    private final int FILE_SIZE = 50000;
    private static  String userName;
    public String dcMessage;
	private  JLabel lblNewLabel;
    private  JButton btnDisconnect;
    private   Logger log;
    private JLabel lblNewLabel_1, lblNewLabel_2;
    private String fName;
    private DataOutputStream dataOut;
    private static int fc=0,resultCount=0;
    public volatile int delCount=0;
    private static int yes=0,no=0;
    private static int  yesCount=0,noCount=0;
    private int vote=0;
    static String  coordinatorName;
    private int result;
    static long endTime;
    static boolean deleteFlag;
    private static HashMap<String,Integer> deleteCount = new HashMap<String,Integer>();
    private HashMap<String,Integer> filecount = new HashMap<String,Integer>();
    private Preferences prefs2= Preferences.userNodeForPackage(ClientHandler.class);   
    
    /* This function is used to connect the client to server running on localhost using the socket number of the server */
    public void connectToServ() {
    	 try {
	            clSocket = new Socket( "localhost", 8888 ); // try and create the socket
	            socketOutput = new PrintWriter(clSocket.getOutputStream(), true); // Output writing stream
	            socketInput = new BufferedReader(new InputStreamReader(clSocket.getInputStream())); // Input reading stream
	                      
	            dataOut = new DataOutputStream(clSocket.getOutputStream()); //data output stream to server
	    		System.out.println("Connected to Server");

	        } 
	        catch (UnknownHostException e) {
	            System.err.println("Don't know about host.\n");
	            System.exit(1);
	        } 
	        catch (IOException e) {
	            System.err.println("Couldn't get I/O for the connection to host.\n");
	            System.exit(1);
	        }
    } 
	
	private JPanel contentPane;
	

	/**
	 * Launch the application.
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FTPClient frame = new FTPClient();
					frame.setVisible(true);
				   
				    
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/*https://github.com/KriechelD/YouTubeChannel/blob/master/YouTubeJava%20%239/src/Main.java -Watch Service
	Listens in the clients shared folder, when a new file is created, the file name is passed on to the sendFile function. */
	public void watcher(Path path) {
		try {
			WatchService watchService = FileSystems.getDefault().newWatchService();			
			path.register(watchService, ENTRY_CREATE,ENTRY_DELETE);

			System.out.println("WatchService listens: " + path);
			System.out.println("================");
			WatchKey key;
			while ((key = watchService.take()) != null) {
				for (WatchEvent<?> event : key.pollEvents()) {
					if(event.kind()==ENTRY_CREATE) {	//If new file is created on directory
						System.out.println("Event: " + event.kind() + " File: " + event.context());
						Path file2 = path.resolve((Path) event.context());
						new Thread(() -> {	//start a new thread with send function call
							try {
									send(file2.getFileName());
								} catch (IOException e) {
										// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}).start();
		                			
					}
					else if(event.kind()==ENTRY_MODIFY) { //If existing file is modified on directory
						System.out.println("Event: " + event.kind() + " File: " + event.context());						
						Path file2 = path.resolve((Path) event.context());
							new Thread(() -> {		//start a new thread with update function call
								try {
									update(file2.getFileName());	
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}).start();

					}
					else if(event.kind()==ENTRY_DELETE) { //If existing file is modified on directory
						System.out.println("Event: " + event.kind() + " File: " + event.context());	
						Path file2 = path.resolve((Path) event.context());
						fName = file2.getFileName().toString();	
						File file = new File("server/serverFiles/"+fName);
			            if(file.exists()) {
							new Thread(() -> {		//start a new thread with update function call
								try {
									Delete(file2.getFileName());								
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}).start();
			            }
					}

				}
				key.reset();
			}

		} catch (IOException e) {
			System.err.println("### IOException : " + e);
		} catch (InterruptedException e) {
			System.err.println("### InterruptedException : " + e);
		}
	}

	//Delete a file using username as parameter
	private void Delete(Path fileName) throws IOException {
			if(delCount<2) {
				System.out.println("Sending delete request to server along with voting poll!");         
	            DataOutputStream dataOut = new DataOutputStream(clSocket.getOutputStream()); //data output stream to server
	            dataOut.writeUTF("del"+" "+fileName.toString());
	            dataOut.flush();
	            DataInputStream din = new DataInputStream(clSocket.getInputStream());  
				coordinatorName=userName;
				//https://stackoverflow.com/questions/2258066/java-run-a-function-after-a-specific-number-of-seconds
				new java.util.Timer().schedule( 
					        new java.util.TimerTask() {
					            @Override
					            public void run() {
					                checkTimeout(); //check for client timeout after 5s
					            }
					        }, 
					        5000 
				);
			}
         
	}
	
	//Decide poll based on votes by other clients
	private void decidePoll(int dec) {
		if(dec==0) {
			System.out.println("Voting failed, keeping all copies of "+fName);
			globalDelete(fName,false);	//global abort
		}
		else if(dec==1) {
			System.out.println("Deleting all copies of "+fName);
			File file = new File("server/serverFiles/"+ fName);
			file.delete();
			globalDelete(fName,true);	//global delete
		}
	}
	
	private void globalDelete(String fName2, boolean choice) {
		int called=0;
		 try {
		if(choice) {
           
			dataOut.writeUTF("GD"+" "+"true");		//Delete all files instruction to server
            dataOut.flush();   
		}
	else if(!choice && called==0){
			called=1;
	        dataOut.writeUTF("GD"+" "+"false");		//Keep all files instruction to server
	        dataOut.flush();   
		}
		 }
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	 	
	}

	//https://stackoverflow.com/questions/25772640/java-socket-multithread-file-transfer
	//Send file to server through dataoutput stream on the client socket 
	public void send(Path path) throws IOException {
		        File file = new File("client/" +userName +"/"+ path);
		        if (file.exists() && !file.isDirectory()) {			//Check client file exists
		        	
		        	System.out.println("Uploading file to server");
		        	//log.info("uploading file to server");
		            int fileSize = (int)file.length();
		            byte [] fileByte = new byte[fileSize]; //Create byte array for data stream
		            InputStream dataIn = new FileInputStream(file); //data input stream from client file           
		            DataOutputStream dataOut = new DataOutputStream(clSocket.getOutputStream()); //data output stream to server
		            int count;
		            count = dataIn.read(fileByte, 0, fileByte.length);
		            dataOut.writeUTF("put"+" "+path.toString());
		            dataOut.write(fileByte, 0, count); //Write to data output stream
		            dataOut.flush();   
		   
		        	}
		        }
	public void update(Path path) throws IOException{	//send updated file to server			 
	                fc++;
	                String fname = path.toString();
	                filecount.put(fname, fc);
		            	int updtcnt=filecount.get(fname);
	                if(updtcnt>1) {
	                	filecount.remove(fname);
	                }
	                else if(updtcnt<2) {	//Update file only once
		                File file = new File("client/" +userName +"/"+ path);
		                if (file.exists() && !file.isDirectory()) {			//Check client file exists
			        	System.out.println("Updating new changes to server");
			        	//log.info("uploading file to server");
			            int fileSize = (int)file.length();
			            byte [] fileByte = new byte[fileSize]; //Create byte array for data stream
			            InputStream dataIn = new FileInputStream(file); //data input stream from client file      		          
			            DataOutputStream dataOut = new DataOutputStream(clSocket.getOutputStream()); //data output stream to server
			            int count;
			            count = dataIn.read(fileByte, 0, fileByte.length);
			            dataOut.writeUTF("update"+" "+path.toString());
			            dataOut.write(fileByte, 0, count); //Write to data output stream
			            dataOut.flush(); 
	                }

		        }
			}	
	
	
	//Listen for server response, if its download then download the file and halt the watcher service
	public void serverListner() throws IOException {	
		while(true) {
 			DataInputStream dis = new DataInputStream(clSocket.getInputStream()); 
 			String input=dis.readUTF();
	    	String [] splitInput = input.split(" "); //Split client input into command + filename
	    	String fileName = splitInput[1];
	    	String command = splitInput[0];	    
		    if(command.equals("download")) {
               
    		    	OutputStream dataOut = new FileOutputStream("client/" +userName+"/"+ fileName); //Create empty file
    				byte [] fileByte = new byte[FILE_SIZE]; //Generate byte array
    				int count;
    				count = dis.read(fileByte, 0, fileByte.length);
    				dataOut.write(fileByte, 0, count); //Write client file data to empty server file
    				dataOut.flush();
    				System.out.println("Received "+fileName+" from server!");
    		        listDirectory();	//List all files in directory after downloading
                 
		    	}
		    else if(command.equals("update")){
		    		System.out.println(fileName +" is outdated, updating from server!");
			    	OutputStream dataOut = new FileOutputStream("client/" +userName+"/"+ fileName); //Select the file to overwrite
					byte [] fileByte = new byte[FILE_SIZE]; //Generate byte array
					int count;
					count = dis.read(fileByte, 0, fileByte.length);
					dataOut.write(fileByte, 0, count); //Write client file data to empty server file
					dataOut.flush();
					System.out.println("Updated "+fileName+" from server!");
			        listDirectory();	//List all files in directory after downloading		    	
		    	}
		    else if(command.equals("delete")) {
		    	System.out.println("Vote on "+fileName+" deletion.");
		    		try {
						Thread.sleep(3000);

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    			if(Math.random() < 0.5) { //Adjust probability here <0.01 for always YES and >0.01 for always NO
	    				    result=0; 	//result 0 is for NO
	    				}
	    			  else {
	    				  result=1;		//result 1 is for YES
	    			  }
		    		resultCount++;
		    		if(resultCount<3) {
			    		if(result==0) {
			    			System.out.println("You have voted NO!!");
			    		}
			    		else {
			    			System.out.println("You have voted YES!!");
			    		}
			    
				    	 DataOutputStream dataOut = new DataOutputStream(clSocket.getOutputStream()); //data output stream to server
				         dataOut.writeUTF("poll"+" "+result);
				         dataOut.flush(); 
			    	 
		    		} 
	    	}

      		else if (command.equals("pollResults")) { //check the votes from clients
      			//long currTime = System.currentTimeMillis()/1000;
        		//if ( currTime < endTime) {
            		  //loop
            	    	 yesCount = Integer.parseInt(splitInput[1]);
            	    	 noCount = Integer.parseInt(splitInput[2]);
      					if(yesCount+noCount > 1) {
           					if(noCount==1) {   //Immediately issue a global abort when 1 NO is received
           							vote++;
         				        	decidePoll(0);          				        	 
           					}
         					else if(yesCount>2){
         							vote++;
         				        	decidePoll(1); //If all votes are yes, issue global delete       				        	 
         						}	
      					}
        		
      				}
	    	else {
	    			System.out.println(input); //Display message from server
	    		}

			}
	}
	
	//Check for client timeout after 5 seconds
	public void checkTimeout() {
			if(yesCount<3 && noCount<1) {
				System.out.println("Delete request timed out, keeping all files!");
				globalDelete(fName,false);
			}
	}
	
	//List all the files inside client directory after downloading new file
	public void listDirectory(){
		File dir = new File("client/"+userName );
		String[] files = dir.list();
		System.out.println("List of Files in directory:");
        for (int i = 0; i < files.length; i++) { //Append each file to list
				System.out.println(i+1+". "+files[i]);
		}
        if (files.length == 0) {
            System.out.println("The directory is empty");
        }
	}

	/**
	 * Create the frame.
	 * @throws BackingStoreException 
	 */
	/*https://www.youtube.com/watch?v=EcdbEax46bA -For GUI design using window builder
	https://github.com/RegisDeVallis/Java_Log_in_JTextArea/blob/master/TextAreaOutputStream.java -Out print lines to textArea
	Setup GUI elements on client side and button listeners*/
	public void initialize()  {

		String def = "Default";
		Preferences prefs = Preferences.userNodeForPackage(UserVerification.class);
		userName=prefs.get("userName", def);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBackground(SystemColor.info);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
	
		 JTextArea textArea= new JTextArea(30, 58);
		 textArea.setLineWrap(true);
		 textArea.setEditable(false);
		 textArea.setBounds(117, 111, 175, 75);
		 DefaultCaret caret = (DefaultCaret)textArea.getCaret();
		 caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		 TextAreaOutputStream taos = TextAreaOutputStream.getInstance(textArea); 
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBounds(51, 73, 252, 138);
		contentPane.add(scrollPane);
		
		lblNewLabel = new JLabel("");
		lblNewLabel.setForeground(SystemColor.textHighlight);
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("Verdana", Font.PLAIN, 15));
		lblNewLabel.setBounds(104, 13, 150, 31);
		contentPane.add(lblNewLabel);
		lblNewLabel.setText("Welcome "+userName);		
		btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {	    	        
				try {
					DataOutputStream dataOut = new DataOutputStream(clSocket.getOutputStream());
		            dataOut.writeUTF("dc"+" "+userName);
		            dataOut.flush();
		            System.exit(1);	//close client
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(1);	//close client
				} //data output stream to server
				
			}
		});
		btnDisconnect.setForeground(Color.RED);
		btnDisconnect.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
		btnDisconnect.setBounds(323, 147, 97, 25);
		contentPane.add(btnDisconnect);
		lblNewLabel_2 = new JLabel("");
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);			
		lblNewLabel_2.setBounds(255, 13, 28, 31);
		lblNewLabel_2.setIcon(new ImageIcon(FTPClient.class.getResource("/serverFiles/iconfinder_Tick_Mark_Dark_1398912.png")));
		contentPane.add(lblNewLabel_2);
		
		//https://stackoverflow.com/questions/7357969/how-to-use-java-code-to-open-windows-file-explorer-and-highlight-the-specified-f
		JButton btnBrowse = new JButton("Browse");
		btnBrowse.setForeground(new Color(30, 144, 255));
		btnBrowse.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				try {
					Desktop.getDesktop().open(new File("client/" +userName)); //open client directory on file explorer
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		btnBrowse.setBounds(323, 93, 97, 25);
		contentPane.add(btnBrowse);
		
	}
	
	//Constructor for client class
	public FTPClient() {
		setTitle("CLIENT");		
		initialize(); 
		connectToServ();
		// Create thread for watcher to run on the user directory			
	       Thread starter = new Thread(new Runnable()  
	        { 
	            public void run() { 						
					Path directory = Paths.get("client/" +userName+"/" );
					watcher(directory);         
	            } 
	        }); 
	    starter.start();
	    //Create a thread to listen for incoming server responses
	       Thread servListener = new Thread(new Runnable()  
	        { 
	            public void run() { 						
	        		try {
	        			serverListner();
	        		} catch (IOException e) {
	        			// TODO Auto-generated catch block
	        			e.printStackTrace();
	        		}						
            
	            } 
	        }); 
	       servListener.start();
	       
	}
	public JLabel getOnlineIcon() {
		return lblNewLabel_2;
	}
}
