/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.passtarget;

import com.sleepycat.persist.model.Persistent;


/**
 * An implementation of a PassTargetRating
 */
@Persistent
public class PassTargetRating implements IPassTargetRating
{
	private double passScore;
	private double goalKickScore;
	private double pressureScore;
	
	private double passScoreStraight;
	private double passScoreChip;
	
	private double passDurationStraight;
	private double passDurationChip;
	
	private double durationScoreStraight;
	private double durationScoreChip;
	
	
	public PassTargetRating()
	{
		passScore = 0;
		goalKickScore = 0;
		passScoreStraight = 0;
		passScoreChip = 0;
		passDurationStraight = Double.MAX_VALUE;
		passDurationChip = Double.MAX_VALUE;
		durationScoreStraight = 0;
		durationScoreChip = 0;
	}
	
	
	@Override
	public double getPassScore()
	{
		return passScore;
	}
	
	
	public void setPassScore(final double passScore)
	{
		this.passScore = passScore;
	}
	
	
	@Override
	public double getGoalKickScore()
	{
		return goalKickScore;
	}
	
	
	public void setGoalKickScore(final double goalKickScore)
	{
		this.goalKickScore = goalKickScore;
	}
	
	
	@Override
	public double getPassScoreStraight()
	{
		return passScoreStraight;
	}
	
	
	public void setPassScoreStraight(final double passScoreStraight)
	{
		this.passScoreStraight = passScoreStraight;
	}
	
	
	@Override
	public double getPassScoreChip()
	{
		return passScoreChip;
	}
	
	
	public void setPassScoreChip(final double passScoreChip)
	{
		this.passScoreChip = passScoreChip;
	}
	
	
	@Override
	public double getPassDurationStraight()
	{
		return passDurationStraight;
	}
	
	
	public void setPassDurationStraight(final double passDurationStraight)
	{
		this.passDurationStraight = passDurationStraight;
	}
	
	
	@Override
	public double getPassDurationChip()
	{
		return passDurationChip;
	}
	
	
	public void setPassDurationChip(final double passDurationChip)
	{
		this.passDurationChip = passDurationChip;
	}
	
	
	@Override
	public double getDurationScoreStraight()
	{
		return durationScoreStraight;
	}
	
	
	public void setDurationScoreStraight(final double durationScoreStraight)
	{
		this.durationScoreStraight = durationScoreStraight;
	}
	
	
	@Override
	public double getDurationScoreChip()
	{
		return durationScoreChip;
	}
	
	
	public void setDurationScoreChip(final double durationScoreChip)
	{
		this.durationScoreChip = durationScoreChip;
	}
	
	
	@Override
	public double getPressureScore()
	{
		return pressureScore;
	}
	
	
	public void setPressureScore(final double pressureScore)
	{
		this.pressureScore = pressureScore;
	}
}
