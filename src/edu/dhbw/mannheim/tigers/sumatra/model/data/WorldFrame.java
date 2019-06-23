/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;


/**
 * This is a data holder between the {@link AWorldPredictor} and the {@link AAgent}, which contains all data concerning
 * the current situation on the field.
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * @author Gero
 * 
 */
public class WorldFrame implements Serializable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long							serialVersionUID	= 6550048556640958060L;
	
	/**  */
	public final Map<Integer, TrackedBot>			foeBots;
	private final Map<Integer, TrackedBot>			foeBotsMutable;
	
	/**  */
	public final Map<Integer, TrackedTigerBot>	tigerBots;
	private final Map<Integer, TrackedTigerBot>	tigerBotsMutable;
	
	/**  */
	public final TrackedBall							ball;
	
	/**  */
	public final long										time;
	
	/**  */
	public final FrameID									id;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public WorldFrame(Map<Integer, TrackedBot> foeBots, Map<Integer, TrackedTigerBot> tigerBots, TrackedBall ball,
			double time, long frameNumber, int cameraId)
	{
		this.ball = ball;
		this.time = ((long) (time/WPConfig.FILTER_CONVERT_NS_TO_INTERNAL_TIME))+WPConfig.FILTER_TIME_OFFSET;
		this.id = new FrameID(cameraId, frameNumber);
		
		this.foeBotsMutable = foeBots;
		this.foeBots = Collections.unmodifiableMap(foeBotsMutable);
		
		this.tigerBotsMutable = tigerBots;
		this.tigerBots = Collections.unmodifiableMap(tigerBotsMutable);
	}
	

	/**
	 * Providing a <strong>shallow</strong> copy of original (Thus collections are created, but filled with the same
	 * values
	 * @param original
	 */
	public WorldFrame(WorldFrame original)
	{
		// Fields
		this.ball = original.ball;
		this.time = original.time;
		this.id = original.id; // new FrameID(original.id);
		

		// Collections
		foeBotsMutable = new HashMap<Integer, TrackedBot>(7, 1);
		for (Entry<Integer, TrackedBot> foe : original.foeBots.entrySet())
		{
			foeBotsMutable.put(foe.getKey(), foe.getValue()); // new TrackedBot(foe.getValue()));
		}
		this.foeBots = Collections.unmodifiableMap(foeBotsMutable);
		

		tigerBotsMutable = new HashMap<Integer, TrackedTigerBot>(7, 1);
		for (Entry<Integer, TrackedTigerBot> tiger : original.tigerBots.entrySet())
		{
			tigerBotsMutable.put(tiger.getKey(), tiger.getValue()); // new TrackedTigerBot(tiger.getValue()));
		}
		this.tigerBots = Collections.unmodifiableMap(tigerBotsMutable);
	}
	

	@Override
	public String toString()
	{
		StringBuilder b = new StringBuilder();
		b.append("[WorldFrame, id = ").append(id).append("|\n");
		b.append("Ball: ").append(ball.pos).append("|\n");
		b.append("Tigers: ");
		for (TrackedBot tiger : tigerBots.values())
		{
			b.append(tiger.pos).append(",");
		}
		b.append("|\n");
		b.append("Enemies: ");
		for (TrackedBot bot : foeBots.values())
		{
			b.append(bot.pos).append(",");
		}
		b.append("]\n");
		return b.toString();
	}
	
	
	// --------------------------------------------------------------------------
	// --- modifier -------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * This class allows modifications on {@link WorldFrame}s, but prevents accidentally use.
	 * 
	 * @author Gero
	 */
	public static class WorldFrameModifier
	{
		
		/**
		 * @param wf
		 * @return An {@link Iterator} on the internal, mutable {@link Map} of {@link TrackedTigerBot}s
		 */
		public Iterator<Entry<Integer, TrackedTigerBot>> getMutableTigersIterator(WorldFrame wf)
		{
			return wf.tigerBotsMutable.entrySet().iterator();
		}
		

		/**
		 * @param wf
		 * @return An {@link Iterator} on the internal, mutable {@link Map} of {@link TrackedBot}s
		 */
		public Iterator<Entry<Integer, TrackedBot>> getMutableFoesIterator(WorldFrame wf)
		{
			return wf.foeBotsMutable.entrySet().iterator();
		}
	}
}
