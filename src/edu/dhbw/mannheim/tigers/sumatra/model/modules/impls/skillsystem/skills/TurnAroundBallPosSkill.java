/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 10, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSkillPositioningCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Turn around the ball by given angle in given time (so speed is relative to total time).
 * Bot should be near ball. Starting position is determined by nearestPointOutside(botPos)
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TurnAroundBallPosSkill extends ASkill
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final float	radius;
	private IVector2		staticBallPos;
	private Circle			circle;
	
	@Configurable
	private static float	turnVel	= 0.5f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param radius
	 */
	public TurnAroundBallPosSkill(final float radius)
	{
		super(ESkillName.TURN_AROUND_BALL_POS);
		this.radius = radius;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public List<ACommand> calcActions(final List<ACommand> cmds)
	{
		if (GeoMath.distancePP(getWorldFrame().getBall().getPos(), staticBallPos) > 50)
		{
			complete();
			return cmds;
		}
		
		IVector2 curPoint = circle.nearestPointOnCircle(getPos());
		IVector2 p2 = GeoMath.stepAlongCircle(curPoint, staticBallPos, 0.05f);
		IVector2 dest = GeoMath.stepAlongLine(curPoint, p2, turnVel);
		float orientation = staticBallPos.subtractNew(dest).getAngle();
		
		if (getWorldFrame().isInverted())
		{
			dest = dest.multiplyNew(-1);
			orientation = AngleMath.normalizeAngle(orientation + AngleMath.PI);
		}
		
		cmds.add(new TigerSkillPositioningCommand(dest, orientation));
		return cmds;
	}
	
	
	@Override
	public List<ACommand> calcEntryActions(final List<ACommand> cmds)
	{
		staticBallPos = getWorldFrame().getBall().getPos();
		float botRadius = AIConfig.getGeometry().getBotRadius();
		circle = new Circle(staticBallPos, radius + botRadius);
		return super.calcEntryActions(cmds);
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
