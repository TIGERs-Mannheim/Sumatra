/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.06.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai;

import java.io.Serializable;

import javax.persistence.Embeddable;


/**
 * Holder for state flags, indicating changes regarding the bots on the field.
 * 
 * @author Malte
 */
@Embeddable
public class BotConnection implements Serializable
{
	/**  */
	private static final long	serialVersionUID	= -7262315920007355219L;
	
	
	private boolean				tigerConnected;
	private boolean				tigerDisconnected;
	private boolean				tigerAdded;
	private boolean				tigerRemoved;
	private boolean				enemyAdded;
	private boolean				enemyRemoved;
	
	
	/**
	 * @param tigerConnected
	 * @param tigerDisconnected
	 * @param tigerAdded
	 * @param tigerRemoved
	 * @param enemyAdded
	 * @param enemyRemoved
	 */
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
		s += "\ttigerConnected: " + tigerConnected + "\n";
		s += "\ttigerDisconnected: " + tigerDisconnected + "\n";
		s += "\ttigerAdded: " + tigerAdded + "\n";
		s += "\ttigerRemoved: " + tigerRemoved + "\n";
		s += "\tenemyAdded: " + enemyAdded + "\n";
		s += "\tenemyRemoved: " + enemyRemoved + "\n";
		return s;
	}
	
	
	/**
	 * Is a new bot connected?
	 * 
	 * @return
	 */
	public boolean isTigerConnected()
	{
		return tigerConnected;
	}
	
	
	/**
	 * @return
	 */
	public boolean isTigerDisconnected()
	{
		return tigerDisconnected;
	}
	
	
	/**
	 * Did a new tiger appeared? Might not be connected.
	 * 
	 * @return
	 */
	public boolean isTigerAdded()
	{
		return tigerAdded;
	}
	
	
	/**
	 * @return
	 */
	public boolean isTigerRemoved()
	{
		return tigerRemoved;
	}
	
	
	/**
	 * @return
	 */
	public boolean isEnemyAdded()
	{
		return enemyAdded;
	}
	
	
	/**
	 * @return
	 */
	public boolean isEnemyRemoved()
	{
		return enemyRemoved;
	}
	
	
	/**
	 * @return
	 */
	public boolean isSomethingTrue()
	{
		return enemyAdded | enemyRemoved | tigerAdded | tigerConnected | tigerDisconnected | tigerRemoved;
	}
	
	
}
