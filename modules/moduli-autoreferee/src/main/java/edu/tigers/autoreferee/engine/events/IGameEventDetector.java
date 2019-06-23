/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 7, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.events;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;


/**
 * @author "Lukas Magel"
 */
public interface IGameEventDetector
{
	/**
	 * @author "Lukas Magel"
	 */
	public enum EGameEventDetectorType
	{
		/**  */
		ATTACKER_TO_DEFENSE_DISTANCE,
		/**  */
		ATTACKER_TOUCHED_KEEPER,
		/**  */
		BALL_LEFT_FIELD_ICING,
		/**  */
		BALL_SPEEDING,
		/**  */
		BOT_COLLISION,
		/**  */
		BOT_IN_DEFENSE_AREA,
		/**  */
		BOT_NUMBER,
		/**  */
		BOT_STOP_SPEED,
		/**  */
		DOUBLE_TOUCH,
		/**  */
		DRIBBLING,
		/**  */
		DEFENDER_TO_KICK_POINT_DISTANCE,
		/**  */
		GOAL,
		/**  */
		KICK_TIMEOUT
	}
	
	/**
	 * @author "Lukas Magel"
	 */
	public static class GameEventDetectorComparator implements
			Comparator<IGameEventDetector>
	{
		
		/**  */
		public static GameEventDetectorComparator	INSTANCE	= new GameEventDetectorComparator();
		
		
		@Override
		public int compare(final IGameEventDetector o1, final IGameEventDetector o2)
		{
			int prio1 = o1.getPriority();
			int prio2 = o2.getPriority();
			if (prio1 > prio2)
			{
				return -1;
			} else if (prio1 < prio2)
			{
				return 1;
			} else
			{
				return 0;
			}
		}
	}
	
	
	/**
	 * @param state
	 * @return
	 */
	public boolean isActiveIn(EGameStateNeutral state);
	
	
	/**
	 * @return
	 */
	public int getPriority();
	
	
	/**
	 * @param frame
	 * @param events
	 * @return
	 */
	Optional<IGameEvent> update(IAutoRefFrame frame, List<IGameEvent> events);
	
	
	/**
	 *
	 */
	public void reset();
	
	
}
