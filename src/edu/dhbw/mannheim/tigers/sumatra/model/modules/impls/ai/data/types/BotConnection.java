/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.06.2011
 * Author(s): Malte
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types;

/**
 * Holder for state flags, indicating changes regarding the bots on the field.
 * 
 * @author Malte
 */
public class BotConnection
{
	private final boolean	tigerConnected;
	private final boolean	tigerDisconnected;
	private final boolean	tigerAdded;
	private final boolean	tigerRemoved;
	private final boolean	enemyAdded;
	private final boolean	enemyRemoved;
	
	
	public BotConnection(boolean tigerConnected, boolean tigerDisconnected, boolean tigerAdded, boolean tigerRemoved,
			boolean enemyAdded, boolean enemyRemoved)
	{
		this.tigerConnected = tigerConnected;
		this.tigerDisconnected = tigerDisconnected;
		this.tigerAdded = tigerAdded;
		this.tigerRemoved = tigerRemoved;
		this.enemyAdded = enemyAdded;
		this.enemyRemoved = enemyRemoved;
	}
	
	@Override
	public String toString()
	{
		String s = "";
		s += "\ttigerConnected: "+tigerConnected+"\n";
		s += "\ttigerDisconnected: "+tigerDisconnected+"\n"; 
		s += "\ttigerAdded: "+tigerAdded+"\n"; 
		s += "\ttigerRemoved: "+tigerRemoved+"\n"; 
		s += "\tenemyAdded: "+enemyAdded+"\n"; 
		s += "\tenemyRemoved: "+enemyRemoved+"\n";
		return s;
	}
	

	public boolean isTigerConnected()
	{
		return tigerConnected;
	}
	

	public boolean isTigerDisconnected()
	{
		return tigerDisconnected;
	}
	

	public boolean isTigerAdded()
	{
		return tigerAdded;
	}
	

	public boolean isTigerRemoved()
	{
		return tigerRemoved;
	}
	

	public boolean isEnemyAdded()
	{
		return enemyAdded;
	}
	

	public boolean isEnemyRemoved()
	{
		return enemyRemoved;
	}
	
	public boolean isSomethingTrue()
	{
		return enemyAdded | enemyRemoved | tigerAdded | tigerConnected | tigerDisconnected | tigerRemoved;
	}
	

}
