package server;

import client.ChatClient3IF;

public class Chatter {

	public String name;
	public ChatClient3IF client;
	
	public Chatter(String name, ChatClient3IF client){
		this.name = name;
		this.client = client;
	}

	public String getName(){
		return name;
	}
	public ChatClient3IF getClient(){
		return client;
	}
	
}