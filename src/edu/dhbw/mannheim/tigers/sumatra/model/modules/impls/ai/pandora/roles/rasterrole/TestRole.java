/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.rasterrole;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.fieldraster.FieldRasterGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.AIRectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.BallVisibleCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ABaseRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;


/**
 * This is a role for testing the positioning field raster.
 * Bot should move to a random point within the specified rectangle in field 'POSITIONRECTANGLE'
 * while the target is the ball. when ball is not visible the role changes its
 * position.
 * 
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class TestRole extends ABaseRole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final BallVisibleCon	ballVisibleCon;
	

	private final static int		POSITIONRECTANGLE	= 6;
	private final AIRectangle		rectangle;
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long		serialVersionUID	= 3615922197485593960L;
	
	
	public TestRole()
	{
		super(ERole.TESTER);
		
		rectangle = FieldRasterGenerator.getInstance().getPositioningRectangle(POSITIONRECTANGLE);
		
		destCon.updateDestination(rectangle.getRandomPointInShape());
		
		ballVisibleCon = new BallVisibleCon();
		
		addCondition(ballVisibleCon);
		
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	

	@Override
	public void update(AIInfoFrame currentFrame)
	{
		if (!AIMath.p2pVisibility(currentFrame.worldFrame, destCon.getDestination(), currentFrame.worldFrame.ball.pos,
				getBotID()))
		{
			Vector2 dest = FieldRasterGenerator.getInstance().getRandomConditionPoint(rectangle, currentFrame.worldFrame,
					getBotID(), ballVisibleCon);
			

			destCon.updateDestination(dest);
		}
		
	}
	

	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		// correct position?
		if (!destCon.checkCondition(wFrame))
		{
			skills.moveTo(destCon.getDestination());
		}
	}
}
