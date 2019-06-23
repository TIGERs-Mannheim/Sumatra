/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.09.2010
 * Author(s): Kï¿½nig
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.standards;

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


/**
 * HiHo, fellows
 * There's a role for Kicker. There's an extra Kicker for set-piece.
 * You may ask: How could anybody willingly work redundant like this?
 * Answer: That's just because the kicker of a set-piece mustn't touch
 * the ball twice.
 * Why: If some bot touches the ball twice:
 * Usually: Who cares
 * Set-piece: Oh no, now there's a set-piece for our enemy. Let's
 * have a look if they are as stupid as our robots.
 * 
 * (c) König (by GuntherB)
 * 
 */
public class KickerSetPiece extends ABaseRole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= -8266636579205950532L;
	
	private static AimingCon	aimingCon;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param type
	 */
	public KickerSetPiece()
	{
		super(ERole.KICKER_SET_PIECE); // TODO StandNear: tolerance = Balldiameter + Botdiameter
		destCon.setTolerance(AIConfig.getTolerances().getPositioning() * 6);
		
		// aiming
		
		aimingCon = new AimingCon(AIConfig.getTolerances().getAiming() * 1.2f);
		addCondition(aimingCon);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void update(AIInfoFrame currentFrame)
	{
		Vector2f dest = new Vector2f(currentFrame.worldFrame.ball.pos);
		if(AIConfig.getGeometry().getFakeOurPenArea().isPointInShape(dest))
		{
			dest = new Vector2f(AIConfig.getGeometry().getFakeOurPenArea().nearestPointOutside(dest));
		}
		destCon.updateDestination(dest);
	}
	

	@Override
	public void calculateSkills(WorldFrame worldFrame, SkillFacade skills)
	{
		if (!destCon.checkCondition(worldFrame))
		{
			skills.moveTo(destCon.getDestination(), aimingCon.getAimingTarget());
			
		} else
		{
			if (!aimingCon.checkCondition(worldFrame))
			{
				skills.aiming(aimingCon, EGameSituation.SET_PIECE);
			}
		}
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void setDestination(IVector2 newDest)
	{
		destCon.updateDestination(newDest);
	}
	

	public void setAimingTarget(IVector2 newTarget)
	{
		aimingCon.updateAimingTarget(newTarget);
	}
}
