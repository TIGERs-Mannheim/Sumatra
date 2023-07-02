/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes.shot;

import edu.tigers.sumatra.geometry.NGeometry;
import edu.tigers.sumatra.math.line.IHalfLine;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

import static edu.tigers.sumatra.loganalysis.eventtypes.shot.ShotBuilder.EndOfPassCause.INTERCEPT_BY_BOT;


public class ShotBuilder
{
	private static final Logger log = LogManager.getLogger(ShotBuilder.class.getName());

	/**
	 * When did the pass start
	 */
	private long startFrame;

	/**
	 * When did the pass end
	 */
	private long endFrame;

	/**
	 * Did the pass reach a receiver of the same team
	 */
	private boolean successful;

	/**
	 * bot that kicks the ball in this pass
	 */
	private ITrackedBot passerBot;

	/**
	 * real receiver of the pass
	 */
	private ITrackedBot receiverBot;
	private EndOfPassCause endOfPassCause;
	private boolean isChipKick; // chip kick
	private ITrackedBot receiverBotAtKick; // fake receiver (receiver with is in pass line during the kick)
	private IVector2 endOfPass; // the end of the pass line (is not always the receiverBot kicker pos)
	private IVector2 ballVelBefore;
	private IVector2 ballAcc;
	/**
	 * State of Builder: makes sure the updates, create methods are invoked in the right order
	 * 0 builder cleared - no data
	 * 1 init updated
	 * 2 endOfPass updated
	 * 3 created Pass
	 */
	private int builderState = 0;


	public void clear()
	{
		this.passerBot = null;
		this.startFrame = 0;
		this.ballVelBefore = null;
		this.ballAcc = null;
		isChipKick = false;
		endOfPassCause = EndOfPassCause.UNKNOWN;
		builderState = 0;
	}


	public void updateInit(ITrackedBot shooter, long startFrame,
			IVector2 ballVelBefore, IVector2 ballAcc, ITrackedBot receiverBotAtKick)
	{
		if (builderState < 0)
		{
			log.error("wrong shot builder state {} (0 expected)\n methods of shot builder are not called in right order",
					builderState);
			return;
		}

		this.passerBot = shooter;
		this.startFrame = startFrame;
		this.ballVelBefore = ballVelBefore;
		this.ballAcc = ballAcc;
		this.receiverBotAtKick = receiverBotAtKick;
		builderState = 1;
	}


	public void updateEndOfPassCause(EndOfPassCause endOfPassCause)
	{

		this.endOfPassCause = endOfPassCause;

		builderState = 2;
	}


	public void updateChipFlag(boolean isChipKick)
	{
		this.isChipKick = isChipKick;
	}


	public boolean isChipKick()
	{
		return this.isChipKick;
	}


	private ShotType getShotType()
	{
		if (getEndOfPassCause() == INTERCEPT_BY_BOT &&
				getReceiverBot() != null && getPasserBot().getTeamColor() == getReceiverBot().getTeamColor())
		{
			// passer bot has same color like receiver bot
			return ShotType.PASS;
		}
		if (getPasserBot() == null)
		{
			return ShotType.PASS;
		}

		// passer bot is of different than receiver bot
		IHalfLine passLine = Lines.halfLineFromPoints(getStartPassPos(), getEndOfPass());
		double marginGoalForGoalShot = 120d;
		ILineSegment goalLineMargin = NGeometry.getGoal(getPasserBot().getTeamColor().opposite())
				.getLineSegment().withMargin(marginGoalForGoalShot);

		var intersectionPassGoalLine = goalLineMargin.intersect(passLine);

		if (intersectionPassGoalLine.isEmpty())
		{
			return ShotType.PASS;
		} else
		{
			// shot in direction of the opponent goal
			return ShotType.GOAL_SHOT;
		}
	}


	public IShotEventType createShotEventType(long endFrame, ITrackedBot receiverBot, IVector2 endOfPass)
			throws WrongBuilderStateException
	{
		if (builderState < 2)
		{
			throw new WrongBuilderStateException("wrong shot builder state " + builderState
					+ " (2 expected)\n methods of shot builder are not called in right order");
		}

		this.endFrame = endFrame;
		this.receiverBot = receiverBot;
		this.endOfPass = endOfPass;

		ShotType shotType = getShotType();

		if (Objects.requireNonNull(shotType) == ShotType.GOAL_SHOT)
		{
			ILineSegment passLine = Lines.segmentFromPoints(getStartPassPos(), getEndOfPass());
			ILineSegment goalLine = NGeometry.getGoal(getPasserBot().getTeamColor().opposite())
					.getLineSegment();
			var intersectionPassGoalLine = goalLine.intersect(passLine);
			successful = !intersectionPassGoalLine.isEmpty();
			return new GoalShot(this);
		}
		successful = receiverBot != null;
		return new Passing(this);
	}


	public IVector2 getStartPassPos()
	{
		if (passerBot == null)
		{
			return Vector2.zero();
		}
		return passerBot.getBotKickerPos();
	}


	public IVector2 getBallVelBefore()
	{
		return ballVelBefore;
	}


	public IVector2 getBallAcc()
	{
		return ballAcc;
	}


	public long getStartFrame()
	{
		return startFrame;
	}


	public long getEndFrame()
	{
		return endFrame;
	}


	public boolean isSuccessful()
	{
		return successful;
	}


	public ITrackedBot getPasserBot()
	{
		return passerBot;
	}


	public ITrackedBot getReceiverBot()
	{
		return receiverBot;
	}


	public ITrackedBot getReceiverBotAtKick()
	{
		return receiverBotAtKick;
	}


	public IVector2 getEndOfPass()
	{
		return endOfPass;
	}


	public EndOfPassCause getEndOfPassCause()
	{
		return endOfPassCause;
	}


	/**
	 * stores why the pass ended
	 */
	public enum EndOfPassCause
	{
		INTERCEPT_BY_BOT,
		BALL_OUT_OF_FIELD,
		BALL_TOO_SLOW,
		GAME_STATE_STOP,
		UNKNOWN
	}


	private enum ShotType
	{
		PASS,
		GOAL_SHOT
	}

	public static class WrongBuilderStateException extends Exception
	{
		public WrongBuilderStateException(String str)
		{
			super(str);
		}
	}
}
