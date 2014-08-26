package com.mukunda.parties;
 

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
 

public class PartyTryLeaveEvent extends Event implements Cancellable {
	public static final HandlerList handlers = new HandlerList();
	  
	private Party party; 
	private Player player;
    private boolean cancelled;

    public boolean isCancelled() {
        return cancelled;
    }
 
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
    
	public HandlerList getHandlers() {
	    return handlers;
	}
	 
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	public PartyTryLeaveEvent( Party party, Player player ) {
		this.party = party; 
		this.player = player;
		
	}
	
	public Party getParty() {
		return party;
	}
	 
	
	public Player getPlayer() {
		return player;
	}
}
