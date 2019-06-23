/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 23, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.LinkedList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * Get ball contact with optionally enabling the dribbler.
 * The dribbler won't be turned off.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class GetBallSkill extends AMoveSkill
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final boolean	dribble;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param dribble
	 */
	public GetBallSkill(boolean dribble)
	{
		super(ESkillName.GET_BALL);
		this.dribble = dribble;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private void calcSpline()
	{
		List<IVector2> nodes = new LinkedList<IVector2>();
		IVector2 lookAtTarget = getWorldFrame().getBall().getPos()
				.addNew(getWorldFrame().getBall().getPos().subtractNew(getPos()));
		
		// addNew(new Vector2(bot.getAngle()).multiply(AIConfig.getGeometry().getBotRadius() * 2));
		nodes.add((GeoMath.stepAlongLine(getWorldFrame().getBall().getPos(), lookAtTarget, -AIConfig.getGeometry()
				.getBotRadius() - AIConfig.getGeometry().getBallRadius())));
		// nodes.add((getWorldFrame().ball.getPos()));
		// IVector2 lookAtTarget = getWorldFrame().ball.getPos().addNew(
		// new Vector2(bot.getAngle()).multiply(AIConfig.getGeometry().getBotRadius() * 2));
		//
		// List<IVector2> nodes = new LinkedList<IVector2>();
		// nodes.add((GeoMath.stepAlongLine(getWorldFrame().ball.getPos(), lookAtTarget, -AIConfig.getGeometry()
		// .getBotRadius())));
		// nodes.add((getWorldFrame().ball.getPos()));
		
		createSpline(nodes, lookAtTarget);
	}
	
	
	@Override
	public List<ACommand> doCalcEntryActions(List<ACommand> cmds)
	{
		calcSpline();
		getDevices().disarm(cmds);
		getDevices().dribble(cmds, dribble);
		
		return cmds;
	}
	
	
	@Override
	protected void periodicProcess(List<ACommand> cmds)
	{
	}
	
	
	@Override
	protected boolean isMoveComplete()
	{
		if (hasBallContact())
		{
			return true;
		}
		boolean trajCompleted = super.isMoveComplete();
		return trajCompleted;
	}
	
	
	@Override
	protected List<ACommand> doCalcExitActions(List<ACommand> cmds)
	{
		stopMove(cmds);
		return cmds;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
