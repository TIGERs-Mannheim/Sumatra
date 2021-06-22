/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * This data class holds the score and the position of an global supporter position
 *
 * @author chris
 */
public class SupportPosition
{
	private IVector2 pos;
	private long birth;
	private double passScore;
	private double shootScore;

	private boolean isShootPosition = false;

	// The bot that will move to this position
	private BotID assignedBot = null;


	/**
	 * Default constructor
	 *
	 * @param pos
	 * @param birth
	 */
	public SupportPosition(IVector2 pos, long birth)
	{
		this.pos = pos;
		this.birth = birth;
	}


	public IVector2 getPos()
	{
		return pos;
	}


	public double getPassScore()
	{
		return passScore;
	}


	public double getShootScore()
	{
		return shootScore;
	}


	public void setPos(final IVector2 pos)
	{
		this.pos = pos;
	}


	public void setPassScore(final double passScore)
	{
		this.passScore = passScore;
	}


	public void setShootScore(final double shootScore)
	{
		this.shootScore = shootScore;
	}


	public long getBirth()
	{
		return birth;
	}


	public boolean isShootPosition()
	{
		return isShootPosition;
	}


	public void setShootPosition(final boolean shootPosition)
	{
		isShootPosition = shootPosition;
	}


	public BotID getAssignedBot()
	{
		return this.assignedBot;
	}


	public void assignBot(BotID id)
	{
		this.assignedBot = id;
	}


	/**
	 * Compares the pass score. If both are equal the older one is higher
	 *
	 * @param supportPosition
	 * @return
	 */
	public int comparePassScoreWith(final SupportPosition supportPosition)
	{
		int scoreCmp = -Double.compare(this.passScore, supportPosition.getPassScore());
		if (scoreCmp == 0)
		{
			scoreCmp = -Double.compare(this.getShootScore(), supportPosition.getShootScore());
			if (scoreCmp == 0)
			{
				scoreCmp = Long.compare(this.birth, supportPosition.getBirth());
			}
		}
		return scoreCmp;
	}


	/**
	 * Compares the shoot score. If both are equal the oder one is higher
	 *
	 * @param supportPosition
	 * @return
	 */
	public int compareShootScoreWith(final SupportPosition supportPosition)
	{
		int scoreCmp = 0;
		double diff = this.getShootScore() - supportPosition.getShootScore();
		if (Math.abs(diff) > 0.01)
		{
			scoreCmp = -Double.compare(this.getShootScore(), supportPosition.getShootScore());
		}

		if (scoreCmp == 0)
		{
			scoreCmp = -Double.compare(this.passScore, supportPosition.getPassScore());
			if (scoreCmp == 0)
			{
				scoreCmp = Long.compare(this.birth, supportPosition.getBirth());
			}
		}
		return scoreCmp;
	}


	/**
	 * Calculate the distance and compare it to given distance
	 *
	 * @param pos
	 * @param distance
	 * @return
	 */
	public boolean isNearTo(SupportPosition pos, double distance)
	{
		return this.getPos().distanceTo(pos.getPos()) < distance;
	}
}
