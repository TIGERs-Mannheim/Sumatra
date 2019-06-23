/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.data;

import java.util.Optional;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * A robot plan for multi team communication (mixed team)
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MultiTeamRobotPlan
{
	private final BotID botID;
	private EMultiTeamRobotRole role = EMultiTeamRobotRole.DEFAULT;
	private Pose targetPose = null;
	private IVector2 shootTarget = null;
	
	
	/**
	 * @param botID for this bot
	 */
	public MultiTeamRobotPlan(final BotID botID)
	{
		this.botID = botID;
	}
	
	
	public BotID getBotID()
	{
		return botID;
	}
	
	
	public EMultiTeamRobotRole getRole()
	{
		return role;
	}
	
	
	public void setRole(final EMultiTeamRobotRole role)
	{
		this.role = role;
	}
	
	
	public Optional<Pose> getTargetPose()
	{
		return Optional.ofNullable(targetPose);
	}
	
	
	public void setTargetPose(final Pose targetPose)
	{
		this.targetPose = targetPose;
	}
	
	
	public Optional<IVector2> getShootTarget()
	{
		return Optional.ofNullable(shootTarget);
	}
	
	
	public void setShootTarget(final IVector2 shootTarget)
	{
		this.shootTarget = shootTarget;
	}
	
	/**
	 * Possible robot roles
	 */
	public enum EMultiTeamRobotRole
	{
		/** */
		DEFAULT,
		/** */
		GOALIE,
		/** */
		DEFENSE,
		/** */
		OFFENSE
	}
}
