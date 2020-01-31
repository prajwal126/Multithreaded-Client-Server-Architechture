1) Run the Server java file
2) Run UserVerification and enter a name and click on register, you must repeat the process for each client.(i.e rerun the Userverification for each client)
3) Once the client is connected to the server, you can browse its local shared directory(client/"username") to delete a file.
4) The probability is set to 50/50 (YES/NO) by default, you can adjust the probability to test different cases.(FTPClient.java line 338)
5) If the clients except cordinator vote YES, all instances of file will be deleted; else, if one of them vote NO, file will be restored back on cordinator.
   Refresh the project directory to see all the file changes in other client's shared directories.
6) Disconnect and restart the clients and servers to test out timeouts:
	1) Disconnect cordinator immediately after deleting the file and while other clients are in voting phase, timeout message will be displayed on those clients.
	2) Disconnect one of the clients after voting poll has been sent by server and before vote is cast, the cordinator will check for client timeout and issue global abort.


Note: Dont delete iconfinder_Tick_Mark_Dark_1398912.png on serverFiles it is used to add check mark when client is connected and online
      This project was developed on eclipse 2019-06 IDE.
     - Use client names a1,a2,a3 preferablly since I've kept some files to delete in their folder.If you want to create a new clients,
      copy files from there are use it.
     - The working directory is not hardcoded, it should take your pwd.If it doesn't work for some reason check Userverification.Java line 109 to 	hardcode it to your needs.