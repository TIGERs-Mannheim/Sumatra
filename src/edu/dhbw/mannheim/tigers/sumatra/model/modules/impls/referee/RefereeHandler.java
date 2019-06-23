/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.12.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.referee;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.ITeamConfigObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamProps;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AReferee;
import edu.moduli.exceptions.InitModuleException;
import edu.moduli.exceptions.StartModuleException;


/**
 * Implementation of {@link AReferee} which holds an instance of the {@link RefereeReceiver} (for receiving referee
 * messages on the network) and one of the {@link RefereeMsgTransmitter} (for sending own messages for testing
 * purposes).
 * 
 * @author Gero
 */
public class RefereeHandler extends AReferee implements ITeamConfigObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger			log				= Logger.getLogger(RefereeHandler.class.getName());
	
	private static final int				TIMEOUT			= 10000;
	
	private final RefereeReceiver			receiver;
	private final RefereeMsgTransmitter	transmitter;
	
	private TeamProps							teamProps		= null;
	private final Object						teamPropsSync	= new Object();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param subconfig
	 */
	public RefereeHandler(SubnodeConfiguration subconfig)
	{
		// Register for team properties
		TeamConfig.getInstance().addObserver(this);
		
		receiver = new RefereeReceiver(subconfig, this);
		transmitter = new RefereeMsgTransmitter(subconfig);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void initModule() throws InitModuleException
	{
		resetCountDownLatch();
		
		log.debug("Initialized.");
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		receiver.start();
		
		log.debug("Started.");
	}
	
	
	@Override
	public void stopModule()
	{
		receiver.cleanup();
		
		log.debug("Stopped.");
	}
	
	
	@Override
	public void deinitModule()
	{
		receiver.cleanup();
	}
	
	
	// --------------------------------------------------------------------------
	// --- on incoming msg ------------------------------------------------------
	// --------------------------------------------------------------------------
	protected void notifyConsumer(RefereeMsg msg)
	{
		getConsumer().onNewRefereeMsg(msg);
	}
	
	
	protected void onNewRefereeMsg(RefereeMsg msg)
	{
		notifyNewRefereeMsg(msg);
	}
	
	
	/**
	 * @throws InterruptedException
	 */
	public void waitOnSignal() throws InterruptedException
	{
		getStartSignal().await();
	}
	
	
	// --------------------------------------------------------------------------
	// --- send own msg ---------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void sendOwnRefereeMsg(int id, Command cmd, int goalsBlue, int goalsYellow, short timeLeft)
	{
		final TeamProps newTeamProps = getTeamProperties();
		if (newTeamProps == null)
		{
			return;
		}
		transmitter.sendOwnRefereeMsg(id, cmd, goalsBlue, goalsYellow, timeLeft, newTeamProps);
	}
	
	
	@Override
	public void onNewTeamConfig(TeamProps teamProps)
	{
		synchronized (teamPropsSync)
		{
			final boolean wasNull = (this.teamProps == null);
			
			this.teamProps = teamProps;
			
			if (wasNull)
			{
				teamPropsSync.notifyAll();
			}
		}
	}
	
	
	/**
	 * @return <code>null</code> if interrupted!
	 */
	protected TeamProps getTeamProperties()
	{
		synchronized (teamPropsSync)
		{
			while (teamProps == null)
			{
				try
				{
					teamPropsSync.wait(TIMEOUT);
				} catch (final InterruptedException err)
				{
					return null;
				}
			}
			
			return teamProps;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
