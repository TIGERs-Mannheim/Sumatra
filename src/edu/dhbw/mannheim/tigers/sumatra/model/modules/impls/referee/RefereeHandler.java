/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.12.2011
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.referee;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.InitModuleException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.StartModuleException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Stage;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.TeamInfo;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.ITeamConfigObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamProps;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AReferee;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IRefereeMsgConsumer;
import edu.dhbw.mannheim.tigers.sumatra.util.GlobalShortcuts;
import edu.dhbw.mannheim.tigers.sumatra.util.GlobalShortcuts.EShortcut;


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
	
	private static final Logger		log				= Logger.getLogger(RefereeHandler.class.getName());
	private static final int			TIMEOUT			= 10000;
	
	private final RefereeReceiver		receiver;
	private final GrSimBallReplacer	ballReplacer;
	private final AutoReferee			autoReferee;
	
	private TeamProps						teamProps		= null;
	private final Object					teamPropsSync	= new Object();
	
	private int								refMsgId			= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param subconfig
	 */
	public RefereeHandler(final SubnodeConfiguration subconfig)
	{
		// Register for team properties
		TeamConfig.getInstance().addObserver(this);
		
		receiver = new RefereeReceiver(subconfig, this);
		ballReplacer = new GrSimBallReplacer(subconfig);
		autoReferee = new AutoReferee(this);
		
		GlobalShortcuts.register(EShortcut.REFEREE_HALT, new Runnable()
		{
			@Override
			public void run()
			{
				sendOwnRefereeMsg(Command.HALT, 0, 0, (short) 0);
			}
		});
		GlobalShortcuts.register(EShortcut.REFEREE_STOP, new Runnable()
		{
			@Override
			public void run()
			{
				sendOwnRefereeMsg(Command.STOP, 0, 0, (short) 0);
			}
		});
		GlobalShortcuts.register(EShortcut.REFEREE_START, new Runnable()
		{
			@Override
			public void run()
			{
				sendOwnRefereeMsg(Command.NORMAL_START, 0, 0, (short) 0);
			}
		});
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void initModule() throws InitModuleException
	{
		resetCountDownLatch();
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		receiver.start();
		
		try
		{
			// register auto referee for new ai frames
			// only use yellow agent, because autoReferee does not use team specific data from ai frames
			AAgent agent = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
			agent.addObserver(autoReferee);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find agent module", err);
		}
	}
	
	
	@Override
	public void stopModule()
	{
		receiver.cleanup();
		
		try
		{
			AAgent agent = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
			agent.removeObserver(autoReferee);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find agent module", err);
		}
	}
	
	
	@Override
	public void deinitModule()
	{
		receiver.cleanup();
	}
	
	
	// --------------------------------------------------------------------------
	// --- on incoming msg ------------------------------------------------------
	// --------------------------------------------------------------------------
	protected void notifyConsumer(final RefereeMsg msg, final ETeamColor color)
	{
		for (IRefereeMsgConsumer consumer : getConsumers())
		{
			if (consumer.getTeamColor().equals(color))
			{
				consumer.onNewRefereeMsg(msg);
			}
		}
	}
	
	
	protected void onNewExternalRefereeMsg(final SSL_Referee msg)
	{
		if (isReceiveExternalMsg())
		{
			onNewRefereeMsg(msg);
		}
	}
	
	
	private void onNewRefereeMsg(final SSL_Referee msg)
	{
		final RefereeMsg msgYellow = new RefereeMsg(msg, ETeamColor.YELLOW);
		final RefereeMsg msgBlue = new RefereeMsg(msg, ETeamColor.BLUE);
		teamProps.setKeeperIdYellow(msgYellow.getTeamInfoYellow().getGoalie());
		teamProps.setKeeperIdBlue(msgBlue.getTeamInfoBlue().getGoalie());
		
		notifyConsumer(msgYellow, ETeamColor.YELLOW);
		notifyConsumer(msgBlue, ETeamColor.BLUE);
		notifyNewRefereeMsg(msgYellow);
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
	public void sendOwnRefereeMsg(final Command cmd, final int goalsBlue, final int goalsYellow, final short timeLeft)
	{
		final TeamProps newTeamProps = getTeamProperties();
		if (newTeamProps == null)
		{
			return;
		}
		
		TeamInfo.Builder teamBlueBuilder = TeamInfo.newBuilder();
		teamBlueBuilder.setGoalie(newTeamProps.getKeeperIdBlue());
		teamBlueBuilder.setName("Blue");
		teamBlueBuilder.setRedCards(1);
		teamBlueBuilder.setScore(goalsBlue);
		teamBlueBuilder.setTimeouts(4);
		teamBlueBuilder.setTimeoutTime(360);
		teamBlueBuilder.setYellowCards(3);
		
		TeamInfo.Builder teamYellowBuilder = TeamInfo.newBuilder();
		teamYellowBuilder.setGoalie(newTeamProps.getKeeperIdYellow());
		teamYellowBuilder.setName("Yellow");
		teamYellowBuilder.setRedCards(0);
		teamYellowBuilder.setScore(goalsYellow);
		teamYellowBuilder.setTimeouts(2);
		teamYellowBuilder.setTimeoutTime(65);
		teamYellowBuilder.setYellowCards(1);
		
		SSL_Referee.Builder builder = SSL_Referee.newBuilder();
		builder.setPacketTimestamp(System.currentTimeMillis());
		builder.setBlue(teamBlueBuilder.build());
		builder.setYellow(teamYellowBuilder.build());
		builder.setCommand(cmd);
		builder.setCommandCounter(refMsgId);
		builder.setCommandTimestamp(System.currentTimeMillis());
		builder.setStageTimeLeft(timeLeft);
		builder.setStage(Stage.NORMAL_FIRST_HALF);
		
		refMsgId++;
		onNewRefereeMsg(builder.build());
	}
	
	
	@Override
	public void onNewTeamConfig(final TeamProps teamProps)
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
	
	
	@Override
	public void replaceBall(final IVector2 pos)
	{
		ballReplacer.replaceBall(pos);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the autoReferee
	 */
	public final AutoReferee getAutoReferee()
	{
		return autoReferee;
	}
}
