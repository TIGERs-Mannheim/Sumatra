/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.kick.data;

/**
 * Sample for kick model identification.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class KickModelSample
{
	private final String botId;
	// [m/s]
	private final double kickVel;
	// [us]
	private final double kickDuration;
	private final double dribbleSpeed;
	private final boolean chip;
	private boolean sampleUsed = true;
	
	
	/** For jackson binding */
	protected KickModelSample()
	{
		botId = "none";
		kickVel = 0;
		kickDuration = 0;
		dribbleSpeed = 0;
		chip = false;
	}
	
	
	/**
	 * @param botId
	 * @param kickVel
	 * @param kickDuration
	 * @param dribbleSpeed
	 * @param chip
	 */
	public KickModelSample(final String botId, final double kickVel, final double kickDuration,
			final double dribbleSpeed, final boolean chip)
	{
		this.botId = botId;
		this.kickVel = kickVel;
		this.kickDuration = kickDuration;
		this.dribbleSpeed = dribbleSpeed;
		this.chip = chip;
	}
	
	
	/**
	 * @return the botId
	 */
	public String getBotId()
	{
		return botId;
	}
	
	
	/**
	 * @return the kickVel
	 */
	public double getKickVel()
	{
		return kickVel;
	}
	
	
	/**
	 * @return the kickDuration
	 */
	public double getKickDuration()
	{
		return kickDuration;
	}
	
	
	/**
	 * @return the dribbleSpeed
	 */
	public double getDribbleSpeed()
	{
		return dribbleSpeed;
	}
	
	
	/**
	 * @return the isChip
	 */
	public boolean isChip()
	{
		return chip;
	}
	
	
	/**
	 * @return the sampleUsed
	 */
	public boolean isSampleUsed()
	{
		return sampleUsed;
	}
	
	
	/**
	 * @param sampleUsed the sampleUsed to set
	 */
	public void setSampleUsed(final boolean sampleUsed)
	{
		this.sampleUsed = sampleUsed;
	}
}
