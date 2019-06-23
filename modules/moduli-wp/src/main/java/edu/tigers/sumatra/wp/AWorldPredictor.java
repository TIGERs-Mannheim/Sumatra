/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.g3force.configurable.IConfigObserver;

import edu.tigers.moduli.AModule;
import edu.tigers.sumatra.cam.ICamFrameObserver;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.wp.util.IRobotInfoProvider;


/**
 * This is the base class for all prediction-implementations, providing basic connections to the predecessor/successor
 * in data-flow and an observable to spread messages.
 * 
 * @author Gero
 */
public abstract class AWorldPredictor extends AModule implements ICamFrameObserver, IConfigObserver
{
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
	@SuppressWarnings("squid:S2250") // Collection methods with O(n) performance
	public final void addObserver(final IWorldFrameObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer that is registered atm.
	 */
	@SuppressWarnings("squid:S2250") // Collection methods with O(n) performance
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
	@SuppressWarnings("squid:S2250") // Collection methods with O(n) performance
	public final void addConsumer(final IWorldFrameObserver consumer)
	{
		consumers.add(consumer);
	}
	
	
	/**
	 * @param consumer that is registered atm.
	 */
	@SuppressWarnings("squid:S2250") // Collection methods with O(n) performance
	public final void removeConsumer(final IWorldFrameObserver consumer)
	{
		consumers.remove(consumer);
	}
	
	
	/**
	 * Notify observers about a new shape map
	 *
	 * @param timestamp
	 * @param shapeMap the shape map
	 * @param source an identifier for the source
	 */
	public final void notifyNewShapeMap(final long timestamp, ShapeMap shapeMap, String source)
	{
		ShapeMap unmodifiableShapeMap = ShapeMap.unmodifiableCopy(shapeMap);
		for (IWorldFrameObserver o : observers)
		{
			o.onNewShapeMap(timestamp, unmodifiableShapeMap, source);
		}
	}
	
	
	public final void notifyClearShapeMap(String source)
	{
		for (IWorldFrameObserver o : observers)
		{
			o.onClearShapeMap(source);
		}
	}
	
	
	public abstract void setRobotInfoProvider(IRobotInfoProvider robotInfoProvider);
}
