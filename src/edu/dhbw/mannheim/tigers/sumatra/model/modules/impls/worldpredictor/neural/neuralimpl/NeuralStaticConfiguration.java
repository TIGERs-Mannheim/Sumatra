/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 31.12.2014
 * Author(s): KaiE
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.neural.neuralimpl;


/**
 * This Class is to generate some static configurations that should
 * not be changed but would be 'magic numbers' in code.
 * 
 * @author KaiE
 */
public class NeuralStaticConfiguration
{
	
	/**
	 * This class contains all static configuration that are used for the Neural WP
	 * 
	 * @author KaiE
	 */
	public static class NeuralWPConfigs
	{
		/** amount of frames that should be combined in the Network */
		public static final int	LastNFrames	= 6;
	}
	
	/**
	 * class to store the configuration for the ball
	 * static inline class of {@link NeuralStaticConfiguration}
	 * 
	 * @author KaiE
	 */
	public static class BallConfigs
	{
		/** time pos.x pos.y vel.x vel.y */
		public static final int	OutputLayer				= 5;
		/** lastNFrames and the amount of data per frame.time, pos.x, pos.y, + look-ahead time */
		public static final int	InputLayer				= ((NeuralWPConfigs.LastNFrames - 1) * OutputLayer) + 1;
		/**  */
		public static final int	HiddenLayer				= 1;
		/**  */
		public static final int	NeuronsPerHidden		= 10;
		/** The ball requires an id */
		public static final int	ID							= -1;
		/** pos_x,pos_y,vel_x,vel_y,acc_x,acc_y */
		public static final int	ConvertedDataArray	= 6;
	}
	
	
	/**
	 * class to store the configuration for the bots.
	 * static inline class of {@link NeuralStaticConfiguration}
	 * 
	 * @author KaiE
	 */
	public static class BotConfigs
	{
		
		
		/** pos.x pos.y orientation time */
		public static final int	OutputLayer				= 7;
		/**
		 * calculated via lastNFrames and the amount of data per frame. time, pos.x, pos.y,vel.x,vel.y, orientation, +
		 * look-ahead time
		 */
		public static final int	InputLayer				= ((NeuralWPConfigs.LastNFrames - 1) * OutputLayer) + 1;
		/**  */
		public static final int	HiddenLayer				= 1;
		/**  */
		public static final int	NeuronsPerHidden		= 10;
		/** posx,posy,velx,vely,ax,ay,orinetdeg,orientvel,orientea */
		public static final int	ConvertedDataArray	= 9;
	}
	
	
}
