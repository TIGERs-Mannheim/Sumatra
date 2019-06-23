/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 29, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.log;

import edu.tigers.sumatra.Referee.SSL_Referee.Stage;
import edu.tigers.sumatra.referee.RefereeMsg;


/**
 * @author "Lukas Magel"
 */
public class GameTime
{
	private final static GameTime	empty	= new GameTime(Stage.NORMAL_FIRST_HALF_PRE, 0);
	
	private final Stage				stage;
	private final long				micros;
	
	
	private GameTime(final Stage stage, final long micros)
	{
		this.stage = stage;
		this.micros = micros;
	}
	
	
	/**
	 * @return
	 */
	public static GameTime empty()
	{
		return empty;
	}
	
	
	/**
	 * @param stage
	 * @param micros
	 * @return
	 */
	public static GameTime of(final Stage stage, final long micros)
	{
		return new GameTime(stage, micros);
	}
	
	
	/**
	 * @param refMsg
	 * @return
	 */
	public static GameTime of(final RefereeMsg refMsg)
	{
		return new GameTime(refMsg.getStage(), refMsg.getStageTimeLeft());
	}
	
	
	/**
	 * @param micros
	 * @return
	 */
	public GameTime subtract(final long micros)
	{
		return new GameTime(stage, this.micros - micros);
	}
	
	
	/**
	 * @return
	 */
	public Stage getStage()
	{
		return stage;
	}
	
	
	/**
	 * @return ms left in the stage
	 */
	public long getMicrosLeft()
	{
		return micros;
	}
}
