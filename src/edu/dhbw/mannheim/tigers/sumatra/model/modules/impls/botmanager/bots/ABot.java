/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ITransceiver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;


/**
 * Bot base class.
 * 
 * @author AndreR
 * 
 */
public abstract class ABot
{
	// --------------------------------------------------------------
	// --- variables ------------------------------------------------
	// --------------------------------------------------------------
	protected int					botId;
	protected String				ip				= "";
	protected int					port			= 0;
	protected EBotType			type			= EBotType.UNKNOWN;
	protected String				name			= "John Doe";
	protected boolean				active		= true;

	private List<IBotObserver> observers = new ArrayList<IBotObserver>();
	
	// --------------------------------------------------------------
	// --- constructor(s) -------------------------------------------
	// --------------------------------------------------------------
	/**
	 * @param botConfig bot-database-XML-file
	 */
	public ABot(SubnodeConfiguration botConfig)
	{
		// --- set default values of botDB ---
		this.botId = botConfig.getInt("[@id]");
		this.ip = botConfig.getString("ip");
		this.port = botConfig.getInt("port");
		this.active = botConfig.getBoolean("active", true);
		
		if (botConfig.getString("type").equals("CtBot"))
		{
			this.type = EBotType.CT;
		}
		if (botConfig.getString("type").equals("SysoutBot"))
		{
			this.type = EBotType.SYSOUT;
		}
		if (botConfig.getString("type").equals("TigerBot"))
		{
			this.type = EBotType.TIGER;
		}
		
		this.name = botConfig.getString("name");
	}
	
	public ABot(EBotType type, int id)
	{
		this.botId = id;
		this.type = type;
	}

	// --------------------------------------------------------------
	// --- abstract-methods -----------------------------------------
	// --------------------------------------------------------------
	public abstract void execute(ACommand cmd);
	public abstract void start();
	public abstract void stop();
	public abstract ITransceiver getTransceiver();

	// --------------------------------------------------------------
	// --- setter/getter --------------------------------------------
	// --------------------------------------------------------------
	public void setActive(boolean active)
	{
		this.active = active;
	}	
	
	public void addObserver(IBotObserver o)
	{
		synchronized(observers)
		{
			observers.add(o);
		}
	}
	
	protected boolean addObserverIfNotPresent(IBotObserver o)
	{
		synchronized(observers)
		{
			if (observers.contains(o))
			{
				return false;
			}
			return observers.add(o);
		}
	}
	
	public void removeObserver(IBotObserver o)
	{
		synchronized(observers)
		{
			observers.remove(o);
		}
	}
	
	public void internalSetBotId(int newId)
	{
		int oldId = botId;
		
		botId = newId;
		
		notifyIdChanged(oldId, newId);
	}
	
	public HierarchicalConfiguration getConfiguration()
	{
		HierarchicalConfiguration config = new HierarchicalConfiguration();
		
		String type = "Unknown";
		
		switch(this.type)
		{
			case CT:
				type = "CtBot";
				break;
			case SYSOUT:
				type = "SysoutBot";
				break;
			case TIGER:
				type = "TigerBot";
				break;
		}
		
		config.addProperty("bot[@id]", botId);
		config.addProperty("bot.name", name);
		config.addProperty("bot.type", type);
		config.addProperty("bot.ip", ip);
		config.addProperty("bot.port", port);
		config.addProperty("bot.active", active);
		
		return config;
	}
	
	public EBotType getType()
	{
		return type;
	}	

	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
		
		notifyNameChanged();
	}

	public int getBotId()
	{
		return botId;
	}	

	public String getIp()
	{
		return ip;
	}
	
	public void setIp(String ip)
	{
		this.ip = ip;

		notifyIpChanged();
	}
	
	public int getPort()
	{
		return port;
	}
	
	public void setPort(int port)
	{
		this.port = port;
		
		notifyPortChanged();
	}
	
	public float getMaxSpeed(float angle)
	{
		return 0;
	}	

	public float getMaxAngularVelocity()
	{
		return 0;
	}
	
	public boolean isActive()
	{
		return active;
	}
	
	@Override
	public String toString()
	{
		return "[Bot: " + getName() + "|" + botId + "]";
	}
	
	protected void notifyNameChanged()
	{	
		synchronized(observers)
		{
			for(IBotObserver o : observers)
			{
				o.onNameChanged(name);
			}
		}
	}
	
	protected void notifyIdChanged(int oldId, int newId)
	{
		synchronized(observers)
		{
			for(IBotObserver o : observers)
			{
				o.onIdChanged(oldId, newId);
			}
		}
	}
	
	protected void notifyIpChanged()
	{
		synchronized(observers)
		{
			for(IBotObserver o : observers)
			{
				o.onIpChanged(ip);
			}
		}
	}
	
	protected void notifyPortChanged()
	{
		synchronized(observers)
		{
			for(IBotObserver o : observers)
			{
				o.onPortChanged(port);
			}
		}
	}
	
	protected void notifyNetworkStateChanged(ENetworkState state)
	{
		synchronized (observers)
		{
			for(IBotObserver o : observers)
			{
				o.onNetworkStateChanged(state);
			}
		}
	}
}
