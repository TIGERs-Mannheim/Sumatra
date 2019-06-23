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

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.TigerDevices;


/**
 * Bot base class.
 * 
 * @author AndreR
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
	private String											controlledBy	= "";
	
	private final Map<EFeature, EFeatureState>	botFeatures;
	private int												kickerMaxCap	= 0;
	
	private final Set<IBotObserver>					observers		= new HashSet<IBotObserver>();
	
	private final TigerDevices							devices;
	
	private int												baseStationKey;
	private int												mcastDelegateKey;
	
	/** special treatment, when bot is manual controlled */
	private transient boolean							manualControl	= false;
	
	
	// --------------------------------------------------------------
	// --- constructor(s) -------------------------------------------
	// --------------------------------------------------------------
	/**
	 * @param botConfig bot-database-XML-file
	 */
	public ABot(final SubnodeConfiguration botConfig)
	{
		// --- set default values of botDB ---
		String strColor = botConfig.getString("color");
		final ETeamColor color;
		if (strColor == null)
		{
			color = ETeamColor.YELLOW;
		} else
		{
			color = ETeamColor.valueOf(strColor);
		}
		botId = BotID.createBotId(botConfig.getInt("[@id]"), color);
		
		botFeatures = getDefaultFeatureStates();
		readFeatures(botConfig);
		
		type = EBotType.getTypeFromCfgName(botConfig.getString(TYPE));
		name = botConfig.getString("name");
		
		devices = new TigerDevices();
		
		if (botConfig.containsKey("baseStationKey"))
		{
			baseStationKey = botConfig.getInt("baseStationKey");
		} else
		{
			baseStationKey = -1;
		}
		if (botConfig.containsKey("mcastDelegateKey"))
		{
			mcastDelegateKey = botConfig.getInt("mcastDelegateKey");
		} else
		{
			mcastDelegateKey = -1;
		}
	}
	
	
	/**
	 * @param type
	 * @param id
	 * @param baseStationKey
	 * @param mcastDelegateKey
	 */
	public ABot(final EBotType type, final BotID id, final int baseStationKey, final int mcastDelegateKey)
	{
		botId = id;
		this.type = type;
		botFeatures = getDefaultFeatureStates();
		
		devices = new TigerDevices();
		this.baseStationKey = baseStationKey;
		this.mcastDelegateKey = mcastDelegateKey;
	}
	
	
	private void readFeatures(final SubnodeConfiguration botConfig)
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
	public abstract float getBatteryLevelMax();
	
	
	/**
	 * @return [V]
	 */
	public abstract float getBatteryLevelMin();
	
	
	/**
	 * @return [V]
	 */
	public abstract float getKickerLevel();
	
	
	/**
	 * @return [V]
	 */
	public abstract float getKickerLevelMax();
	
	
	/**
	 * @return
	 */
	public abstract ENetworkState getNetworkState();
	
	
	// --------------------------------------------------------------
	// --- setter/getter --------------------------------------------
	// --------------------------------------------------------------
	/**
	 * @param o
	 */
	public void addObserver(final IBotObserver o)
	{
		synchronized (observers)
		{
			observers.add(o);
		}
	}
	
	
	protected boolean addObserverIfNotPresent(final IBotObserver o)
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
	 * @param o
	 */
	public void removeObserver(final IBotObserver o)
	{
		synchronized (observers)
		{
			observers.remove(o);
		}
	}
	
	
	/**
	 * @param newId
	 */
	public void internalSetBotId(final BotID newId)
	{
		final BotID oldId = botId;
		
		botId = newId;
		
		notifyIdChanged(oldId, newId);
	}
	
	
	/**
	 * @return
	 */
	public HierarchicalConfiguration getConfiguration()
	{
		final HierarchicalConfiguration config = new HierarchicalConfiguration();
		
		config.addProperty("bot[@id]", botId.getNumber());
		config.addProperty("bot.name", name);
		config.addProperty("bot.type", type.getCfgName());
		config.addProperty("bot.color", botId.getTeamColor());
		config.addProperty("bot.mcastDelegateKey", mcastDelegateKey);
		config.addProperty("bot.baseStationKey", baseStationKey);
		
		for (Map.Entry<EFeature, EFeatureState> entry : getBotFeatures().entrySet())
		{
			config.addProperty("bot.features." + entry.getKey(), entry.getValue());
		}
		
		return config;
	}
	
	
	/**
	 * @return
	 */
	public EBotType getType()
	{
		return type;
	}
	
	
	/**
	 * @return
	 */
	public String getName()
	{
		return name;
	}
	
	
	/**
	 * @param name
	 */
	public void setName(final String name)
	{
		this.name = name;
		
		notifyNameChanged();
	}
	
	
	/**
	 * @return
	 */
	public BotID getBotID()
	{
		return botId;
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
	
	
	protected void notifyIdChanged(final BotID oldId, final BotID newId)
	{
		synchronized (observers)
		{
			for (final IBotObserver o : observers)
			{
				o.onIdChanged(oldId, newId);
			}
		}
	}
	
	
	protected void notifyNetworkStateChanged(final ENetworkState state)
	{
		synchronized (observers)
		{
			for (final IBotObserver o : observers)
			{
				o.onNetworkStateChanged(state);
			}
		}
	}
	
	
	protected void notifyNewSplineData(final SplinePair3D spline)
	{
		synchronized (observers)
		{
			for (final IBotObserver observer : observers)
			{
				observer.onNewSplineData(spline);
			}
		}
	}
	
	
	/**
	 * @param spline
	 */
	public abstract void newSpline(SplinePair3D spline);
	
	
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
	public final void setKickerMaxCap(final int kickerMaxCap)
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
	
	
	protected void notifyBotBlocked(final boolean blocked)
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
	public final void setControlledBy(final String controlledBy)
	{
		this.controlledBy = controlledBy;
		notifyBotBlocked(!controlledBy.isEmpty());
	}
	
	
	/**
	 * @return the color
	 */
	public final ETeamColor getColor()
	{
		return botId.getTeamColor();
	}
	
	
	/**
	 * @return the manualControl
	 */
	public final boolean isManualControl()
	{
		return manualControl;
	}
	
	
	/**
	 * @param manualControl the manualControl to set
	 */
	public final void setManualControl(final boolean manualControl)
	{
		this.manualControl = manualControl;
	}
	
	
	/**
	 * @return the baseStationKey
	 */
	public final int getBaseStationKey()
	{
		return baseStationKey;
	}
	
	
	/**
	 * @return the mcastDelegateKey
	 */
	public final int getMcastDelegateKey()
	{
		return mcastDelegateKey;
	}
	
	
	/**
	 * @param baseStationKey the baseStationKey to set
	 */
	public final void setBaseStationKey(final int baseStationKey)
	{
		this.baseStationKey = baseStationKey;
	}
	
	
	/**
	 * @param mcastDelegateKey the mcastDelegateKey to set
	 */
	public final void setMcastDelegateKey(final int mcastDelegateKey)
	{
		this.mcastDelegateKey = mcastDelegateKey;
	}
}
