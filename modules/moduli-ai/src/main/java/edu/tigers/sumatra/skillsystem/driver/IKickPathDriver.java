/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 3, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.driver;

import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.skillsystem.skills.KickSkill.EMoveMode;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IKickPathDriver extends IPathDriver
{
	/**
	 * @return
	 */
	boolean isEnableDribbler();
	
	
	/**
	 * @return
	 */
	boolean armKicker();
	
	
	/**
	 * @param shootSpeed
	 */
	default void setShootSpeed(final double shootSpeed)
	{
	}
	
	
	/**
	 * @param moveMode
	 */
	default void setMoveMode(final EMoveMode moveMode)
	{
	}
	
	
	/**
	 * @param ready4Kick
	 */
	default void setRoleReady4Kick(final boolean ready4Kick)
	{
	}
	
	
	/**
	 * @return
	 */
	default boolean isSkillReady4Kick()
	{
		return true;
	}
	
	
	/**
	 * @param dest
	 */
	default void setDestForAvoidingOpponent(final IVector2 dest)
	{
	}
	
	
	/**
	 * 
	 */
	default void unsetDestForAvoidingOpponent()
	{
	}
	
	
	/**
	 * @param pos
	 */
	default void setProtectPos(final IVector2 pos)
	{
	}
}
