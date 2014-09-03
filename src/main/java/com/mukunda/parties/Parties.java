package com.mukunda.parties;

import java.util.ArrayList;
import java.util.HashMap; 
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor; 
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler; 
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent; 
import org.bukkit.event.player.PlayerJoinEvent; 
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent; 
import org.bukkit.plugin.java.JavaPlugin; 

public class Parties extends JavaPlugin implements Listener {
	
	public HashMap<UUID,Party> partyMap;
	public HashMap<UUID,PartyInvite> partyInvites;
	
	public static Parties instance;
	
	public ArrayList<Party> parties;
	 
	
	public static Parties getInstance() {
		return instance;
	}
	
	//---------------------------------------------------------------------------------------------
	@Override
	public void onEnable() {
		instance = this;
		
		partyMap = new HashMap<UUID,Party>();
		partyInvites = new HashMap<UUID,PartyInvite>();
		parties = new ArrayList<Party>(); 
	    getServer().getPluginManager().registerEvents(this, this); 
		 
	}
	
	//---------------------------------------------------------------------------------------------
	@Override
	public void onDisable() {
		
		instance = null;
	}
	/*
	//---------------------------------------------------------------------------------------------
	public Object getMetadata( Metadatable object, String key  ) {
		List<MetadataValue> values = object.getMetadata( key );
		if( values == null ) return null;
		for( MetadataValue value : values ) {
			 if( value.getOwningPlugin() == this ) {
		        return (Party)value.value();
		     }	
		}  
		return null;	
	}*/
	
	//---------------------------------------------------------------------------------------------
	public Party getParty( Player player ) {
		return getParty( player.getUniqueId() ); 
	}
	//---------------------------------------------------------------------------------------------
	public Party getParty( UUID pid ) {
		return partyMap.get( pid ); 
	}
	
	//---------------------------------------------------------------------------------------------
	public boolean hasPartyWithPeople( Player player ) {
		Party party = getParty( player );
		if( party == null ) return false;
		if( party.players.size() > 1 ) return true;
		return false;
	}
	
	public void ensureInParty( Player player ) {
		if( getParty(player.getUniqueId()) == null ) {
			joinParty( player, new Party() );
		} 
	}
	
	//---------------------------------------------------------------------------------------------
	private void leaveParty( Player player, boolean auto  ) { 
		Party party = getParty( player.getUniqueId() );
		if( party == null ) {
			if( !auto ) player.sendMessage( ChatColor.GOLD + "You aren't in a party." );
			return;
		}  
		
		PartyTryLeaveEvent event = new PartyTryLeaveEvent( party, player );
		Bukkit.getPluginManager().callEvent( event );
		if( event.isCancelled() ) return;
		
		party.remove( player );
		partyMap.remove( player.getUniqueId() );
		if( !auto ) player.sendMessage( ChatColor.GOLD + "You left the party." );
		 
	}
	
	//---------------------------------------------------------------------------------------------
	private void leaveParty( Player player  ) { 
		leaveParty(player,false);		
	}
	
	//---------------------------------------------------------------------------------------------
	private void joinParty( Player player, Party party ) {
		if( party.isFull() ) {
			player.sendMessage ( ChatColor.GOLD + "That party is full." );
			return;
			
		}
		leaveParty( player, true );
		party.add(player );
		partyMap.put(  player.getUniqueId(), party );
		 
		player.sendMessage ( ChatColor.GOLD + "You joined a party." );
	}
	
	//---------------------------------------------------------------------------------------------
	private PartyInvite getPartyInvite( Player player ) {
		UUID id = player.getUniqueId();
		PartyInvite invitation = partyInvites.get( id );
		return invitation;
	}
	
	//---------------------------------------------------------------------------------------------
	private void sendPartyInvite( Player from, Player player ) {
		Party party = getParty(from.getUniqueId());
		if( party == null ) {
			from.sendMessage( ChatColor.GOLD + "You aren't in a party." );
			return;
		}
		if( party != null && from.getUniqueId() != party.leader ) {
			from.sendMessage( ChatColor.GOLD + "Only the party leader can invite people." );
			return;
		}
		
		if( party.isFull() ) {
			from.sendMessage( ChatColor.GOLD + "Your party is full." );
			return;
		}
		
		partyInvites.put( player.getUniqueId(), new PartyInvite( party ) );
		
		if( hasPartyWithPeople(player) ) {
			player.sendMessage( 
					from.getDisplayName() + 
					ChatColor.GOLD + " has invited you to their party. Type " 
					+ChatColor.WHITE +  "/join" +ChatColor.GOLD + " to leave your party and join theirs." );			
		} else {
			player.sendMessage( 
					from.getDisplayName() + 
					ChatColor.GOLD + " has invited you to their party. Type " 
					+ChatColor.WHITE +  "/join" +ChatColor.GOLD + " to accept." );
		}
		
		
		party.message( player.getDisplayName() + ChatColor.GOLD + " was invited to the party." );
	}
	
	private void acceptPartyInvite( Player player ) {
		PartyInvite request = getPartyInvite( player );
		if( request == null ) {
			player.sendMessage( ChatColor.GOLD + "You don't have a pending invite." );
			return;
		}
		
		if( request.isValid() ) {
			if( request.party.isFull() ) {
				player.sendMessage( ChatColor.GOLD + "The party is full." );
			
			} else {
				joinParty( player, request.party );
			}
			
			
		} else {
			player.sendMessage( ChatColor.GOLD + "Your invitation expired." );
			
		}
		partyInvites.remove( player.getUniqueId() );
	}
	
	//---------------------------------------------------------------------------------------------
	private void onInviteCommand( Player player,  String[] args ) {
		if( args.length < 1 ) {
			player.sendMessage( "/invite <player> - Invite someone to your party." );
			return;
		}
		@SuppressWarnings("deprecation")
		Player target = Bukkit.getPlayer( args[0] );
		if( target == null ) {
			player.sendMessage( ChatColor.GOLD + "Player not found." );
			return;
		} 
		
		if( target == player ) {
			player.sendMessage( ChatColor.GOLD + "You can't invite yourself." );
			return;
		}
		
		Party party = getParty(player.getUniqueId());
		if( party == null ) {
			party = new Party();
			joinParty( player, party );
		} 
		 
		sendPartyInvite( player, target );
	}
	
	//---------------------------------------------------------------------------------------------
	private void onLeaderCommand( Player player, String[] args ) {
		Party party = getParty(player);
		if( party == null ) {
			player.sendMessage( ChatColor.GOLD + "You aren't in a party." );
			return;
		}
		
		if( player.getUniqueId() != party.leader ) {
			player.sendMessage( ChatColor.GOLD + "You aren't the party leader." );
			return;
		}
		
		if( args.length < 1 ) {
			player.sendMessage( "/leader <player> - Give party leadership to someone else." );
			return;
		}
		
		@SuppressWarnings("deprecation")
		Player target = Bukkit.getPlayer( args[0] );
		if( target == null ) {
			player.sendMessage( ChatColor.GOLD + "Player not found." );
			return;
		}
		if( target == player ) {
			player.sendMessage( ChatColor.GOLD + "You are the leader." );
			return;
		}
		
		if( getParty(target) != party ) {
			player.sendMessage( "That person isn't in your party." );
			return;
		}
		
		party.setLeader( target.getUniqueId() );
		
	}

    //---------------------------------------------------------------------------------------------
    @Override
    public boolean onCommand( CommandSender sender, Command cmd, String label, String[] args ) {
    	if( cmd.getName().equalsIgnoreCase("ptest") ) { // TODO remove debug
    		 
 
    		return true;
    	} else if( cmd.getName().equalsIgnoreCase("invite") ) {
    		if( !(sender instanceof Player) ) return true;
    		onInviteCommand( (Player)sender, args ); 
    		return true;
    	} else if( cmd.getName().equalsIgnoreCase("join" ) ) {
    		
    		if( !(sender instanceof Player) ) return true;
    		
    		acceptPartyInvite( (Player)sender );
    		/*
    		Player player = (Player)sender;
    		Party party = new Party();
    		parties.add( party );
    		joinParty( player, party );
    		*/
        	return true;
    	} else if( cmd.getName().equalsIgnoreCase("leave") ) {
    		if( !(sender instanceof Player) ) return true;
    		Player player = (Player)sender;
    		
    		
    		
    		leaveParty( player );
        	return true;
    	} else if( cmd.getName().equalsIgnoreCase("leader") ) {
    		if( !(sender instanceof Player) ) return true;
    		Player player = (Player)sender;
    		onLeaderCommand( player, args );
    		return true;
    	}
    	return false;
    }
    //---------------------------------------------------------------------------------------------
 
    
    @EventHandler 
    public void onEvent( EntityRegainHealthEvent event ) {
    	if( event.getEntityType() == EntityType.PLAYER ) {
    		Player p = (Player)event.getEntity();
    		Party party = getParty( p.getUniqueId() );
    		if( party != null ) {
    			party.updatePanelDelayed();
    		}
    	}  
    }
    @EventHandler 
    public void onEvent( EntityDamageEvent event ) {
    	if( event.getEntityType() == EntityType.PLAYER ) {
    		Player p = (Player)event.getEntity();
    		Party party = getParty( p.getUniqueId() );

    		if( party != null ) {
    			party.updatePanelDelayed();
    		}
    	}  
    }
    @EventHandler 
    public void onEvent( PlayerDeathEvent event ) {
		Player p = event.getEntity();
		Party party = getParty( p.getUniqueId() );

		if( party != null ) {
			party.updatePanelDelayed(10);
		}
    }
    @EventHandler 
    public void onEvent( PlayerRespawnEvent event ) {
		Player p = event.getPlayer();
		Party party = getParty( p.getUniqueId() );

		if( party != null ) {
			party.updatePanelDelayed();
		}
    }
    
    @EventHandler 
    public void onEvent( PlayerQuitEvent event ) {
		Player p = event.getPlayer();
		Party party = getParty( p.getUniqueId() );

		if( party != null ) {
			party.updatePanelDelayed();
		}
    }
    @EventHandler 
    public void onEvent( PlayerJoinEvent event ) {
		Player p = event.getPlayer();
		Party party = getParty( p.getUniqueId() );

		if( party != null ) {
			party.updatePanelDelayed();
		}
    }
}
