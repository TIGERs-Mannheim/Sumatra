/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.06.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.ICircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.AimingCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ABaseRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.ballaim.GetBallAndAimSkill;


/**
 * Role that tries to get the Ball and shoots in onto the opponents goal.
 * 
 * @author Malte
 * 
 */
public class GetAndShootRole extends ABaseRole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= 1520958125309161826L;
	
	private final Vector2f		destination;
	
	private Vector2f				target;
	
	private final float			AIM_TOLERANCE		= AIConfig.getTolerances().getAiming();
	private final float			NEAR_TOLERANCE		= AIConfig.getTolerances().getNextToBall();
	/** As the bot should not move _on_ the ball but near it we use the same threshold as in {@link GetBallAndAimSkill} */
	// private static final float POS_TOLERANCE = GetBallAndAimSkill.START_AIM_BALL_DISTANCE_TOLERANCE;
	private AimingCon				aimingCon;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public GetAndShootRole()
	{
		super(ERole.GETBALLANDSHOOT);
		destination = new Vector2f(0, 0);
		target = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
		
		aimingCon = new AimingCon(AIM_TOLERANCE);
		addCondition(aimingCon);
		aimingCon.updateAimingTarget(target);
		aimingCon.setNearTolerance(NEAR_TOLERANCE);
		destCon.setTolerance(250);
		
	}
	

	@Override
	public void update(AIInfoFrame currentFrame)
	{
		destCon.updateDestination(currentFrame.worldFrame.ball.pos);
	}
	

	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		if (!destCon.checkCondition(wFrame))
		{
			ICircle c = new Circle(wFrame.ball.pos, 200);
			IVector2 v = wFrame.ball.pos.subtractNew(wFrame.tigerBots.get(getBotID()).pos);
			skills.moveTo(c.nearestPointOutside(wFrame.ball.pos.addNew(v.scaleToNew(-100))), -1);
		} else
		{
			if (aimingCon.checkCondition(wFrame)) // && destCon.checkCondition(wFrame))
			{
				skills.kickArm();
				skills.kickAuto();
				skills.dribble(false);
				System.out.println("KickAuto!");
			} else
			{
				skills.aiming(target, AIM_TOLERANCE, EGameSituation.SET_PIECE);
				skills.disarm();
				// skills.dribble(true);
			}
		}
	}
	

	@Override
	public void initDestination(IVector2 destination)
	{
		
	}
	

	@Override
	public IVector2 getDestination()
	{
		return this.destination;
	}
	

	public void setTarget(IVector2 target)
	{
		this.target = new Vector2f(target);
	}
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
