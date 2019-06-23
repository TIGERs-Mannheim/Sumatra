/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.07.2011
 * Author(s): DanielW
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.LookAtCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ABaseRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;


/**
 * TODO DanielW, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author DanielW
 * 
 */
public class Receiver extends ABaseRole
{
	
	/**  */
	private static final long	serialVersionUID		= 283112669260557653L;
	private Vector2f				passTarget;
	private LookAtCon				lookAtCon;
	

	private Vector2f				goal						= AIConfig.getGeometry().getGoalTheir().getGoalCenter();
	private final float			IS_SHOOTEN_TOLERANCE	= 100;
	private final float			SHOOT_VEL				= 1;
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param type
	 */
	public Receiver()
	{
		super(ERole.RECEIVER);
		lookAtCon = new LookAtCon();
		addCondition(lookAtCon);
	}
	

	@Override
	public void update(AIInfoFrame f)
	{
		Line targetToGoal = Line.newLine(passTarget, goal);
		IVector2 intersectPoint;
		if (f.worldFrame.ball.vel.getLength2() > 0.02)
		{
			Line ballLine = new Line(f.worldFrame.ball.pos, f.worldFrame.ball.vel);
			
			try
			{
				intersectPoint = AIMath.intersectionPoint(ballLine, targetToGoal);
				

			} catch (MathException err1)
			{
				intersectPoint = AIMath.leadPointOnLine(f.worldFrame.ball.pos, targetToGoal);
			}
			
		} else
		{
			intersectPoint = AIMath.leadPointOnLine(f.worldFrame.ball.pos, targetToGoal);
		}
		

		IVector2 dest;
		
		if (passTarget.subtractNew(intersectPoint).getLength2() < IS_SHOOTEN_TOLERANCE
				&& f.worldFrame.ball.vel.getLength2() > SHOOT_VEL)
		{
			// shooten = true;
			dest = new Vector2f(intersectPoint);
		} else
		{
			dest = passTarget;
		}
		
		float offset = AIConfig.getGeometry().getBallRadius() + AIConfig.getGeometry().getBotRadius()
				+ AIConfig.getTolerances().getNextToBall();
		IVector2 offsetPosition = dest.addNew(targetToGoal.directionVector().scaleToNew(-offset));
		
		destCon.updateDestination(new Vector2f(offsetPosition));
		
	}
	

	public void setTarget(IVector2 target)
	{
		this.passTarget = new Vector2f(target);
	}
	

	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		
		skills.kickArm();
		
		if (!destCon.checkCondition(wFrame))
		{
			skills.moveTo(destCon.getDestination(), goal);
		}
		
	}
	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
