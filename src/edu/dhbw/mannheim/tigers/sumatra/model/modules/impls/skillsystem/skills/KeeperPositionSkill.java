/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 8, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * Simple test keeper for comparision with
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KeeperPositionSkill extends PositionSkill
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public KeeperPositionSkill()
	{
		super(ESkillName.POSITION_KEEPER);
	}
	
	
	@Override
	protected void doCalcActions(final List<ACommand> cmds)
	{
		IVector2 goalCenter = AIConfig.getGeometry().getGoalOur().getGoalCenter();
		Circle circle = new Circle(goalCenter, 400);
		List<IVector2> points = circle.lineIntersections(Line.newLine(getWorldFrame().getBall().getPos(), goalCenter));
		for (IVector2 p : points)
		{
			if (p.x() > goalCenter.x())
			{
				setDestination(p);
				setOrientation(getWorldFrame().getBall().getPos().subtractNew(p).getAngle());
			}
		}
	}
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
