/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.visualizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.bot.ERobotMode;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotStatus
{
	private boolean visible = false;
	private boolean connected = false;
	private double batRel = 0;
	private double kickerRel = 0;
	
	private boolean hideRcm = false;
	private boolean hideAi = false;
	private Map<EFeature, EFeatureState> botFeatures = Collections.emptyMap();
	private List<String> brokenFeatures = new ArrayList<>();
	private ERobotMode robotMode;
	
	
	@Override
	public boolean equals(final Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (obj.getClass() != this.getClass())
		{
			return false;
		}
		BotStatus bs = (BotStatus) obj;
		boolean equalBools = bs.isVisible() == visible && bs.isConnected() == connected &&
				bs.isHideRcm() == hideRcm && bs.isHideAi() == hideAi;
		boolean equalDoubles = Math.abs(bs.getBatRel() - batRel) < 0.001 &&
				Math.abs(bs.getKickerRel() - kickerRel) < 0.001;
		
		return equalBools && equalDoubles && bs.getBotFeatures().equals(botFeatures);
	}
	
	
	@Override
	public int hashCode()
	{
		return super.hashCode();
	}
	
	
	/**
	 * @return the visible
	 */
	public final boolean isVisible()
	{
		return visible;
	}
	
	
	/**
	 * @param visible the visible to set
	 */
	public final void setVisible(final boolean visible)
	{
		this.visible = visible;
	}
	
	
	/**
	 * @return the connected
	 */
	public final boolean isConnected()
	{
		return connected;
	}
	
	
	/**
	 * @param connected the connected to set
	 */
	public final void setConnected(final boolean connected)
	{
		this.connected = connected;
	}
	
	
	/**
	 * @return the batRel
	 */
	public final double getBatRel()
	{
		return batRel;
	}
	
	
	/**
	 * @param batRel the batRel to set
	 */
	public final void setBatRel(final double batRel)
	{
		this.batRel = batRel;
	}
	
	
	/**
	 * @return the kickerRel
	 */
	public final double getKickerRel()
	{
		return kickerRel;
	}
	
	
	/**
	 * @param kickerRel the kickerRel to set
	 */
	public final void setKickerRel(final double kickerRel)
	{
		this.kickerRel = kickerRel;
	}
	
	
	/**
	 * @return the hideRcm
	 */
	public final boolean isHideRcm()
	{
		return hideRcm;
	}
	
	
	/**
	 * @param hideRcm the hideRcm to set
	 */
	public final void setHideRcm(final boolean hideRcm)
	{
		this.hideRcm = hideRcm;
	}
	
	
	/**
	 * @return the hideAi
	 */
	public final boolean isHideAi()
	{
		return hideAi;
	}
	
	
	/**
	 * @param hideAi the hideAi to set
	 */
	public final void setHideAi(final boolean hideAi)
	{
		this.hideAi = hideAi;
	}
	
	
	/**
	 * @return the botFeatures
	 */
	public final Map<EFeature, EFeatureState> getBotFeatures()
	{
		return botFeatures;
	}
	
	
	/**
	 * @param features the bot features to set
	 */
	public final void setBotFeatures(Map<EFeature, EFeatureState> features)
	{
		this.botFeatures = features;
	}
	
	
	/**
	 * @return the broken features
	 */
	public final List<String> getBrokenFeatures()
	{
		return brokenFeatures;
	}
	
	
	/**
	 * @param brokenFeatures List of not working botFeatures as Strings
	 */
	public final void setBrokenFeatures(List<String> brokenFeatures)
	{
		this.brokenFeatures = brokenFeatures;
	}
	
	
	/**
	 * @return the robotMode
	 */
	public ERobotMode getRobotMode()
	{
		return robotMode;
	}
	
	
	/**
	 * @param robotMode
	 */
	public void setRobotMode(final ERobotMode robotMode)
	{
		this.robotMode = robotMode;
	}
}
