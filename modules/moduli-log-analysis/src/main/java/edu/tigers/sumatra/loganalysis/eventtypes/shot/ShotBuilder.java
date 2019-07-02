/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes.shot;

import edu.tigers.sumatra.geometry.NGeometry;
import edu.tigers.sumatra.math.line.v2.IHalfLine;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import org.apache.log4j.Logger;

import java.util.Optional;

import static edu.tigers.sumatra.loganalysis.eventtypes.shot.ShotBuilder.EndOfPassCause.INTERCEPT_BY_BOT;


public class ShotBuilder
{
	private static final Logger log = Logger.getLogger(ShotBuilder.class.getName());

	/** When did the pass start */
	private long startTimestamp;

	/** When did the pass end */
	private long endTimestamp;

	/** Did the pass reach a receiver of the same team */
	private boolean successful;

	/** bot that kicks the ball in this pass */
	private ITrackedBot passerBot;

	/** real receiver of the pass */
	private ITrackedBot receiverBot;

    /** stores why the pass ended */
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

    private EndOfPassCause endOfPassCause;

	private boolean isChipKick; // chip kick
	private ITrackedBot receiverBotAtKick; // fake receiver (receiver with is in pass line during the kick)
	private IVector2 endOfPass; // the end of the pass line (is not always the receiverBot kicker pos)
	
	
	private IVector2 ballVelBefore;
	private IVector2 ballAcc;


	private double marginGoalForGoalShot = 120d;

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
		this.startTimestamp = 0;
		this.ballVelBefore = null;
		this.ballAcc = null;
        isChipKick = false;
        endOfPassCause = EndOfPassCause.UNKNOWN;
        builderState = 0;
	}
	
	
	public void updateInit(ITrackedBot shooter, long startPassTimestamp,
			IVector2 ballVelBefore, IVector2 ballAcc, ITrackedBot receiverBotAtKick)
	{
		if(builderState < 0)
		{
			log.error("wrong shot builder state " + builderState + " (0 expected)\n methods of shot builder are not called in right order");
			return;
		}

		this.passerBot = shooter;
		this.startTimestamp = startPassTimestamp;
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
		if(getEndOfPassCause() == INTERCEPT_BY_BOT &&
				getReceiverBot() != null && getPasserBot().getTeamColor() == getReceiverBot().getTeamColor())
		{
			//passer bot has same color like receiver bot
			return ShotType.PASS;
		}

		//passer bot is of different than receiver bot
		IHalfLine passLine = Lines.halfLineFromPoints(getStartPassPos(), getEndOfPass());
		ILineSegment goalLineMargin = NGeometry.getGoal(getPasserBot().getTeamColor().opposite())
				.getLineSegment().withMargin(marginGoalForGoalShot);

		Optional<IVector2> intersectionPassGoalLine = goalLineMargin.intersectHalfLine(passLine);

		if(intersectionPassGoalLine.isPresent())
		{
			//shot in direction of the foe goal
			return ShotType.GOAL_SHOT;
		}
		else
		{
			return ShotType.PASS;
		}
	}

	public IShotEventType createShotEventType(long endPassTimestamp, ITrackedBot receiverBot, IVector2 endOfPass) throws WrongBuilderStateException
	{
		if(builderState < 2)
		{
			throw new WrongBuilderStateException("wrong shot builder state " + builderState + " (2 expected)\n methods of shot builder are not called in right order");
		}

		this.endTimestamp = endPassTimestamp;
		this.receiverBot = receiverBot;
		this.endOfPass = endOfPass;

		ShotType shotType = getShotType();

		switch (shotType)
		{
			default:
			case PASS:
				successful = receiverBot != null;
				return new Passing(this);
			case GOAL_SHOT:
				ILineSegment passLine = Lines.segmentFromPoints(getStartPassPos(), getEndOfPass());
				ILineSegment goalLine = NGeometry.getGoal(getPasserBot().getTeamColor().opposite())
						.getLineSegment();

				Optional<IVector2> intersectionPassGoalLine = goalLine.intersectSegment(passLine);
				successful = intersectionPassGoalLine.isPresent();
				return new GoalShot(this);
		}
	}
	
	
	public IVector2 getStartPassPos()
	{
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
	
	
	public long getStartTimestamp()
	{
		return startTimestamp;
	}
	
	
	public long getEndTimestamp()
	{
		return endTimestamp;
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

	public EndOfPassCause getEndOfPassCause() {
		return endOfPassCause;
	}

	public class WrongBuilderStateException extends Exception
	{
		public WrongBuilderStateException(String str)
		{
			super(str);
		}
	}
}