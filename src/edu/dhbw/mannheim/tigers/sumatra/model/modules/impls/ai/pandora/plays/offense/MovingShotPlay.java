/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.11.2012
 * Author(s): Mark Geiger
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense;

import java.awt.Color;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole.EBallContact;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.ChipKickRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.ShooterV2Role;


/**
 * Makes the direct ShotPlay more effective and try to save time during the gameplay.
 * First this play try to get the ball with the ball getter-role, afterwards it will
 * shoot to the best point.
 * 
 * @author Mark Geiger
 * 
 */
public class MovingShotPlay extends AOffensivePlay

{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log				= Logger.getLogger(MovingShotPlay.class.getName());
	
	private final long				predictionTime	= 50;
	private long						oldTime			= System.currentTimeMillis();
	
	private boolean					firstrun			= true;
	private boolean					prePredict		= true;
	
	private final BallGetterRole	ballGetter;
	private final ShooterV2Role	shooter;
	private final MoveRole			move;
	private ChipKickRole				chipper;
	
	private Vector2					ballPositionOld;
	private Vector2					ballDirection;
	private Vector2					ballDest;
	private float						ballSpeed;
	private float						ballSpeed2;
	
	private Line						line;
	private EState						state				= EState.INITIALPREDICTION;
	
	enum EState
	{
		INITIALPREDICTION,
		POSITIONING,
		WAIT,
		ADJUST,
		SHOOT;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public MovingShotPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		addCriterion(new BallPossessionCrit(EBallPossession.NO_ONE, EBallPossession.WE));
		ballGetter = new BallGetterRole(AIConfig.getGeometry().getGoalTheir().getGoalCenter(), EBallContact.DISTANCE);
		shooter = new ShooterV2Role();
		move = new MoveRole(EMoveBehavior.LOOK_AT_BALL);
		
		ballPositionOld = new Vector2(aiFrame.worldFrame.getBall().getPos());
		setTimeout(Long.MAX_VALUE);
		addAggressiveRole(move,
				aiFrame.worldFrame.getTiger(AiMath.getReceiver(aiFrame, aiFrame.worldFrame.tigerBotsAvailable)).getPos());
		
		oldTime = System.currentTimeMillis();
		setTimeout(6);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		if (firstrun == true)
		{
			firstrun = false;
			move.updateDestination(move.getPos());
		}
		
		long currentTime = System.currentTimeMillis();
		switch (state)
		{
			case INITIALPREDICTION:
				if ((currentTime - oldTime) > predictionTime)
				{
					Vector2 ballPosNew = new Vector2(currentFrame.worldFrame.ball.getPos());
					ballDirection = ballPosNew.subtractNew(ballPositionOld);
					state = EState.POSITIONING;
					ballSpeed = ballDirection.getLength2();
					ballSpeed2 = currentFrame.worldFrame.getBall().getVel().getLength2();
					ballDest = predictWhereBallWillBe(currentFrame);
					
				} else if (((currentTime - oldTime) > (predictionTime / 2)) && (prePredict == true))
				{
					prePredict = false;
					
				}
				break;
			case POSITIONING:
				state = EState.WAIT;
				resetTimer();
				break;
			case WAIT:
				
				move.updateDestination(ballDest);
				currentFrame.addDebugShape(new DrawableCircle(
						new Circle(GeoMath.leadPointOnLine(move.getPos(), line), 100), Color.CYAN));
				if (GeoMath.distancePP(ballDest, currentFrame.worldFrame.ball.getPos()) < 130)
				{
					switchRoles(move, ballGetter, currentFrame);
					resetTimer();
					state = EState.ADJUST;
				}
				break;
			case ADJUST:
				if (ballGetter.isCompleted())
				{
					
					boolean freeWayToGoal = currentFrame.getTacticalInfo().getTigersApproximateScoringChance();
					
					if (freeWayToGoal)
					{
						switchRoles(ballGetter, shooter, currentFrame);
					} else
					{
						chipper = new ChipKickRole(AiMath.determinChipShotTarget(currentFrame.worldFrame, 400.0f,
								move.getPos(), 2000), 1000);
						switchRoles(ballGetter, chipper, currentFrame);
					}
					state = EState.SHOOT;
					resetTimer();
					setTimeout(4);
				}
				break;
			case SHOOT:
				if (shooter.isCompleted())
				{
					changeToFinished();
				}
				break;
			default:
				break;
		}
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		// idle
	}
	
	
	@Override
	protected void timedOut(AIInfoFrame frame)
	{
		changeToFailed();
	}
	
	
	/**
	 * 
	 * @author Mark Geiger
	 * 
	 * @return predicted ballPosition nearest to activeRole
	 */
	private Vector2 predictWhereBallWillBe(AIInfoFrame frame)
	{
		Vector2 output = null;
		
		if (!ballDirection.isZeroVector())
		{
			
			log.error("Calc BallSpeed: Vektor: " + ballSpeed);
			log.error("Calc BallSpeed: getVel: " + ballSpeed2);
			
			Line ballLine = new Line(frame.worldFrame.ball.getPos(), ballDirection);
			Vector2 leadPoint = GeoMath.leadPointOnLine(move.getPos(), ballLine);
			Vector2 viewBotToBall = frame.worldFrame.ball.getPos().subtractNew(leadPoint);
			float scalProduct = viewBotToBall.scalarProduct(ballDirection);
			ballDirection.normalize();
			
			if (scalProduct < 0)
			{
				log.error("Entgegengesetzt Richtung");
				output = leadPoint;
			} else
			{
				log.error("Gleiche Richtung");
				output = leadPoint;
			}
			
			
		} else
		{
			changeToFailed();
		}
		
		return output;
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}


// ToDo, abort wenn distanz ball LeadPoint größer wird.
// keine Bot targets außerhalb Spielfeld
// default chipKick, wenn auch mit chip alles blockiert.
// wenn ball weg von bot fährt, was dann ?
// timeouts regeln
// play richtig beenden, wenn fehler

// ist ball vor mir, oder hinter mir ? vorbei fahren und schuss!
// second calculation for Lead Point / wohin fahren?
// Criterion hinzufügen play nicht wählen, wenn bald nicht passend speed + bereich

// Move role, nicht zum ball drehen, sondern in gewissen fällen zwischen Ball und EnemyGoal, zumindest
// wenn ball nicht von hinten anrollt.
