/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.frames;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordWfFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMapConst;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;


/**
 * This is a data holder between the {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor} and
 * the {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent}, which contains all data concerning
 * the current situation on the field.
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 * @author Gero
 */
public class WorldFrame extends SimpleWorldFrame implements IRecordWfFrame
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** our enemies visible */
	public final BotIDMapConst<TrackedTigerBot>	foeBots;
	
	/** tiger bots that were detected by the WorldPredictor */
	public final BotIDMapConst<TrackedTigerBot>	tigerBotsVisible;
	/** tiger bots that were detected by the WorldPredictor AND are connected */
	public final IBotIDMap<TrackedTigerBot>		tigerBotsAvailable;
	
	
	private final ETeamColor							teamColor;
	
	private final boolean								inverted;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param simpleWorldFrame
	 * @param teamColor
	 * @param invert
	 */
	public WorldFrame(final SimpleWorldFrame simpleWorldFrame, final ETeamColor teamColor, final boolean invert)
	{
		super(simpleWorldFrame);
		this.teamColor = teamColor;
		inverted = invert;
		
		BotIDMap<TrackedTigerBot> foes = new BotIDMap<TrackedTigerBot>();
		BotIDMap<TrackedTigerBot> tigersVisible = new BotIDMap<TrackedTigerBot>();
		BotIDMap<TrackedTigerBot> tigersAvailable = new BotIDMap<TrackedTigerBot>();
		for (Map.Entry<BotID, TrackedTigerBot> entry : simpleWorldFrame.getBots().entrySet())
		{
			final BotID botID = entry.getKey();
			TrackedTigerBot bot = entry.getValue();
			
			if (bot.getTeamColor() == getTeamColor())
			{
				tigersVisible.put(botID, bot);
				if ((bot.getBot() != null) && (bot.getBot().getNetworkState() == ENetworkState.ONLINE)
						&& !bot.getBot().isManualControl()
						&& AIConfig.getGeometry().getFieldWBorders().isPointInShape(bot.getPos())
						&& bot.isVisible())
				{
					tigersAvailable.put(botID, bot);
				}
			}
			else
			{
				foes.put(botID, bot);
			}
		}
		foeBots = BotIDMapConst.unmodifiableBotIDMap(foes);
		tigerBotsAvailable = BotIDMapConst.unmodifiableBotIDMap(tigersAvailable);
		tigerBotsVisible = BotIDMapConst.unmodifiableBotIDMap(tigersVisible);
	}
	
	
	/**
	 * Providing a <strong>shallow</strong> copy of original (Thus new collections are created, but filled with the same
	 * values
	 * 
	 * @param original
	 */
	public WorldFrame(final WorldFrame original)
	{
		super(original);
		teamColor = original.teamColor;
		inverted = original.inverted;
		foeBots = BotIDMapConst.unmodifiableBotIDMap(original.foeBots);
		tigerBotsAvailable = original.tigerBotsAvailable;
		tigerBotsVisible = BotIDMapConst.unmodifiableBotIDMap(original.tigerBotsVisible);
	}
	
	
	@Override
	public String toString()
	{
		final StringBuilder b = new StringBuilder();
		b.append("[WorldFrame, id = ").append(getId()).append("|\n");
		b.append("Ball: ").append(getBall().getPos()).append("|\n");
		b.append("Tigers: ");
		for (final TrackedBot tiger : tigerBotsVisible.values())
		{
			b.append(tiger.getPos()).append(",");
		}
		b.append("|\n");
		b.append("Enemies: ");
		for (final TrackedBot bot : foeBots.values())
		{
			b.append(bot.getPos()).append(",");
		}
		b.append("]\n");
		return b.toString();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Get {@link TrackedTigerBot} from current {@link WorldFrame}.
	 * 
	 * @param botId
	 * @return tiger {@link TrackedTigerBot}
	 */
	public TrackedTigerBot getTiger(final BotID botId)
	{
		return tigerBotsVisible.get(botId);
	}
	
	
	/**
	 * Get foe {@link TrackedBot} from current {@link WorldFrame}.
	 * 
	 * @param botId
	 * @return foe {@link TrackedBot}
	 */
	public TrackedBot getFoeBot(final BotID botId)
	{
		return foeBots.get(botId);
	}
	
	
	/**
	 * @return {@link Iterator} for foe bots map
	 */
	public Iterator<Entry<BotID, TrackedTigerBot>> getFoeBotMapIterator()
	{
		return foeBots.entrySet().iterator();
	}
	
	
	// --------------------------------------------------------------------------
	// --- modifier -------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public BotIDMapConst<TrackedTigerBot> getFoeBots()
	{
		return foeBots;
	}
	
	
	@Override
	public BotIDMapConst<TrackedTigerBot> getTigerBotsVisible()
	{
		return tigerBotsVisible;
	}
	
	
	@Override
	public IBotIDMap<TrackedTigerBot> getTigerBotsAvailable()
	{
		return tigerBotsAvailable;
	}
	
	
	@Override
	public final ETeamColor getTeamColor()
	{
		return teamColor;
	}
	
	
	/**
	 * @return the inverted
	 */
	@Override
	public final boolean isInverted()
	{
		return inverted;
	}
	
}
