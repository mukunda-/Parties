package com.mukunda.parties;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard; 

public class CustomBoard {
	/*public class Entry {
		String last_value;
		int number;
	} */

	public Scoreboard board;
	public Objective objective;
	ArrayList<String> list;
	
	//---------------------------------------------------------------------------------------------
	public CustomBoard( String name ) {
		list = new ArrayList<String>();
		board = Bukkit.getScoreboardManager().getNewScoreboard();
		objective = board.registerNewObjective( name, "dummy");
		objective.setDisplayName( name );
		objective.setDisplaySlot( DisplaySlot.SIDEBAR ); 
	}
	
	//---------------------------------------------------------------------------------------------
	public void clear() {
		for( String a: list ) {
			board.resetScores( a );
		}
		list.clear();
	}
	
	//---------------------------------------------------------------------------------------------
	public void add( String text, int number ) {
		if( text.length() > 16 ) {
			text = text.substring(0, 16);
		}
		Score score = objective.getScore( text );
		score.setScore( number );
		list.add( text );
	}
	
	//---------------------------------------------------------------------------------------------
	public Scoreboard getScoreboard() {
		return board;
	}
}
