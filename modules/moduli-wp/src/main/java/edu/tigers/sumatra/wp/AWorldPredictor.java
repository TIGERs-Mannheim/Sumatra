/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.g3force.configurable.IConfigObserver;

import edu.tigers.moduli.AModule;
import edu.tigers.sumatra.cam.ICamFrameObserver;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiType;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * This is the base class for all prediction-implementations, providing basic connections to the predecessor/successor
 * in data-flow and an observable to spread messages.
 * 
 * @author Gero
 */
public abstract class AWorldPredictor extends AModule implements ICamFrameObserver, IConfigObserver
{
	/** */
	public static final String MODULE_TYPE = "AWorldPredictor";
	/** */
	public static final String MODULE_ID = "worldpredictor";
	
	
	protected final List<IWorldFrameObserver> observers = new CopyOnWriteArrayList<>();
	protected final List<IWorldFrameObserver> consumers = new CopyOnWriteArrayList<>();
	
	
	/**
	 * Add an observer for worldFrames.<br>
	 * Observers are notified after all consumers.
	 * An observer may do <b>some</b> processing in their overridden method,
	 * but please keep it fast or simply take a copy and do processing somewhere else (different Thread!)
	 *
	 * @param observer that wants to listen for new frames
	 */
	public final void addObserver(final IWorldFrameObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer that is registered atm.
	 */
	public final void removeObserver(final IWorldFrameObserver observer)
	{
		observers.remove(observer);
	}
	
	
	/**
	 * Add a consumer for worldFrames.<br>
	 * Consumers are notified <b>before</b> observers.
	 * They should not do <b>any</b> processing in their overridden method to
	 * avoid delays that would be propagated to all other consumers!
	 *
	 * @param consumer that uses the worldframe for fast further processing
	 */
	public final void addConsumer(final IWorldFrameObserver consumer)
	{
		consumers.add(consumer);
	}
	
	
	/**
	 * @param consumer that is registered atm.
	 */
	public final void removeConsumer(final IWorldFrameObserver consumer)
	{
		consumers.remove(consumer);
	}
	
	
	/**
	 * Reset the ball to given position
	 * 
	 * @param pos of the ball
	 */
	public abstract void setLatestBallPosHint(final IVector2 pos);
	
	
	/**
	 * Update the bot to AI assignment
	 *
	 * @param botID
	 * @param aiTeam
	 */
	public abstract void updateBot2AiAssignment(BotID botID, EAiType aiTeam);
	
	
	/**
	 * @return the current assignment
	 */
	public abstract Map<BotID, EAiType> getBotToAiMap();
}
