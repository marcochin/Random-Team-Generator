package com.marcochin.teamrandomizer;
import java.io.Serializable;

public class UpdateObj implements Serializable{
	private String action;
	private String playerName;
	
	public UpdateObj (String action, String playerName){
		try{
			if(action.equals("add") || action.equals("delete")){
				this.action = action;
			}
			else {
				throw new Exception("Update action must be \"add\" or \"delete\"");
			}
		}catch(Exception e){
			e.getMessage();
		}
		
		this.playerName = playerName;
	}
	
	public String getAction() {
		return action;
	}
	
	public String getPlayerName() {
		return playerName;
	}
	
}
