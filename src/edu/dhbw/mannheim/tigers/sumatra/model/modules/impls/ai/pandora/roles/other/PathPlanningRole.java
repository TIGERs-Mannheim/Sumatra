/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.10.2010
 * Author(s): ChristianK
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.DestinationCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;


/**
 * Role for PP testing
 * 
 * @author ChristianK
 * 
 */
public class PathPlanningRole extends ARole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= -8707667219906676270L;
	
	private final float			BOT_RADIUS			= AIConfig.getGeometry().getBotRadius();
	private final float			BALL_RADIUS			= AIConfig.getGeometry().getBallRadius();
	
	DestinationCon					standNear			= new DestinationCon();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public PathPlanningRole()
	{
		super(ERole.PP_ROLE);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * determine destination, according to AIInfoframe, protectionArea and type
	 */
	@Override
	public void update(AIInfoFrame currentFrame)
	{
		log.debug("updateSkills");
		
		Vector2 ball = new Vector2(currentFrame.worldFrame.ball.pos);
		
		standNear.updateDestination(new Vector2(ball.x - (BOT_RADIUS + BALL_RADIUS + 5), ball.y));
	}
	

	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		// log.debug("calcSkills");
		
		// log.info("botId:" + getBotID() + wFrame.tigerBots);
		// --- do not handle dummy bots!! ---
		/*
		 * if (getBotID() > 999)
		 * {
		 * return skills;
		 * }
		 */
		// log.info("WEITER:");
		
		// correct position?
		// if (!standNear.checkCondition(wFrame, getBotID()))
		// {
		// Path newPath = sisyphus.getPath(wFrame, getBotID(), standNear.getDestination());
		// // TODO: newPath = null - solved... Remove later...
		// if (newPath.changed)
		// {
		
		// for (PathPoint pathPoint : newPath.path)
		// {
		skills.moveTo(standNear.getDestination(), 0);
		// }
		
		// }
		// }
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public IVector2 getDestination()
	{
		return standNear.getDestination();
	}
	

	@Override
	public void initDestination(IVector2 destination)
	{
		// Nothing to do here... (s???)
	}
}
