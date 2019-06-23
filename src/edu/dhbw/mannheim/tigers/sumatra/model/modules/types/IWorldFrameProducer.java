/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 25.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

/**
 * The counterpart to the {@link IWorldFrameConsumer}
 * 
 * @author Gero
 */
public interface IWorldFrameProducer
{
	/**
	 * @param consumer
	 */
	void addWorldFrameConsumer(IWorldFrameConsumer consumer);
	
	
	/**
	 * @param consumer
	 */
	void removeWorldFrameConsumer(IWorldFrameConsumer consumer);
	
	
	/**
	 * @param consumer
	 */
	void addWorldFrameConsumerHungry(IWorldFrameConsumer consumer);
	
	
	/**
	 * @param consumer
	 */
	void removeWorldFrameConsumerHungry(IWorldFrameConsumer consumer);
}
