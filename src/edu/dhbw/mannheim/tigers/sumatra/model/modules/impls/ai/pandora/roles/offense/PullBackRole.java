/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.05.2011
 * Author(s): TobiasK
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.LookAtCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ABaseRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;


/**
 * This Role should be used if ballpossession is BOTH.
 * 
 * @author TobiasK
 * 
 */

public class PullBackRole extends ABaseRole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID			= 4523052064201118142L;
	

	private final float			AIMING_DISTANCE			= AIConfig.getTolerances().getPositioning()
																				+ AIConfig.getGeometry().getBallRadius()
																				+ AIConfig.getGeometry().getBotRadius();
	
	private final float			DESTINATION_TOLERANCE	= AIMING_DISTANCE;
	
	Vector2							startPosition;
	float								startOrientation;
	float								destinationOrientation;
	Vector2							goalCenter;
	boolean							start							= true;
	private State					innerState;
	float 							botAngle = 0;;
	boolean							noOpponent = false;
	Vector2 							imaginaryViewPoint;
	LookAtCon						lookAtCondition;
	
	private enum State
	{
		DRIBBLE,
		PULLBACK
	};
	
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * creates a new Role, that will pass the ball to a target bot
	 * @see PullBackRole
	 */
	public PullBackRole()
	{
		super(ERole.PULL_BACK_ROLE);
		
		destCon.setTolerance(DESTINATION_TOLERANCE);
		lookAtCondition = new LookAtCon((float) (Math.PI/8));
		addCondition(lookAtCondition);
		
		goalCenter = new Vector2(AIConfig.getGeometry().getGoalTheir().getGoalCenter());
		innerState = State.DRIBBLE;
	}
	
	public void beforeFirstUpdate(AIInfoFrame currentFrame){
		startPosition = new Vector2(currentFrame.worldFrame.ball.pos);
		startOrientation = currentFrame.worldFrame.tigerBots.get(getBotID()).angle;
	
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void update(AIInfoFrame currentFrame)
	{
		if(start){
			beforeFirstUpdate(currentFrame);
			start = false;
		}
		
		Vector2 destination = new Vector2();
		
		
		switch(innerState){
			case DRIBBLE:
				destination.x = currentFrame.worldFrame.ball.pos.x; //(float) (startPosition.x - Math.cos(startOrientation + Math.PI) * 5);
				destination.y = currentFrame.worldFrame.ball.pos.y; //(float) (startPosition.y - Math.sin(startOrientation + Math.PI) * 5);
				
				destCon.updateDestination(destination);
				
				if(currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.BOTH || currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.THEY){
					TrackedBot opponentBallGetter = currentFrame.tacticalInfo.getOpponentBallGetter();
					imaginaryViewPoint = new Vector2((float)(opponentBallGetter.pos.x + Math.cos(opponentBallGetter.angle + Math.PI) * 400),(float)(opponentBallGetter.pos.y + Math.sin(opponentBallGetter.angle + Math.PI) * 400));
					lookAtCondition.updateTarget(imaginaryViewPoint);
				}
				else{
					
					imaginaryViewPoint = new Vector2(currentFrame.worldFrame.ball.pos);
					lookAtCondition.updateTarget(imaginaryViewPoint);
				}			
				
				break;
				
			case PULLBACK:
				destination.x = (float) (startPosition.x + Math.cos(startOrientation + Math.PI) * 200);
				destination.y = (float) (startPosition.y + Math.sin(startOrientation + Math.PI) * 200);
				
				destination.x = destination.x - (300 - (startPosition.x - destination.x));
				destination.y = destination.y - (300 - (startPosition.y - destination.y));
				
				
//				if(currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.BOTH || currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.THEY){
//					TrackedBot opponentBallGetter = currentFrame.tacticalInfo.getOpponentBallGetter();
//					destination.y = (float) (destination.y - (300 - (Math.cos(opponentBallGetter.angle + Math.PI) * 200)));
//				}
				
				destCon.updateDestination(destination);
				lookAtCondition.updateTarget(goalCenter);
		}
		
	
		
	}
	

	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		skills.moveTo(destCon.getDestination(), lookAtCondition.getLookAtTarget());	
		skills.dribble(10000);
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
public void makePullback(){
innerState = State.PULLBACK;
}

	/**
	 * the position the sender shall move to,
	 * to be only set by the play
	 * 
	 * @param desPos
	 */
	public void updateDesignatedPos(IVector2 desPos)
	{
		destCon.updateDestination(desPos);
	}
	
}
