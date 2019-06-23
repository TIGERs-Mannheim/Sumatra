/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.08.2010
 * Author(s):
 * FlorianS
 * TobiasK
 * DanielW
 * ChristianK
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
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
 * BallGetter goes behind the ball and acquires ball possession.
 * Assumes the ball just sits somewhere and no-one possesses the ball.
 * If the constructor is used without the viewPoint parameter the fastest
 * way towards the ball will be taken.
 * Otherwise viewPoint is the point the bot will look at when it is
 * behind the ball in order to shoot right after getting the ball. (not implemented in version istanbul)
 * 
 * @author FlorianS, TobiasK
 */
public class BallGetterRole extends ABaseRole
{
	/**  */
	private static final long		serialVersionUID		= -3377800609979002337L;
	
	private IVector2					viewPoint				= AIConfig.INIT_VECTOR;
	
	private LookAtCon					lookAtCon;
	
	float									usedSpace;
	/** distance between the centers of bot and ball (when bot looks at the ball) */
	private final float				SPACE_MIN				= AIConfig.getRoles().getBallGetter().getSpaceMin();
	/** distance between the centers of bot and ball (when bot doesn't look at the ball) */
	private final float				SPACE_MAX				= AIConfig.getRoles().getBallGetter().getSpaceMax();
	/**
	 * velocity limit until which the ball is considered as sitting still
	 * will not be considered in this version
	 */
	// private final float VELOCITY_TOLERANCE = AIConfig.getRoles().getBallGetter().getVelocityTolerance();
	/** distance between bot and ball when the bot shall start dribbling */
	private final float				DRIBBLING_DISTANCE	= AIConfig.getRoles().getBallGetter().getDribblingDistance();
	
	/** distance botPosition <-> ball that the bot tries to go to first */
	private final float				POSITIONING_DISTANCE	= AIConfig.getRoles().getBallGetter().getPositioningPreAiming();
	
	private final EGameSituation	gameSituation;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	public BallGetterRole(EGameSituation gameSituation)
	{
		this(AIConfig.INIT_VECTOR, gameSituation);
	}
	

	public BallGetterRole(IVector2 viewPoint, EGameSituation situation)
	{
		super(ERole.BALL_GETTER);
		
		this.viewPoint = viewPoint;
		
		lookAtCon = new LookAtCon();
		addCondition(lookAtCon);
		
		this.gameSituation = situation;
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
		
		Vector2f dest = new Vector2f((calculateDestination(currentFrame)));
		if (AIConfig.getGeometry().getFakeOurPenArea().isPointInShape(dest))
		{
			dest = new Vector2f(AIConfig.getGeometry().getFakeOurPenArea().nearestPointOutside(dest));
		}
		destCon.updateDestination(dest);
		
		if (!viewPoint.equals(AIConfig.INIT_VECTOR))
		{
			lookAtCon.updateTarget(viewPoint);
		} else
		{
			lookAtCon.updateTarget(currentFrame.worldFrame.ball.pos);
		}
	}
	

	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		if (gameSituation == EGameSituation.SET_PIECE || gameSituation == EGameSituation.KICK_OFF)
		{
			skills.setMoveFast(false);
		}
		

		final float distBotBall = AIMath.distancePP(getPos(wFrame), wFrame.ball.pos);
		
		skills.dribble(distBotBall <= DRIBBLING_DISTANCE && gameSituation == EGameSituation.GAME);
		
		boolean correctPosition = destCon.checkCondition(wFrame);
		boolean correctAngle = lookAtCon.checkCondition(wFrame);
		
		if (!correctPosition || !correctAngle)
		{
			skills.moveTo(destCon.getDestination(), lookAtCon.getLookAtTarget());
		}
	}
	

	/**
	 * calculates the position, where our bot shall be send to. In later versions, you will differ between a rolling ball
	 * and a sitting ball here
	 */
	private IVector2 calculateDestination(AIInfoFrame frame)
	{
		WorldFrame worldFrame = frame.worldFrame;
		final Vector2f ballPos = worldFrame.ball.pos;
		
		if (!viewPoint.equals(AIConfig.INIT_VECTOR))
		{
			if (gameSituation == EGameSituation.GAME)
			{
				return AIMath.stepAlongLine(ballPos, lookAtCon.getLookAtTarget(), -usedSpace);
			} else
			{
				return AIMath.stepAlongLine(ballPos, lookAtCon.getLookAtTarget(), -POSITIONING_DISTANCE);
			}
		} else
		{
			if (gameSituation == EGameSituation.GAME)
			{
				return AIMath.stepAlongLine(ballPos, worldFrame.tigerBots.get(getBotID()).pos, usedSpace);
			} else
			{
				return AIMath.stepAlongLine(ballPos, worldFrame.tigerBots.get(getBotID()).pos, POSITIONING_DISTANCE);
			}
		}
	}
	

	public void setViewPoint(IVector2 newViewPoint)
	{
		this.viewPoint = newViewPoint;
	}
	

	public void setDestTolerance(float newTolerance)
	{
		destCon.setTolerance(newTolerance);
	}
	

	public boolean hasReachedDestination(AIInfoFrame curFrame)
	{
		return destCon.checkCondition(curFrame.worldFrame);
	}
}