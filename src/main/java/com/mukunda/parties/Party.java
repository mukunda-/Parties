package com.mukunda.parties;
 
import java.util.ArrayList; 
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player; 

//---------------------------------------------------------------------------------------------
public class Party {
	
	public ArrayList<UUID> players;
	public UUID leader; 
	
	public static final int MAX_PLAYERS = 10;
	
	public boolean stale; // if all members leave the party
	// this is so players can't join an empty party and such.
	
	CustomBoard board;
	
	public Party() {
		leader = null;
		players = new ArrayList<UUID>();
		board = new CustomBoard( ChatColor.GOLD.toString() + ChatColor.BOLD  + "Party" ); 
		stale = false;
	}
	
	//---------------------------------------------------------------------------------------------
	public boolean isFull() {
		return players.size() >= MAX_PLAYERS;
	}
	
	//---------------------------------------------------------------------------------------------
	public void message( String text ) {
		for( UUID id : players ) {
			Player p = Bukkit.getPlayer( id );
			if( p != null ) {
				p.sendMessage( text );
			}
		}
	}
	
	//---------------------------------------------------------------------------------------------
	public void add( Player p ) {
		
		UUID pid = p.getUniqueId();
		for( UUID id : players ) {
			if( pid.equals(id) ) return; // already in party
		}
		message( p.getDisplayName() + ChatColor.GOLD + " has joined the party." );
		players.add( p.getUniqueId() ); 
		Bukkit.getServer().getPluginManager().callEvent( 
				new PartyEvent( this, PartyEvent.Type.JOIN, pid ) );
		
		if( leader == null ) {
			leader = pid;
			Parties.getInstance().parties.add( this );
			 
			Bukkit.getServer().getPluginManager().callEvent( 
					new PartyEvent( this, PartyEvent.Type.LEADER, leader ) );
			
		}
		
		updatePanel();
	}
	
	//---------------------------------------------------------------------------------------------
	public void remove( UUID pid ) {
		
		OfflinePlayer p = Bukkit.getOfflinePlayer( pid );
		if( p.isOnline() ) {
			p.getPlayer().setScoreboard( Bukkit.getScoreboardManager().getNewScoreboard() );
		}
		
		players.remove( pid );
		Bukkit.getServer().getPluginManager().callEvent( 
				new PartyEvent( this, PartyEvent.Type.LEAVE, pid ) );
		
		if( players.isEmpty() ) {
			
			Parties.getInstance().parties.remove( this );
			leader = null;
			stale = true; 
			
			Bukkit.getServer().getPluginManager().callEvent( 
					new PartyEvent( this, PartyEvent.Type.STALE, null ) );
			return;
		}
		
		if( pid.equals(leader) ) {
			
			setLeader( players.get(0) );
			 
			Bukkit.getServer().getPluginManager().callEvent( 
					new PartyEvent( this, PartyEvent.Type.LEADER, leader ) );
			
		}
		
		
		message( ((!p.isOnline()) ? p.getName() : p.getPlayer().getDisplayName())
				+ ChatColor.GOLD + " has left the party." );
		updatePanel();
	}
	
	//---------------------------------------------------------------------------------------------
	public void remove( Player p ) {
		remove( p.getUniqueId() );
		
	}
	
	//---------------------------------------------------------------------------------------------
	public void setLeader( UUID p ) {
		leader = p;
		if( p != null ) {
			OfflinePlayer player = Bukkit.getOfflinePlayer( p );
			message( 
					((!player.isOnline()) ? player.getName() : player.getPlayer().getDisplayName()) 
					+ " is now the party leader." );
		}
	}
	
	//---------------------------------------------------------------------------------------------
	public boolean contains( UUID uuid ) {
		return players.contains( uuid );
		
	}
	
	//---------------------------------------------------------------------------------------------
	public void updatePanel() {
		board.clear(); 
		
		if( players.size() >= 1 ) {
			for( UUID id: players ) {
				OfflinePlayer p = Bukkit.getOfflinePlayer( id );
				if( p.isOnline() ) {
					int health =  Math.min(99, (int)Math.ceil(p.getPlayer().getHealth() * 5)) ;
					if( id.equals(leader) && health > 20 ) {
						board.add( ChatColor.GREEN + p.getName(), health);
					} else if( health > 20 ) {
						board.add( ChatColor.WHITE + p.getName(), health);
					} else if( health > 0 ) {
						board.add( ChatColor.RED + p.getName(), health);
					} else {
						board.add( ChatColor.DARK_GRAY + p.getName() + " (dead)", 0);
					}
				} else {
					board.add( ChatColor.DARK_GRAY + p.getName() + " (offline)", 0 );
				} 
			}
			
			 
		}
		for( UUID id: players ) {
			OfflinePlayer p = Bukkit.getOfflinePlayer( id );
			if( p.isOnline() ) {
				p.getPlayer().setScoreboard( board.getScoreboard() );
			}
		}
	}
	
	public void updatePanelDelayed() {
		updatePanelDelayed(2);
	}
	
	public void updatePanelDelayed( int delay ) {
		Bukkit.getScheduler().scheduleSyncDelayedTask( Parties.getInstance(), new Runnable() {
			public void run() {
				updatePanel(); 
			}
		}, delay );
	} 
}
