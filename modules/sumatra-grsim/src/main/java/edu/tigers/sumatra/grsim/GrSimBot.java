/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 23, 2013
 * Author(s): TilmanS
 * *********************************************************
 */
package edu.tigers.sumatra.grsim;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.botmanager.bots.ASimBot;
import edu.tigers.sumatra.ids.BotID;


/**
 * Bot used when playing in grSim simulator
 * Generates grSim protobuf commands
 * 
 * @author TilmanS
 */
@Persistent
public class GrSimBot extends ASimBot
{
	private transient GrSimStatus					status					= new GrSimStatus();
	private transient final GrSimBaseStation	baseStation;
															
	@Configurable
	private static double							center2DribblerDist	= 90;
																						
																						
	static
	{
		ConfigRegistration.registerClass("botmgr", GrSimBot.class);
	}
	
	
	@SuppressWarnings("unused")
	private GrSimBot()
	{
		super();
		baseStation = null;
	}
	
	
	/**
	 * @param id
	 * @param baseStation
	 */
	public GrSimBot(final BotID id, final GrSimBaseStation baseStation)
	{
		super(EBotType.GRSIM, id, baseStation);
		this.baseStation = baseStation;
	}
	
	
	/**
	 * @return the status
	 */
	public final GrSimStatus getStatus()
	{
		return status;
	}
	
	
	@Override
	public void sendMatchCommand()
	{
		super.sendMatchCommand();
		baseStation.sendMatchCommand(this);
	}
	
	
	@Override
	protected double getFeedbackDelay()
	{
		return 0.09;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public double getCenter2DribblerDist()
	{
		return center2DribblerDist;
	}
}
