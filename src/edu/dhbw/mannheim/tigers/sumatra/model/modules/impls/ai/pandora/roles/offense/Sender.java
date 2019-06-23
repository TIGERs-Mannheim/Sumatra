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
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
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
public class Sender extends ABaseRole
{
	/**  */
	private static final long		serialVersionUID	= -8374401797710010387L;
	
	private LookAtCon					lookAtCon;
	
	float									usedSpace;
	/** distance between the centers of bot and ball (when bot looks at the ball) */
	private final float				SPACE_MIN			= 0;
	/** distance between the centers of bot and ball (when bot doesn't look at the ball) */
	private final float				SPACE_MAX			= AIConfig.getRoles().getBallGetter().getSpaceMax();
	

	private final EGameSituation	gameSituation;
	
	private IVector2					target;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	

	public Sender(EGameSituation gameSit)
	{
		super(ERole.ARMED_BALL_GETTER);
		
		lookAtCon = new LookAtCon();
		addCondition(lookAtCon);
		
		gameSituation = gameSit;
		this.target = null;
		
	}
	

	/**
	 * The idea of this BallGetter is the following:
	 * * We have a ball somewhere on the field
	 * * We want to "get" it, while looking at a given viewpoint (if the viewpoint is not defined, simply look at the
	 * ball)
	 */
	@Override
	public void update(AIInfoFrame currentFrame)
	{
		Vector2 botPos = new Vector2(currentFrame.worldFrame.tigerBots.get(getBotID()).pos);
		Vector2 ballPos = new Vector2(currentFrame.worldFrame.ball.pos);
		
		float currentAngleBetweenBallAndBot = AIMath.angleBetweenXAxisAndLine(botPos, ballPos);
		
		// angle between ball and bot (0-2PI);
		float angleBB = Math.abs(currentAngleBetweenBallAndBot - currentFrame.worldFrame.tigerBots.get(getBotID()).angle);
		
		// angle between ball and bot(0-PI)
		if (angleBB > Math.PI)
		{
			angleBB = (float) (Math.PI * 2 - angleBB);
		}
		
		// calculate desired distance between bot and ball. distance depends on the angle between bot and ball
		usedSpace = (float) (SPACE_MIN + (SPACE_MAX - SPACE_MIN) * (angleBB / Math.PI));
		
		destCon.updateDestination(calculateDestination(currentFrame));
		
		lookAtCon.updateTarget(target);
		
		currentFrame.addDebugPoint(lookAtCon.getLookAtTarget());
	}
	

	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		boolean correctPosition = destCon.checkCondition(wFrame);
		boolean correctAngle = lookAtCon.checkCondition(wFrame);
		
		if (gameSituation == EGameSituation.SET_PIECE || gameSituation == EGameSituation.KICK_OFF)
		{
			skills.setMoveFast(false);
		} else
		{
			skills.setMoveFast(true);
		}
		if (!correctPosition || !correctAngle)
		{
			skills.moveTo(destCon.getDestination(), lookAtCon.getLookAtTarget());
		}
		
		final float kicklength = target.subtractNew(getPos(wFrame)).getLength2();
		final float END_VEL = 1.0f; // m/s
		// skills.kickArm(kicklength, END_VEL);
		skills.kickArmDirect(701);
	}
	

	/**
	 * calculates the position, where our bot shall be send to. In later versions, you will differ between a rolling ball
	 * and a sitting ball here
	 */
	private IVector2 calculateDestination(AIInfoFrame frame)
	{
		WorldFrame worldFrame = frame.worldFrame;
		IVector2 ballPos = worldFrame.ball.pos;
		

		float FACTOR = 100;
		ballPos = ballPos.addNew(worldFrame.ball.vel.multiplyNew(FACTOR));
		return AIMath.stepAlongLine(ballPos, lookAtCon.getLookAtTarget(), -usedSpace);
		

	}
	

	public void setDestTolerance(float newTolerance)
	{
		destCon.setTolerance(newTolerance);
	}
	

	public void setTarget(IVector2 target)
	{
		this.target = target;
	}
}
