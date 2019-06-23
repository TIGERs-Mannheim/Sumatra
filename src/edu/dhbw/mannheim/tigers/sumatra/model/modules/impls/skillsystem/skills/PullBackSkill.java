/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 3, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.LinkedList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplineTrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * Pulls the ball backwards
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class PullBackSkill extends AMoveSkill
{
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final IVector2	destination;
	private final IVector2	lookAtTarget;
	private boolean			pull	= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param destination
	 * @param lookAtTarget
	 */
	public PullBackSkill(IVector2 destination, IVector2 lookAtTarget)
	{
		super(ESkillName.PULL_BACK);
		this.destination = destination;
		this.lookAtTarget = lookAtTarget;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void periodicProcess(TrackedTigerBot bot, List<ACommand> cmds)
	{
		if (pull && !bot.hasBallContact())
		{
			getBall(bot);
		} else if (!pull && bot.hasBallContact())
		{
			pull(bot);
		}
	}
	
	
	@Override
	protected List<ACommand> doCalcEntryActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		getDevices().dribble(cmds, true);
		return cmds;
	}
	
	
	@Override
	protected List<ACommand> doCalcExitActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		getDevices().dribble(cmds, false);
		return cmds;
	}
	
	
	private void pull(TrackedTigerBot bot)
	{
		List<IVector2> nodes = new LinkedList<IVector2>();
		nodes.add(destination);
		createSpline(bot, nodes, lookAtTarget, getGen(bot));
		pull = true;
	}
	
	
	private void getBall(TrackedTigerBot bot)
	{
		List<IVector2> nodes = new LinkedList<IVector2>();
		nodes.add(getWorldFrame().ball.getPos());
		createSpline(bot, nodes, lookAtTarget, getGen(bot));
		pull = false;
	}
	
	
	private SplineTrajectoryGenerator getGen(TrackedTigerBot bot)
	{
		SplineTrajectoryGenerator gen = createDefaultGenerator(bot);
		gen.setPositionTrajParams(1, 1);
		return gen;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
