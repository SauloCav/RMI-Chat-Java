package server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteRef;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.Vector;

import client.ChatClient3IF;

public class ChatServer extends UnicastRemoteObject implements ChatServerIF {
	String line = "---------------------------------------------\n";
	private Vector<Chatter> chatters;
	private static final long serialVersionUID = 1L;
	
	public ChatServer() throws RemoteException {
		super();
		chatters = new Vector<Chatter>(10, 1);
	}

	public static void main(String[] args) {
		startRMIRegistry();	
		String hostName = "localhost";
		String serviceName = "GroupChatService";
		
		if(args.length == 2){
			hostName = args[0];
			serviceName = args[1];
		}
		
		try{
			ChatServerIF hello = new ChatServer();
			Naming.rebind("rmi://" + hostName + "/" + serviceName, hello);
			System.out.println("RMI Chat Server está rodando...");
		}
		catch(Exception e){
			System.out.println("O Server encontrou problemas ao iniciar!");
		}	
	}

	public static void startRMIRegistry() {
		try{
			java.rmi.registry.LocateRegistry.createRegistry(1099);
			System.out.println("RMI Server preparado!");
		}
		catch(RemoteException e) {
			e.printStackTrace();
		}
	}
		
	public String sayHello(String ClientName) throws RemoteException {
		System.out.println(ClientName + " Enviou uma Mensagem");
		return "Olá " + ClientName + " do grupo Chat Server";
	}
	
	public void updateChat(String name, String nextPost) throws RemoteException {
		String message =  name + " : " + nextPost + "\n";
		sendToAll(message);
	}
	
	@Override
	public void passIDentity(RemoteRef ref) throws RemoteException {	
		try{
			System.out.println(line + ref.toString());
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void registerListener(String[] details) throws RemoteException {	
		System.out.println(new Date(System.currentTimeMillis()));
		System.out.println(details[0] + " Juntou-se ao Chat");
		System.out.println(details[0] + "'s hostname : " + details[1]);
		System.out.println(details[0] + "'sRMI service : " + details[2]);
		registerChatter(details);
	}

	private void registerChatter(String[] details){		
		try{
			ChatClient3IF nextClient = ( ChatClient3IF )Naming.lookup("rmi://" + details[1] + "/" + details[2]);
			
			chatters.addElement(new Chatter(details[0], nextClient));
			
			nextClient.messageFromServer("[Server] : Olá " + details[0] + " Você agora está livre para conversar no Chat.\n");
			
			sendToAll("[Server] : " + details[0] + " has joined the group.\n");
			
			updateUserList();		
		}
		catch(RemoteException | MalformedURLException | NotBoundException e){
			e.printStackTrace();
		}
	}

	private void updateUserList() {
		String[] currentUsers = getUserList();	
		for(Chatter c : chatters){
			try {
				c.getClient().updateUserList(currentUsers);
			} 
			catch (RemoteException e) {
				e.printStackTrace();
			}
		}	
	}

	private String[] getUserList(){

		String[] allUsers = new String[chatters.size()];
		for(int i = 0; i< allUsers.length; i++){
			allUsers[i] = chatters.elementAt(i).getName();
		}
		return allUsers;
	}

	public void sendToAll(String newMessage){	
		for(Chatter c : chatters){
			try {
				c.getClient().messageFromServer(newMessage);
			} 
			catch (RemoteException e) {
				e.printStackTrace();
			}
		}	
	}

	@Override
	public void leaveChat(String userName) throws RemoteException{
		
		for(Chatter c : chatters){
			if(c.getName().equals(userName)){
				System.out.println(line + userName + " left the chat session");
				System.out.println(new Date(System.currentTimeMillis()));
				chatters.remove(c);
				break;
			}
		}		
		if(!chatters.isEmpty()){
			updateUserList();
		}			
	}
	
	@Override
	public void sendPM(int[] privateGroup, String privateMessage) throws RemoteException{
		Chatter pc;
		for(int i : privateGroup){
			pc= chatters.elementAt(i);
			pc.getClient().messageFromServer(privateMessage);
		}
	}
	
}