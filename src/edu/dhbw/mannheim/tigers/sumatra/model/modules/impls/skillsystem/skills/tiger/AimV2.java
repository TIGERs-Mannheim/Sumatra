/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.05.2011
 * Author(s): DanielW
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger;

import java.util.ArrayList;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillGroup;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.controller.PIDControllerRotateZ2;
import edu.dhbw.mannheim.tigers.sumatra.util.controller.PIDControllerZ2;


/**
 * improved aim skill that uses robots ability to drive curves by driving sideways while rotating
 * 
 * @author DanielW
 * 
 */
public class AimV2 extends ASkill
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final IVector2					targetPosition;
	private final float						initialRadius;
	private float								radius;
	
	private final PIDControllerRotateZ2	aimController			= new PIDControllerRotateZ2(2.0f, 0.11f, 0.00f, 0.25f);
	private final PIDControllerZ2			radiusController		= new PIDControllerZ2(2.0f, 0.1f, 0, 0.5f, 0.1f);
	
	private final PIDControllerRotateZ2	rotationController	= new PIDControllerRotateZ2(AIConfig.getSkills()
																						.getRotatePIDConf());
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param dest
	 * @param objectId
	 */
	public AimV2(IVector2 target, EGameSituation gameSituation)
	{
		super(ESkillName.AIM, ESkillGroup.MOVE);
		
		this.targetPosition = target;
		switch (gameSituation)
		{
			case GAME: // r = bot+ball
				this.initialRadius = AIConfig.getGeometry().getBallRadius() + AIConfig.getGeometry().getBotRadius() - 20;
				break;
			
			case KICK_OFF:
			case SET_PIECE: // r = bot +ball + 100mm
				this.initialRadius = AIConfig.getGeometry().getBallRadius() + AIConfig.getGeometry().getBotRadius() + 100;
				break;
			
			default: // r = 250
				this.initialRadius = 250;
		}
		
		this.radius = initialRadius;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	@Override
	protected boolean compareContent(ASkill newSkill)
	{
		AimV2 move = (AimV2) newSkill;
		final float TARGET_POSITION_TOLERANCE = 5.0f; // treat two targets as the same when difference is less [mm]
		return targetPosition.equals(move.targetPosition, TARGET_POSITION_TOLERANCE)
				&& AIMath.isZero(initialRadius - move.initialRadius);
	}
	

	@Override
	public ArrayList<ACommand> calcActions(ArrayList<ACommand> cmds)
	{
		float currentAngle = getBall().pos.subtractNew(getBot().pos).getAngle();
		float targetAngle = targetPosition.subtractNew(getBall().pos).getAngle();
		
		float aimVelocity = aimController.process(currentAngle, targetAngle, 0);
//		System.out.println("Aim V2 vel is: " + aimVelocity);
		// System.out.println("Aim V2 prev er:" + aimController.getPreviousError());
		//
		// difference in radius
		float currentRadius = getBall().pos.subtractNew(getBot().pos).getLength2();
		// float currentDiff = getBall().pos.subtractNew(getBot().pos).getLength2() - radius;
		// System.out.println("AimV2 diff: " + currentDiff);
		
		float forwardVelocity = radiusController.process(currentRadius / 1000, radius / 1000, 0);
		

		float turnVelocity = rotationController.process(getBot().angle, getBall().pos.subtractNew(getBot().pos)
				.getAngle(), 0);
		
		if (AIMath.isZero(Math.abs(currentAngle - targetAngle), AIConfig.getTolerances().getAiming())
				&& getBot().vel.getLength2() < AIConfig.getSkills().getMoveSpeedThreshold()
				&& AIMath.isZero(Math.abs(getBall().pos.subtractNew(getBot().pos).getAngle() - getBot().angle), AIConfig
						.getTolerances().getViewAngle()) && AIMath.isZero(Math.abs(currentRadius - radius), 5))
		{
			// complete();
			radius -= 10;
			if (radius <= AIConfig.getGeometry().getBallRadius() + AIConfig.getGeometry().getBotRadius() - 30)
			{
				complete();
			}
			cmds.add(new TigerMotorMoveV2(Vector2.ZERO_VECTOR, 0, 0));
		} else
		{
			cmds.add(new TigerMotorMoveV2(new Vector2(aimVelocity, -forwardVelocity), aimVelocity / (currentRadius / 1000)
					+ turnVelocity, 0));
		}
		

		return cmds;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
