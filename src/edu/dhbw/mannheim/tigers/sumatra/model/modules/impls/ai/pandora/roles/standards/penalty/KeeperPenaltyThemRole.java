/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.02.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.standards.penalty;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.LookAtCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ABaseRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;


/**
 * The Penalty Keeper.<br>
 * Elfmeterkiller!
 * 
 * @author Malte
 */
public class KeeperPenaltyThemRole extends ABaseRole
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -3596166487004268419L;
	
	private Vector2				destination;
	private Vector2				target;
	
	private LookAtCon				lookAtCon;
	
	/** Indicates whether the penalty has been released. */
	private boolean				ready;
	
	private TrackedBot shooter = null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public KeeperPenaltyThemRole()
	{
		super(ERole.KEEPER_PENALTY_THEM);
		
		ready = false;
		lookAtCon = new LookAtCon();
		addCondition(lookAtCon);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	@Override
	public void update(AIInfoFrame currentFrame)
	{
		// The command "penalty for them" just came up!
		if (!ready )
		{
			destination = new Vector2(AIConfig.getGeometry().getGoalOur().getGoalCenter());
			target = new Vector2(currentFrame.worldFrame.ball.pos);
		}
		// Penalty has been released!
		else if (ready )
		{
			// TODO: skill so einsetzen, dass er beim zielpunkt nicht auf v = 0 sein muss!
			// dann die range was runtersetzen.
//			float f = (float) Math.random();
//			f = f * 400;
//			f = f - 200;
//			destination.setY(f);
//			
			try
			{
				float y = AIMath.intersectionPoint(AIConfig.getGeometry().getGoalLineOur(),
						new Line(shooter.pos, new Vector2(shooter.angle))).y();
				if(y > 2000 || y < -2000)
				{
					y = 0;
				}
				destination.setY(y);
			} catch (MathException err)
			{
				destination.setY(0);
			}
			destination.setX(destination.x() + 110);	// former: 80
		}
		

		lookAtCon.updateTarget(target);
		destCon.updateDestination(destination);
		

	}
	

	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		// log.debug("calcSkills");
		
		boolean dest = destCon.checkCondition(wFrame);
		boolean angl = lookAtCon.checkCondition(wFrame);
		
		// conditions completed?
		if (!dest || !angl)
		{
			if(!ready)
			{
				skills.setBallIsObstacle(true);
			}
			skills.moveTo(destCon.getDestination(), lookAtCon.getLookAtTarget());
		}
	}
	

	@Override
	public boolean isKeeper()
	{
		return true;
	}
	

	public void setReady(boolean ready)
	{
		this.ready = ready;
	}
	

	public void setShooter(TrackedBot shooter)
	{
		this.shooter = shooter;
	}


	public TrackedBot getShooter()
	{
		return shooter;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
