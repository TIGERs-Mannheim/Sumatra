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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.SumatraCam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ACam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AReferee;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IRefereeMsgConsumer;
import edu.dhbw.mannheim.tigers.sumatra.util.GlobalShortcuts;
import edu.dhbw.mannheim.tigers.sumatra.util.GlobalShortcuts.EShortcut;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;


/**
 * Implementation of {@link AReferee} which holds an instance of the {@link RefereeReceiver} (for receiving referee
 * messages on the network) and one of the {@link RefereeMsgTransmitter} (for sending own messages for testing
 * purposes).
 * 
 * @author Gero
 */
public class RefereeHandler extends AReferee
{
	private static final Logger	log		= Logger.getLogger(RefereeHandler.class.getName());
	private final RefereeReceiver	receiver;
	private final IBallReplacer	ballReplacer;
	private int							refMsgId	= 0;
	
	
	/**
	 * @param subconfig
	 */
	public RefereeHandler(final SubnodeConfiguration subconfig)
	{
		receiver = new RefereeReceiver(subconfig, this);
		if ("sumatra".equals(subconfig.getString("ballReplacer", "")))
		{
			SumatraCam cam = null;
			try
			{
				cam = (SumatraCam) SumatraModel.getInstance().getModule(ACam.MODULE_ID);
			} catch (ModuleNotFoundException err)
			{
				log.error("Could not find cam module", err);
			}
			ballReplacer = cam;
		} else
		{
			ballReplacer = new GrSimBallReplacer(subconfig);
		}
		
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
	
	
	@Override
	public void initModule() throws InitModuleException
	{
		resetCountDownLatch();
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		receiver.start();
	}
	
	
	@Override
	public void stopModule()
	{
		receiver.cleanup();
	}
	
	
	@Override
	public void deinitModule()
	{
		receiver.cleanup();
	}
	
	
	// --------------------------------------------------------------------------
	// --- on incoming msg ------------------------------------------------------
	// --------------------------------------------------------------------------
	protected void notifyConsumer(final RefereeMsg msg)
	{
		for (IRefereeMsgConsumer consumer : getConsumers())
		{
			consumer.onNewRefereeMsg(msg);
		}
	}
	
	
	protected void onNewExternalRefereeMsg(final SSL_Referee msg)
	{
		if (isReceiveExternalMsg())
		{
			if (isNewMessage(msg))
			{
				log.trace("Referee msg: " + msg.getCommand());
			}
			onNewRefereeMsg(msg);
		}
	}
	
	
	private void onNewRefereeMsg(final SSL_Referee msg)
	{
		final RefereeMsg msgYellow = new RefereeMsg(msg, ETeamColor.YELLOW);
		final RefereeMsg msgBlue = new RefereeMsg(msg, ETeamColor.BLUE);
		TeamConfig.setKeeperIdYellow(msgYellow.getTeamInfoYellow().getGoalie());
		TeamConfig.setKeeperIdBlue(msgBlue.getTeamInfoBlue().getGoalie());
		
		notifyConsumer(msgYellow);
		notifyConsumer(msgBlue);
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
	public void sendOwnRefereeMsg(final Command cmd, final int goalsBlue, final int goalsYellow, final int timeLeft)
	{
		SSL_Referee refMsg = createRefereeMsg(cmd, goalsBlue, goalsYellow, timeLeft, refMsgId);
		refMsgId++;
		onNewRefereeMsg(refMsg);
	}
	
	
	/**
	 * @param cmd
	 * @param goalsBlue
	 * @param goalsYellow
	 * @param timeLeft
	 * @param refId
	 * @return
	 */
	private SSL_Referee createRefereeMsg(final Command cmd, final int goalsBlue, final int goalsYellow,
			final int timeLeft, final int refId)
	{
		TeamInfo.Builder teamBlueBuilder = TeamInfo.newBuilder();
		teamBlueBuilder.setGoalie(TeamConfig.getKeeperIdBlue());
		teamBlueBuilder.setName("Blue");
		teamBlueBuilder.setRedCards(1);
		teamBlueBuilder.setScore(goalsBlue);
		teamBlueBuilder.setTimeouts(4);
		teamBlueBuilder.setTimeoutTime(360);
		teamBlueBuilder.setYellowCards(3);
		
		TeamInfo.Builder teamYellowBuilder = TeamInfo.newBuilder();
		teamYellowBuilder.setGoalie(TeamConfig.getKeeperIdYellow());
		teamYellowBuilder.setName("Yellow");
		teamYellowBuilder.setRedCards(0);
		teamYellowBuilder.setScore(goalsYellow);
		teamYellowBuilder.setTimeouts(2);
		teamYellowBuilder.setTimeoutTime(65);
		teamYellowBuilder.setYellowCards(1);
		
		SSL_Referee.Builder builder = SSL_Referee.newBuilder();
		builder.setPacketTimestamp(SumatraClock.currentTimeMillis());
		builder.setBlue(teamBlueBuilder.build());
		builder.setYellow(teamYellowBuilder.build());
		builder.setCommand(cmd);
		builder.setCommandCounter(refId);
		builder.setCommandTimestamp(SumatraClock.currentTimeMillis());
		builder.setStageTimeLeft(timeLeft);
		builder.setStage(Stage.NORMAL_FIRST_HALF);
		
		return builder.build();
	}
	
	
	@Override
	public void replaceBall(final IVector2 pos)
	{
		ballReplacer.replaceBall(pos);
	}
}
