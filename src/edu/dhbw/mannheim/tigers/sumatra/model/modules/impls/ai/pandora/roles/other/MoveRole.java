/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.10.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.LookAtCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.AroundTheBallPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ABaseRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;


/**
 * <pre>
 * ----o
 * ---o
 * --o <--r--> Ball
 * ---o
 * ----o
 * 
 * |x - center| = radius   <-- circ equation
 * 
 * <pre>
 * @author Malte
 * TODO: lookAt condition!
 * 
 */
public class MoveRole extends ABaseRole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= 5551236360355949612L;
	
	/** position of the object that is surrounded by the bots */
	private IVector2				center;
	/** radius of the circle */
	private float					radius;
	/** direction center - bot */
	private Vector2				direction;
	
	private LookAtCon				lookAt;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param type
	 */
	public MoveRole()
	{
		super(ERole.MOVEROLE);
		
		lookAt = new LookAtCon();
		addCondition(lookAt);
		
		direction = new Vector2(AIConfig.INIT_VECTOR);
		center = AIConfig.INIT_VECTOR;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Updates center, radius and direction for the MoveRole.
	 * Is called in {@link AroundTheBallPlay#beforeUpdate}.
	 * 
	 * @param center
	 * @param radius
	 * @param direction
	 */
	public void updateCirclePos(IVector2 center, float radius, IVector2 direction)
	{
		this.center = center;
		this.radius = radius;
		this.direction = new Vector2(direction);
	}
	

	@Override
	public void update(AIInfoFrame currentFrame)
	{
		// sets the length of the vector to 'radius'
		direction.scaleTo(radius);
		// adds the vector to the center
		
		Vector2 destination = center.addNew(direction);
		if(AIConfig.getGeometry().getFakeOurPenArea().isPointInShape(destination))
		{
			destination = AIConfig.getGeometry().getFakeOurPenArea().nearestPointOutside(destination);
		}
		destCon.updateDestination(destination);
		
		// update lookAt
		lookAt.updateTarget(center);
		
		if (direction.equals(AIConfig.INIT_VECTOR) || center.equals(AIConfig.INIT_VECTOR))
		{
			System.out.println("MoveRole: Wants to go to InitVector... need better data, dudes! :O");
		}
	}
	

	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		boolean correctPosition = destCon.checkCondition(wFrame);
		boolean correctAngle = lookAt.checkCondition(wFrame);
		
		if (!correctPosition || !correctAngle)
		{
			skills.moveTo(destCon.getDestination(), lookAt.getLookAtTarget());
		}
		
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
