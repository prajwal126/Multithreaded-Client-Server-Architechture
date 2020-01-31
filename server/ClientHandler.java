//Prajwal Prasad
//1001750483

import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


//https://github.com/alexandersteele/Multi-threaded-FTP-Server-Client/blob/master/server/ClientHandler.java -Used as reference to build clienthandler
//https://www.geeksforgeeks.org/multi-threaded-chat-application-set-1/ -Used as reference to build clienthandler
public class ClientHandler extends Thread {
    private final int FILE_SIZE = 50000; //Max file size for file transfer
    private Socket socket = null;
    String client;
    private static String delFile;
    private Boolean isLoggedin = false;
    PrintWriter textOut;
    BufferedReader textIn;
    DataInputStream dataInput;DataOutputStream dataOutput;
    private static int fc=0,yes=0,no=0;
    private HashMap<String,Integer> filecount = new HashMap<String,Integer>();
    private HashMap<String,Integer> votes = new HashMap<String,Integer>();
    public HashSet<String> uname = new HashSet<String>(); 
    private static DataOutputStream controllerOutput;
    private static String controllerName;
    //Initialize clienthandler constructor
    public ClientHandler(Socket socket,String user, DataOutputStream dos,DataInputStream din) {
		super("ClientHandler");
		this.socket = socket;
		this.client=user;
		this.dataInput=din;
		this.dataOutput=dos;
		this.isLoggedin=true;
    }
    
    
    public void run() {
    	
        	InetAddress inet = socket.getInetAddress(); //get IP address of client machine       	
        	//textOut = new PrintWriter(dataOut, true); //text output to client
        	InputStream input;
            		while(this.isLoggedin) {
            			try {
                       		input=this.socket.getInputStream();
            				String dis = new DataInputStream(input).readUTF(); 
            		    	String [] splitInput = dis.split(" "); //Split client input into command + arg
            		    	String fileName = splitInput[1];
            		    	String command = splitInput[0];
            	    		 //List all the files on the server directory
            		    	if (command.equals("list")) { //Generate a list of files in serverFiles
            	                log(inet, "LIST"); //Log request
            					File dir = new File("server/serverFiles");
            					String[] files = dir.list();
            					String fileList = "";
            	                System.out.println(files.length);
            	                for (int i = 0; i < files.length; i++) { //Append each file to list
            							fileList += files[i] + " ";
            					}
            	                if (files.length == 0) {
            	                    textOut.println("The directory is empty");
            	                }
            	                else{
            	                    textOut.println(fileList);
            	                }
            				}
            		    	
            		    	//Store file in server directory if the client wants to send file to server 
            				else if (command.equals("put")) { //Transfer file from  client to server
            					if(fileName!=null) {					
            				              log(inet, "PUT"); //Log request
            				              	File dir = new File("server/serverFiles");
            								String[] files = dir.list();
            								String fileList = "";
            				                for (int i = 0; i < files.length; i++) { //Append each file to list
            										fileList += files[i] + " ";
            								}
            				                if(!fileList.contains(fileName)) {
            				                	 System.out.println("Recieving file from "+client);
            						              	//log.info("Recieving file from client!");
            						                //InputStream dataIn = socket.getInputStream(); //Get data from client for server
            										OutputStream dataOut = new FileOutputStream("server/serverFiles/" + fileName); //Create empty file
            										byte [] fileByte = new byte[FILE_SIZE]; //Generate byte array
            										int count;
            										count = input.read(fileByte, 0, fileByte.length);
            										dataOut.write(fileByte, 0, count); //Write client file data to empty server file
            										dataOut.flush();
            										sendtoAllClients(fileName,socket,client,false); //call method broadcast file to all clients connected	
            				                }
            					}	
            	  
            	            }
            		    	//Update the file on all clients and server based on a counter
            				else if (command.equals("update")) { //Transfer file from  client to server
            					if(fileName!=null) {
            						
            				              	log(inet, "UPDATE"); //Log request
            				                fc++;
            				                filecount.put(fileName, fc); //Keep track of number of times file has been updated
             				            	int updtcnt=filecount.get(fileName);
            				                if(updtcnt>2) {
            				                	filecount.remove(fileName);
            				                }
            
            				                if(updtcnt<2) {
            					                System.out.println("Recieving updated file from "+client);
                								OutputStream dataOut = new FileOutputStream("server/serverFiles/" + fileName); //Create empty file
                								byte [] fileByte = new byte[FILE_SIZE]; //Generate byte array
                								int count;
                								count = input.read(fileByte, 0, fileByte.length);
                								dataOut.write(fileByte, 0, count); //Write client file data to empty server file
                								dataOut.flush();
            				                	sendtoAllClients(fileName,socket,client,true); //call method broadcast file to all clients connected
            				                }
            				  
            					}
            	  
            	            }
            		    	//Send delete messages to other clients from the cordinator's request
            				else if (command.equals("del")) { //Transfer file from  client to server
            					if(fileName!=null) {      						
            				         if(yes<2) {
                				         log(inet, "DELETE"); //Log request
                				         System.out.println("Received delete notification from "+client); 
                				         delFile=fileName;
                				         controllerOutput = new DataOutputStream(socket.getOutputStream());
                				         controllerName=client;	//set cordinator name as current client
                				         yes++;
                				         //votes.put("yes", yes);
            				        	 sendDeleteMessage(fileName,socket,client); //call method to broadcast message to all clients connected   
            				         }                				  
            					}
            	  
            	            }
            				else if (command.equals("GD")) { //Global delete request from cordinator
            					if(fileName!=null) {      						
            				         if(fileName.equals("true")) {
                				        globalDelete(delFile, socket, client, true);   //Delete all instaces of file
            				         } 
            				         else if(fileName.equals("false")){
            				        	 globalDelete(delFile, socket, client, false);  //keep all instances of file
            				         }
            					}
            	  
            	            }
            		    	//Collect the votes from clients and send to cordinator
            				else if (command.equals("poll")) { //Transfer file from  client to server
            					int vote = Integer.parseInt(fileName);
            					if(vote==0) {      						
            				         log(inet, "POLL"); //Log request
            				         no++;
            				        // votes.put("no", no);  
            					}
            					else {
              				         log(inet, "POLL"); //Log request
              				         yes++;
            				        // votes.put("yes", yes);
            					}
            					for(ClientHandler mc : Server.ar) {
            						if(mc.client.equals(controllerName)) {
                						mc.dataOutput.writeUTF("pollResults"+" "+ yes +" "+ no);
                    					mc.dataOutput.flush();      							
            							break;
            						}
            						else if(!Server.userList.contains(controllerName) && mc.client==client) { //if cordinator has dc
            							try {
											Thread.sleep(4000);	//sleep for 4 sec
											mc.dataOutput.writeUTF("The Poll failed, The cordinator has disconnected/crashed.");
											mc.dataOutput.flush();
											break;
											} catch (InterruptedException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
											}
            						}
            					}
            					//controllerOutput.writeUTF("pollResults"+" "+ yes +" "+ no);
            					//controllerOutput.flush();
            	            } 
            		    	//Check if client wants to disconnect
            				else if(command.equals("dc")) {
            					System.out.println(fileName+" has disconnected!");
            					//Server.textArea_1.append(fileName+" has disconnected! \n");
            					this.isLoggedin=false;	//set login boolean to false
            					int index=0;
            					for(int i=0;i<Server.ar.size();i++) {
            						if(Server.ar.get(i).client==client) {
            							index=i;
            						}
            					}
            					Preferences prefs = Preferences.userNodeForPackage(UserVerification.class);
            					//Remove all instances of client from server as preferences.
            					Server.ar.remove(index); //remove user from the server vector
            					Server.userList.removeAll(Collections.singleton(client)); //remove user from the server userlist
            					if(Server.userList.isEmpty()) {
            						System.out.println("No active clients!"); 
            						Server.textArea_1.setText("No active clients!");
            						prefs.clear();
            						
            					}
            					else {
                					System.out.println("Active clients:");               					
            	            		Server.textArea_1.setText("Active clients: \n");
                					for(String ac:Server.userList) {
                						System.out.println("* "+ac);	//print all active users in userlist
                						Server.textArea_1.append("* "+ac+"\n");
                					}
            					}

            				}

            				} catch (Exception e) {
            					    e.printStackTrace();
            				}
            			}
    }
    

    //Perform actions based on global abort or global delete
	private void globalDelete(String fName2, Socket socket2, String client2, boolean b) {
		if(b) { //Global delete
        	for (ClientHandler mc : Server.ar)  //check current client to clients stored on server vector
            { 
                // if the recipient is found, write on its 
                // output stream   
        		
        			String toUserName = mc.client; //client the file needs to be sent
                	File file = new File("client/"+toUserName+"/"+fName2); //Location of server file

    				if (file.exists() && !file.isDirectory()) { //Check server file exists                          
    					file.delete();
                    }    				
    				try {
    					mc.dataOutput.writeUTF(fName2 +" has been deleted based on poll!");
						mc.dataOutput.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            }
	     }
		else { //Global abort
		try {
    	for (ClientHandler mc : Server.ar)  //check current client to clients stored on server vector
        	{ 
            // if the recipient is found, write on its 
            // output stream     		
    			//String toUserName = mc.client; //client the file needs to be sent
    		if(mc.client.equals(client2)) {
    		  	File file = new File("server/serverFiles/"+fName2); //Location of server file
            	
				if (file.exists() && !file.isDirectory()) { //Check server file exists                          
                    int fileSize = (int)file.length(); //Generate dynamic file size
				    byte [] fileByte = new byte[fileSize]; //Create byte array for data
				    InputStream dataIn = new FileInputStream(file); //Input server file data
				    //DataOutputStream dataOut = new DataOutputStream(mc.dataOutput);
				    mc.dataOutput.writeUTF("download"+" "+fName2); //send download notification to client
				    int count;
				    count = dataIn.read(fileByte, 0, fileByte.length);
				    mc.dataOutput.write(fileByte, 0, count); //Write data over stream
				    mc.dataOutput.flush();
                }
    		}
    		else if(!mc.client.equals(client2) && mc.client==client){
				mc.dataOutput.writeUTF("Voting failed, keeping all copies of "+fName2);
				mc.dataOutput.flush();
				break;
    		}
	          
        	}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	 }	
	} 


	/*https://www.geeksforgeeks.org/multi-threaded-chat-application-set-1/ -Expanded upon the concept of sending to multiple active clients 
    from string to files. This function broadcasts the file upon receiving to all active users using their data I/O streams, sockets and username. */
    private void sendtoAllClients(String path,Socket sock,String currUser, boolean mod) throws IOException{
    	if(mod) {
            	for (ClientHandler mc : Server.ar)  //check current client to clients stored on server vector
                { 
                    // if the recipient is found, write on its 
                    // output stream     		
            		String toUserName = mc.client; //client the file needs to be sent
                    if (currUser!=toUserName)  //To avoid sending the file back to the sender
                    {
                    	
                    	System.out.println("Sending update notification to "+toUserName);
                    	File file = new File("server/serverFiles/" + path); //Location of server file

        				if (file.exists() && !file.isDirectory()) { //Check server file exists                          
                            int fileSize = (int)file.length(); //Generate dynamic file size
        				    byte [] fileByte = new byte[fileSize]; //Create byte array for data
        				    InputStream dataIn = new FileInputStream(file); //Input server file data       				   
        				    mc.dataOutput.writeUTF("update"+" "+path.toString()); //send update notification to client
        				    int count;
        				    count = dataIn.read(fileByte, 0, fileByte.length);
        				    mc.dataOutput.write(fileByte, 0, count); //Write data over stream
        				    mc.dataOutput.flush();        				    
                        }

                    } 
                }
    	}
    	else {
        	for (ClientHandler mc : Server.ar)  //check current client to clients stored on server vector
            { 
                // if the recipient is found, write on its 
                // output stream     		
        		String toUserName = mc.client; //client the file needs to be sent
                if (currUser!=toUserName)  //To avoid sending the file back to the sender
                { 
                	System.out.println("Sending to "+toUserName);
                	File file = new File("server/serverFiles/" + path); //Location of server file

    				if (file.exists() && !file.isDirectory()) { //Check server file exists
                        
                        int fileSize = (int)file.length(); //Generate dynamic file size
    				    byte [] fileByte = new byte[fileSize]; //Create byte array for data
    				    InputStream dataIn = new FileInputStream(file); //Input server file data
    				    //DataOutputStream dataOut = new DataOutputStream(mc.dataOutput);
    				    mc.dataOutput.writeUTF("download"+" "+path.toString()); //send download notification to client
    				    int count;
    				    count = dataIn.read(fileByte, 0, fileByte.length);
    				    mc.dataOutput.write(fileByte, 0, count); //Write data over stream
    				    mc.dataOutput.flush();
    				 
                    }

                } 
            }
    	}		
	}
    
    private void sendDeleteMessage(String path,Socket sock,String currUser) throws IOException{
            	for (ClientHandler mc : Server.ar)  //check current client to clients stored on server vector
                { 
                    // if the recipient is found, write on its 
                    // output stream     		
            		String toUserName = mc.client; //client the file needs to be sent
            		
                    if (currUser!=toUserName)  //To avoid sending the file back to the sender
                    {
                    	   System.out.println("Sending delete notification to "+toUserName);                   	
        				   mc.dataOutput.writeUTF("delete"+" "+path.toString() +" "+sock); //send update notification to client
        				   mc.dataOutput.flush();        				    
                     }

                  } 
                
    	}
    
    //Log requests to text file
	public void log (InetAddress inet, String request) throws IOException { 	
		  Date date = new Date(); SimpleDateFormat ft = new SimpleDateFormat
		  ("dd.MM.yyyy ':' HH:mm:ss"); File file = new File("server/log.txt");
		  BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
		  out.write(ft.format(date) + " : " + inet.getHostAddress() + " : " + request);
		  out.newLine(); out.close();
		 
    }

}
