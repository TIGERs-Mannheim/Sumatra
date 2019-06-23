/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.dhbw.mannheim.tigers.moduli.AModule;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.IMergedCamFrameObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.MergedCamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.realworld.RealBotWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;


/**
 * This is the base class for all prediction-implementations, providing basic connections to the predecessor/successor
 * in data-flow and an observable to spread messages.
 * 
 * @author Gero
 */
public abstract class AWorldPredictor extends AModule implements IWorldFrameProducer
{
	/** */
	public static final String								MODULE_TYPE				= "AWorldPredictor";
	/** */
	public static final String								MODULE_ID				= "worldpredictor";
	
	protected List<IWorldFrameConsumer>					consumers				= new CopyOnWriteArrayList<IWorldFrameConsumer>();
	protected List<IWorldFrameConsumer>					consumersHungry		= new CopyOnWriteArrayList<IWorldFrameConsumer>();
	protected final List<IWorldPredictorObserver>	observers				= new CopyOnWriteArrayList<IWorldPredictorObserver>();
	protected final List<IMergedCamFrameObserver>	mergedFrameObservers	= new CopyOnWriteArrayList<>();
	
	/**
	 * 'enum' for the WorldPredictor implementations. When adding a new WP extend this list.
	 * 
	 * @author KaiE
	 */
	public enum PredictorKey
	{
		/** Key for OracleExtKalman.java */
		Kalman,
		/** Key for NeuralWP.java */
		Neural,
		/** Key for {@link RealBotWorldPredictor} */
		BOT
		
	}
	
	
	protected void clearAllObserversAndConsumers()
	{
		consumers.clear();
		consumersHungry.clear();
		observers.clear();
		mergedFrameObservers.clear();
	}
	
	
	@Override
	public void addWorldFrameConsumer(final IWorldFrameConsumer consumer)
	{
		consumers.add(consumer);
	}
	
	
	@Override
	public void removeWorldFrameConsumer(final IWorldFrameConsumer consumer)
	{
		consumers.remove(consumer);
	}
	
	
	@Override
	public void addWorldFrameConsumerHungry(final IWorldFrameConsumer consumer)
	{
		consumersHungry.add(consumer);
	}
	
	
	@Override
	public void removeWorldFrameConsumerHungry(final IWorldFrameConsumer consumer)
	{
		consumersHungry.remove(consumer);
	}
	
	
	/**
	 * WOrldframeObservers
	 * 
	 * @param observer
	 */
	public void addObserver(final IWorldPredictorObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IWorldPredictorObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	/**
	 * WOrldframeObservers
	 * 
	 * @param observer
	 */
	public void addMergedFrameObserver(final IMergedCamFrameObserver observer)
	{
		synchronized (mergedFrameObservers)
		{
			mergedFrameObservers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeMergedFrameObserver(final IMergedCamFrameObserver observer)
	{
		synchronized (mergedFrameObservers)
		{
			mergedFrameObservers.remove(observer);
		}
	}
	
	
	/**
	 * This method is called asynchronously from the WorldPredictorImpl, when the prediction is done
	 * 
	 * @param predictedFrame
	 * @param createdBy
	 */
	public abstract void onPutPredictedWorldFrame(SimpleWorldFrame predictedFrame, PredictorKey createdBy);
	
	
	/**
	 * This Method is responsible to provide access to the latest merged CamFrame
	 * 
	 * @param wpi
	 * @return MergedCamDetectionFrame
	 */
	public abstract MergedCamDetectionFrame pollNewCamFrame(PredictorKey wpi);
	
	
	/**
	 * Set a new last ball pos to force the BallProcessor to use another visible ball
	 * 
	 * @param pos
	 */
	public abstract void setLatestBallPosHint(IVector2 pos);
}
