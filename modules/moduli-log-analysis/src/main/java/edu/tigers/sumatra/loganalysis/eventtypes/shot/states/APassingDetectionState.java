/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes.shot.states;

import java.util.Comparator;
import java.util.Optional;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.loganalysis.eventtypes.shot.PassTypeDetectionFrame;
import edu.tigers.sumatra.loganalysis.eventtypes.shot.ShotBuilder;
import edu.tigers.sumatra.loganalysis.eventtypes.shot.ShotDetection;
import edu.tigers.sumatra.loganalysis.microtypes.KickDetection;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.vision.data.IKickEvent;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


public abstract class APassingDetectionState implements IState
{
	private EPassingDetectionState stateId;
	private int countFrameSinceInit = 0;


	public APassingDetectionState(EPassingDetectionState stateId)
	{
		this.stateId = stateId;
	}


	@Override
	public void doEntryActions()
	{
		countFrameSinceInit = 0;
	}


	public void callNextFrameForDetection(PassTypeDetectionFrame frame)
	{
		nextFrameForDetection(frame);
		countFrameSinceInit++;
	}


	protected abstract void nextFrameForDetection(PassTypeDetectionFrame frame);


	protected boolean checkChipKick(PassTypeDetectionFrame frame)
	{
		SimpleWorldFrame wf = frame.getWorldFrameWrapper().getSimpleWorldFrame();
		ShotDetection passingDetection = frame.getPassingDetection();
		ITrackedBall ball = wf.getBall();

		if (ball.isChipped())
		{
			// Chip detected
			passingDetection.setState(EPassingDetectionState.CHIP);
			return true;
		}
		return false;
	}


	protected boolean checkInitKickEvent(PassTypeDetectionFrame frame)
	{
		SimpleWorldFrame wf = frame.getWorldFrameWrapper().getSimpleWorldFrame();
		ShotDetection passingDetection = frame.getPassingDetection();
		ShotBuilder shotBuilder = frame.getShotBuilder();
		Optional<IKickEvent> kickEvent = wf.getKickEvent();
		ITrackedBall ball = wf.getBall();

		if (frame.getKickDetection().getState() == KickDetection.KickDetectionState.KICK && kickEvent.isPresent())
		{
			ITrackedBot kickingBot = wf.getBot(kickEvent.get().getKickingBot());
			IVector2 ballVelBefore = ball.getVel();

			shotBuilder.updateEndOfPassCause(ShotBuilder.EndOfPassCause.INTERCEPT_BY_BOT);
			passingDetection.setState(EPassingDetectionState.NO_PASS);

			ITrackedBot receiverBotAtKick = getMockTargetOfPass(kickingBot, ball.getVel(), wf);
			frame.getShotBuilder().updateInit(kickingBot, frame.getFrameId(), ballVelBefore,
					ball.getAcc(), receiverBotAtKick);

			passingDetection.setState(EPassingDetectionState.PASS_DETECTION);

			return true;
		}
		return false;
	}


	private ITrackedBot getMockTargetOfPass(final ITrackedBot kickerBot, IVector2 kickDirection, SimpleWorldFrame wf)
	{
		ILine shootLine = Line.fromDirection(kickerBot.getBotKickerPos(), kickDirection);
		return wf.getBots().values().stream()
				.filter(bot -> bot.getTeamColor() == kickerBot.getTeamColor() && bot.getBotId() != kickerBot.getBotId())
				.filter(bot -> (shootLine.isPointInFront(bot.getBotKickerPos())))
				.filter(bot -> shootLine.distanceTo(bot.getBotKickerPos()) < Geometry.getBotRadius())
				.min(Comparator.comparingDouble(b -> shootLine.distanceTo(b.getBotKickerPos()))).orElse(null);

	}


	public EPassingDetectionState getId()
	{
		return stateId;
	}


	@Override
	public String getIdentifier()
	{
		return getId().toString();
	}


	public int getCountFrameSinceInit()
	{
		return countFrameSinceInit;
	}
}
