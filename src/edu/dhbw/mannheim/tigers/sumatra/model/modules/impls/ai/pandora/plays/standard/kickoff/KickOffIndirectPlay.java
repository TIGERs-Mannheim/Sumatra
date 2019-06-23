/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.04.2012
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.kickoff;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.AStandardPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassSenderRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.RedirectRole;


/**
 * This play starts an indirect shot on the opponent goal. Play waits until
 * a referee 'READY' cmd is recieved.
 * 
 * (see also {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.IndirectShotV2Play})
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class KickOffIndirectPlay extends AStandardPlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** ball velocity to estimate a shooten ball */
	private static final double	MIN_BALL_VELOCITY		= 1.0;
	private static final float		POS_OFFSET				= 100;
	
	private final PassSenderRole	passer;
	private final RedirectRole		receiver;
	
	
	/** ready flag used for triggering referee 'READY' CMD */
	private boolean					refereeReady;
	
	private final IVector2			initPositionReceiver	= new Vector2(
																				-100,
																				-((AIConfig.getGeometry().getFieldWidth() / 2) - POS_OFFSET));
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public KickOffIndirectPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		passer = new PassSenderRole(initPositionReceiver);
		receiver = new RedirectRole(initPositionReceiver, true);
		
		addAggressiveRole(passer, aiFrame.worldFrame.ball.getPos().subtractNew(new Vector2(-POS_OFFSET, 0)));
		addAggressiveRole(receiver, initPositionReceiver);
		
		refereeReady = false;
		
		// wait for ever, until ready signal will reset timer
		setTimeout(Long.MAX_VALUE);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected void beforeFirstUpdate(AIInfoFrame frame)
	{
		
	}
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		if (refereeReady)
		{
			if (passer.isCompleted())
			{
				boolean ballWasNearReceiver = false;
				
				receiver.setReady();
				
				if (receiver.getPos().subtractNew(frame.worldFrame.ball.getPos()).getLength2() < POS_OFFSET)
				{
					ballWasNearReceiver = true;
				}
				
				if (ballWasNearReceiver && (frame.worldFrame.ball.getVel().getLength2() > MIN_BALL_VELOCITY))
				{
					changeToFinished();
				}
				
				if (receiver.isCompleted())
				{
					changeToFinished();
				}
			} else
			{
				passer.updateReceiverPos(GeoMath.stepAlongLine(calcReceiverPosition(frame), AIConfig.getGeometry()
						.getGoalTheir().getGoalCenter(), AIConfig.getGeometry().getBotRadius()));
				passer.setReceiverReady();
			}
		} else if ((frame.refereeMsgCached != null) && (frame.refereeMsgCached.getCommand() == Command.NORMAL_START))
		{
			refereeReady = true;
			resetTimer();
			setTimeout(5);
			
			passer.updateReceiverPos(calcReceiverPosition(frame));
		} else
		{
			passer.updateDestination(frame.worldFrame.ball.getPos().subtractNew(new Vector2(0, -POS_OFFSET * 2)));
			receiver.updateDestination(calcReceiverPosition(frame));
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private IVector2 calcReceiverPosition(AIInfoFrame frame)
	{
		if (refereeReady)
		{
			return initPositionReceiver.subtractNew(new Vector2(-POS_OFFSET / 2, 0));
		}
		return initPositionReceiver;
	}
	
	
	@Override
	protected void timedOut(AIInfoFrame currentFrame)
	{
		changeToFinished();
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		// nothing todo
	}
}
