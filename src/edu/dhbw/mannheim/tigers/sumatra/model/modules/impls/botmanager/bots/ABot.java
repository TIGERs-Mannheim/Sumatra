/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.TigerDevices;


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
	private static final Logger						log				= Logger.getLogger(ABot.class.getName());
	
	private static final String						TYPE				= "type";
	
	protected BotID										botId;
	private EBotType										type				= EBotType.UNKNOWN;
	protected String										name				= "John Doe";
	protected boolean										active			= true;
	private String											controlledBy	= "";
	
	private final Map<EFeature, EFeatureState>	botFeatures;
	private int												kickerMaxCap	= 0;
	
	private final Set<IBotObserver>					observers		= new HashSet<IBotObserver>();
	
	private final TigerDevices							devices;
	
	
	// --------------------------------------------------------------
	// --- constructor(s) -------------------------------------------
	// --------------------------------------------------------------
	/**
	 * @param botConfig bot-database-XML-file
	 */
	public ABot(SubnodeConfiguration botConfig)
	{
		// --- set default values of botDB ---
		botId = new BotID(botConfig.getInt("[@id]"));
		active = botConfig.getBoolean("active", true);
		
		botFeatures = getDefaultFeatureStates();
		readFeatures(botConfig);
		
		type = EBotType.getTypeFromCfgName(botConfig.getString(TYPE));
		name = botConfig.getString("name");
		
		devices = new TigerDevices(type);
	}
	
	
	/**
	 * @param type
	 * @param id
	 */
	public ABot(EBotType type, BotID id)
	{
		botId = id;
		this.type = type;
		botFeatures = getDefaultFeatureStates();
		
		devices = new TigerDevices(type);
	}
	
	
	private void readFeatures(SubnodeConfiguration botConfig)
	{
		final List<?> features = botConfig.configurationsAt("features");
		for (final Object obj : features)
		{
			if (!(obj instanceof SubnodeConfiguration))
			{
				log.warn("Unexpected state: object is no SubnodeConfiguration");
				continue;
			}
			final SubnodeConfiguration featureConfig = (SubnodeConfiguration) obj;
			
			List<ConfigurationNode> nodes = featureConfig.getRoot().getChildren();
			for (ConfigurationNode node : nodes)
			{
				String key = node.getName();
				String value = featureConfig.getString(key);
				
				try
				{
					EFeature feature = EFeature.valueOf(key);
					EFeatureState state = EFeatureState.valueOf(value);
					botFeatures.put(feature, state);
				} catch (IllegalArgumentException e)
				{
					log.error("Could not parse feature type or state in config file. key=" + key + " value=" + value);
				}
			}
		}
	}
	
	
	// --------------------------------------------------------------
	// --- abstract-methods -----------------------------------------
	// --------------------------------------------------------------
	
	protected abstract Map<EFeature, EFeatureState> getDefaultFeatureStates();
	
	
	/**
	 * 
	 * @param cmd
	 */
	public abstract void execute(ACommand cmd);
	
	
	/**
	 * 
	 */
	public abstract void start();
	
	
	/**
	 * 
	 */
	public abstract void stop();
	
	
	/**
	 * @return [V]
	 */
	public abstract float getBatteryLevel();
	
	
	/**
	 * @return [V]
	 */
	public abstract float getKickerLevel();
	
	
	/**
	 * @return
	 */
	public abstract ENetworkState getNetworkState();
	
	
	// --------------------------------------------------------------
	// --- setter/getter --------------------------------------------
	// --------------------------------------------------------------
	/**
	 * 
	 * @param active
	 */
	public void setActive(boolean active)
	{
		this.active = active;
	}
	
	
	/**
	 * 
	 * @param o
	 */
	public void addObserver(IBotObserver o)
	{
		synchronized (observers)
		{
			observers.add(o);
		}
	}
	
	
	protected boolean addObserverIfNotPresent(IBotObserver o)
	{
		synchronized (observers)
		{
			if (observers.contains(o))
			{
				return false;
			}
			return observers.add(o);
		}
	}
	
	
	/**
	 * 
	 * @param o
	 */
	public void removeObserver(IBotObserver o)
	{
		synchronized (observers)
		{
			observers.remove(o);
		}
	}
	
	
	/**
	 * 
	 * @param newId
	 */
	public void internalSetBotId(BotID newId)
	{
		final BotID oldId = botId;
		
		botId = newId;
		
		notifyIdChanged(oldId, newId);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public HierarchicalConfiguration getConfiguration()
	{
		final HierarchicalConfiguration config = new HierarchicalConfiguration();
		
		config.addProperty("bot[@id]", botId.getNumber());
		config.addProperty("bot.name", name);
		config.addProperty("bot.type", type.getCfgName());
		config.addProperty("bot.active", active);
		
		for (Map.Entry<EFeature, EFeatureState> entry : getBotFeatures().entrySet())
		{
			config.addProperty("bot.features." + entry.getKey(), entry.getValue());
		}
		
		return config;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public EBotType getType()
	{
		return type;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String getName()
	{
		return name;
	}
	
	
	/**
	 * 
	 * @param name
	 */
	public void setName(String name)
	{
		this.name = name;
		
		notifyNameChanged();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public BotID getBotID()
	{
		return botId;
	}
	
	
	/**
	 * 
	 * @return
	 */
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
		synchronized (observers)
		{
			for (final IBotObserver o : observers)
			{
				o.onNameChanged(name);
			}
		}
	}
	
	
	protected void notifyIdChanged(BotID oldId, BotID newId)
	{
		synchronized (observers)
		{
			for (final IBotObserver o : observers)
			{
				o.onIdChanged(oldId, newId);
			}
		}
	}
	
	
	protected void notifyNetworkStateChanged(ENetworkState state)
	{
		synchronized (observers)
		{
			for (final IBotObserver o : observers)
			{
				o.onNetworkStateChanged(state);
			}
		}
	}
	
	
	/**
	 * @return the botFeatures
	 */
	public final Map<EFeature, EFeatureState> getBotFeatures()
	{
		return botFeatures;
	}
	
	
	/**
	 * @return the kickerMaxCap
	 */
	public final int getKickerMaxCap()
	{
		return kickerMaxCap;
	}
	
	
	/**
	 * @param kickerMaxCap the kickerMaxCap to set
	 */
	public final void setKickerMaxCap(int kickerMaxCap)
	{
		this.kickerMaxCap = kickerMaxCap;
	}
	
	
	/**
	 * @return the devices
	 */
	public final TigerDevices getDevices()
	{
		return devices;
	}
	
	
	protected void notifyBotBlocked(boolean blocked)
	{
		synchronized (observers)
		{
			for (final IBotObserver o : observers)
			{
				o.onBlocked(blocked);
			}
		}
	}
	
	
	/**
	 * @return the controlledBy
	 */
	public final String getControlledBy()
	{
		return controlledBy;
	}
	
	
	/**
	 * @param controlledBy the controlledBy to set
	 */
	public final void setControlledBy(String controlledBy)
	{
		this.controlledBy = controlledBy;
		notifyBotBlocked(!controlledBy.isEmpty());
	}
}
