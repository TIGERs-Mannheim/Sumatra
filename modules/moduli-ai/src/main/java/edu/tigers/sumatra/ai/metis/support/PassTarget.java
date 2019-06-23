/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import static edu.tigers.sumatra.skillsystem.skills.AKickSkill.EKickMode;
import static java.lang.Math.round;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;


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
	private double score = 0;
	private long timeReached = 0;
	private double shootScore = 0;
	private double receiveScore = 0;
	private EKickMode kickMode;
	private long birth = 0;
	private transient List<Double> intermediateScores = new ArrayList<>();
	
	
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
		kickMode = botID.isBot() ? EKickMode.PASS : EKickMode.MAX;
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
		kickMode = passTarget.getKickMode();
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
	public double getShootScore()
	{
		return shootScore;
	}
	
	
	@Override
	public double getReceiveScore()
	{
		return receiveScore;
	}
	
	
	@Override
	public EKickMode getKickMode()
	{
		return kickMode;
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
	
	
	public void setShootScore(final double shootScore)
	{
		this.shootScore = shootScore;
	}
	
	
	public void setReceiveScore(final double receiveScore)
	{
		this.receiveScore = receiveScore;
	}
	
	
	/**
	 * @param kickMode how to kick to this pass target
	 * @return the pass target for chaining
	 */
	public void setKickMode(EKickMode kickMode)
	{
		this.kickMode = kickMode;
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
	
	
	public void setIntermediateScores(List<Double> scores)
	{
		this.intermediateScores = scores;
	}
	
	
	@Override
	public List<Double> getIntermediateScores()
	{
		return intermediateScores;
	}
	
	
	@Override
	public double getTimeUntilReachedInS(long currentTimestamp)
	{
		return (timeReached - currentTimestamp) * 1e-9;
	}
	
	
	@Override
	public String toString()
	{
		return "PassTarget{" +
				"kickerPos=" + kickerPos +
				", botID=" + botID +
				", kickMode=" + kickMode +
				'}';
	}
}
