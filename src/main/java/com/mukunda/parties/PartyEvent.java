package com.mukunda.parties;

import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PartyEvent extends Event {
	public static final HandlerList handlers = new HandlerList();
	
	public enum Type {
		JOIN,
		LEAVE, 
		LEADER,
		STALE,
		TRYLEAVE
	};
	
	private Party party;
	private Type type;
	private UUID player;
	
	public HandlerList getHandlers() {
	    return handlers;
	}
	 
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	public PartyEvent( Party party, Type type, UUID player ) {
		this.party = party;
		this.type = type;
		this.player = player;
		
	}
	
	public Party getParty() {
		return party;
	}
	
	public Type getType() {
		return type;
	}
	
	public UUID getPlayer() {
		return player;
	}
}
