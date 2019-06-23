/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import static java.lang.Math.round;

import org.apache.commons.lang.Validate;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * An implementation of a pass target.
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class PassTarget implements IPassTarget
{
	private final IVector2 kickerPos;
	private final BotID botID;
	private long timeReached = 0;
	
	private double score = 0;
	private double goalKickScore = 0;
	private double passScore = 0;
	
	private long birth = 0;
	private double passRange = 0;
	
	
	@SuppressWarnings("unused")
	private PassTarget()
	{
		kickerPos = null;
		botID = null;
	}
	
	
	/**
	 * New Pass targets with required values
	 * 
	 * @param kickerPos
	 * @param botID
	 */
	public PassTarget(final IVector2 kickerPos, final BotID botID)
	{
		Validate.notNull(kickerPos);
		Validate.notNull(botID);
		this.kickerPos = kickerPos;
		this.botID = botID;
	}
	
	
	/**
	 * Copy relevant values
	 * 
	 * @param passTarget
	 */
	public PassTarget(IPassTarget passTarget)
	{
		this.kickerPos = passTarget.getKickerPos();
		this.botID = passTarget.getBotId();
		birth = passTarget.getBirth();
	}
	
	
	@Override
	public int compareTo(final IPassTarget o)
	{
		int scoreCmp = -Double.compare(round(100 * score), round(100 * o.getScore()));
		if (scoreCmp == 0)
		{
			scoreCmp = Double.compare(kickerPos.distanceToSqr(Geometry.getGoalTheir().getCenter()),
					o.getKickerPos().distanceToSqr(Geometry.getGoalTheir().getCenter()));
			if (scoreCmp == 0)
			{
				scoreCmp = Long.compare(birth, o.getBirth());
			}
		}
		return scoreCmp;
	}
	
	
	@Override
	public DynamicPosition getDynamicTarget()
	{
		return new DynamicPosition(getKickerPos(), getPassRange());
	}
	
	
	@Override
	public IVector2 getKickerPos()
	{
		return kickerPos;
	}
	
	
	@Override
	public IVector2 getBotPos()
	{
		return getKickerPos();
	}
	
	
	@Override
	public BotID getBotId()
	{
		return botID;
	}
	
	
	@Override
	public double getScore()
	{
		return score;
	}
	
	
	@Override
	public long getTimeReached()
	{
		return timeReached;
	}
	
	
	@Override
	public double getGoalKickScore()
	{
		return goalKickScore;
	}
	
	
	@Override
	public double getPassScore()
	{
		return passScore;
	}
	
	
	@Override
	public boolean isSimilarTo(IPassTarget passTarget)
	{
		return passTarget.getBotId().equals(getBotId())
				&& VectorMath.distancePPSqr(passTarget.getKickerPos(), getKickerPos()) < 200 * 200;
	}
	
	
	/**
	 * @param score
	 * @return the pass target for chaining
	 */
	public PassTarget setScore(final double score)
	{
		this.score = score;
		return this;
	}
	
	
	/**
	 * @param timeReached
	 * @return the pass target for chaining
	 */
	public void setTimeReached(final long timeReached)
	{
		this.timeReached = timeReached;
	}
	
	
	public void setGoalKickScore(final double goalKickScore)
	{
		this.goalKickScore = goalKickScore;
	}
	
	
	public void setPassScore(final double passScore)
	{
		this.passScore = passScore;
	}
	
	
	public void setPassRange(final double passRange)
	{
		this.passRange = passRange;
	}
	
	
	@Override
	public long getBirth()
	{
		return birth;
	}
	
	
	/**
	 * @param birth
	 * @return this for chaining
	 */
	public void setBirth(final long birth)
	{
		this.birth = birth;
	}
	
	
	@Override
	public double getTimeUntilReachedInS(long currentTimestamp)
	{
		return (timeReached - currentTimestamp) * 1e-9;
	}
	
	
	@Override
	public double getAge(long currentTimestamp)
	{
		return (currentTimestamp - birth) * 1e-9;
	}
	
	
	@Override
	public double getPassRange()
	{
		return passRange;
	}
	
	
	@Override
	public String toString()
	{
		return "PassTarget{" +
				"kickerPos=" + kickerPos +
				", botID=" + botID +
				'}';
	}
}
