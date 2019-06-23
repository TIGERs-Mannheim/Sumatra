/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.06.2013
 * Author(s): jan, SebastianN
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.tecchallenges;

import java.awt.Color;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole.EBallContact;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.ShooterV2Role;


/**
 * This Play should be called for the technical challenge in 2013
 * @author jan
 * 
 */
public class ShootingChallengePlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log								= Logger.getLogger(ShootingChallengePlay.class.getName());
	
	private BallGetterRole			ballGetter;
	private ShooterV2Role			shooter;
	private MoveRole					haltrole;
	private MoveRole					gotoStart;
	private long						timeoutTimestamp				= 0;
	private final long				TIMEOUT_AIMING					= 20000;
	private final float				INTERFERE_RADIUS				= AIConfig.getGeometry().getBotRadius() * 3f;
	
	private EState						state								= EState.WAIT;
	private boolean					allowHaltBallgetterSwitch	= false;
	private boolean					allowGetterShooterSwitch	= false;
	
	enum EState
	{
		GET,
		WAIT,
		STARTPOS,
		PREPARE_SHOOT,
		SHOOT;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public ShootingChallengePlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		addCriterion(new BallPossessionCrit(EBallPossession.NO_ONE, EBallPossession.WE));
		haltrole = new MoveRole(MoveRole.EMoveBehavior.NORMAL);
		
		addAggressiveRole(haltrole, aiFrame.worldFrame.ball.getPos());
		setTimeout(Long.MAX_VALUE);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected void beforeFirstUpdate(AIInfoFrame frame)
	{
		haltrole.updateDestination(haltrole.getPos());
	}
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		switch (state)
		{
		
			case WAIT:
				if (currentFrame.refereeMsg != null)
				{
					if ((currentFrame.refereeMsg.getCommand() == Command.DIRECT_FREE_BLUE)
							|| (currentFrame.refereeMsg.getCommand() == Command.DIRECT_FREE_YELLOW))
					{
						log.debug("Change roles from wait to gotoStart");
						log.warn("Wait State: current Role" + getRoles().get(0));
						gotoStart = new MoveRole(MoveRole.EMoveBehavior.DO_COMPLETE);
						// destination is the kickoff place in the fieldcenter
						switchRoles(haltrole, gotoStart, currentFrame);
						gotoStart.updateDestination(AIConfig.getGeometry().getCenter());
						state = EState.STARTPOS;
						allowHaltBallgetterSwitch = true;
					}
				}
				changeToHalt(currentFrame);
				break;
			case STARTPOS:
				// log.warn(gotoStart.getCurrentState() + "" + gotoStart.getPos());
				if (gotoStart.isCompleted())
				{
					state = EState.GET;
					ballGetter = new BallGetterRole(AIConfig.getGeometry().getGoalTheir().getGoalCenter(),
							EBallContact.DISTANCE);
					log.debug("Change roles from gotoStart to BallGetter");
					switchRoles(gotoStart, ballGetter, currentFrame);
				}
				changeToHalt(currentFrame);
				break;
			case GET:
				if (ballGetter.isCompleted())
				{
					if (allowHaltBallgetterSwitch)
					{
						allowHaltBallgetterSwitch = false;
						log.debug("Change roles from ball getter to shooter");
						timeoutTimestamp = System.currentTimeMillis();
						
						shooter = new ShooterV2Role(true);
						switchRoles(ballGetter, shooter, currentFrame);
						state = EState.SHOOT;
						allowGetterShooterSwitch = true;
					}
				}
				changeToHalt(currentFrame);
				break;
			case SHOOT:
				long timePassed = System.currentTimeMillis() - timeoutTimestamp;
				if (!shooter.isShooting() && (checkForReady(currentFrame) || (timePassed > TIMEOUT_AIMING)))
				{
					if ((timePassed > TIMEOUT_AIMING)
							&& (currentFrame.tacticalInfo.getBallPossession().getEBallPossession() == EBallPossession.WE))
					{
						log.debug("Timeout for aiming was reached. GIVE IT YOUR ALL, SHOOTER-BRO!");
					}
					shooter.setReady(true);
				}
				if (shooter.isShooting() && !shooter.isCompleted())
				{
					shooter.updateDestination(currentFrame.worldFrame.ball.getPos());
					shooter.updateLookAtTarget(shooter.getTarget());
				}
				if (shooter.isCompleted())
				{
					if (allowGetterShooterSwitch)
					{
						allowGetterShooterSwitch = false;
					}
				}
				changeToHalt(currentFrame);
				break;
			default:
				changeToHalt(currentFrame);
				break;
		}
	}
	
	
	/**
	 * @param currentFrame
	 * @return Whether someone could possibly block our shot.
	 */
	
	private boolean hasPossibleBlocker(AIInfoFrame currentFrame)
	{
		float shotLineLength = GeoMath.distancePP(currentFrame.worldFrame.ball.getPos(), shooter.getTarget());
		int decreasingFactorSteps = (int) Math.floor(shotLineLength / 150.0f);
		for (int i = decreasingFactorSteps, j = 0; i > 0; i--, j++)
		{
			float decreaseFactor = (i + decreasingFactorSteps) / (decreasingFactorSteps * 2.0f);
			IVector2 newCirclePos = GeoMath.stepAlongLine(currentFrame.worldFrame.ball.getPos(), shooter.getTarget(),
					j * 150);
			currentFrame.addDebugShape(new DrawableCircle(new Circle(newCirclePos, INTERFERE_RADIUS * decreaseFactor),
					Color.BLACK));
			BotIDMap<TrackedBot> defender = new BotIDMap<TrackedBot>();
			for (TrackedBot tiger : currentFrame.worldFrame.tigerBotsVisible.values())
			{
				if (tiger == shooter.getBot())
				{
					continue;
				}
				defender.put(tiger.getId(), tiger);
			}
			for (TrackedBot enemy : currentFrame.worldFrame.foeBots.values())
			{
				defender.put(enemy.getId(), enemy);
			}
			for (TrackedBot enemy : defender.values())
			{
				// log.debug("Checking distance between Bot(" + enemy.getId() + ") and target...: "
				// + GeoMath.distancePP(enemy, newCirclePos) + " <-> " + (INTERFERE_RADIUS * decreaseFactor));
				if (GeoMath.distancePP(enemy, newCirclePos) < (INTERFERE_RADIUS * decreaseFactor))
				{
					return true;
				}
			}
		}
		currentFrame.addDebugShape(new DrawableCircle(new Circle(shooter.getTarget(), INTERFERE_RADIUS / 1.4f),
				Color.BLACK));
		if (GeoMath.distancePP(currentFrame.tacticalInfo.getOpponentKeeper(), shooter.getTarget()) < (INTERFERE_RADIUS / 1.4f))
		{
			return true;
		}
		return false;
	}
	
	
	private boolean checkForReady(AIInfoFrame currentFrame)
	{
		if (shooter.getTarget() != null)
		{
			if (!shooter.checkMoveCondition(currentFrame.worldFrame))
			{
				return false;
			}
			if (GeoMath.p2pVisibility(currentFrame.worldFrame, currentFrame.worldFrame.ball.getPos(), shooter.getTarget()))
			{
				// log.debug("Ball -> Target visible!");
				if (!hasPossibleBlocker(currentFrame))
				{
					// log.debug("No target was found");
					return true;
				}
			}
		} else
		{
			log.debug("checkForReady failed -> no 'best target' found.");
		}
		return false;
	}
	
	
	/**
	 * @param currentFrame
	 */
	private void changeToHalt(AIInfoFrame currentFrame)
	{
		// log.warn(getRoles().get(0));
		if (currentFrame.refereeMsg != null)
		{
			if ((currentFrame.refereeMsg.getCommand() == Command.HALT)
					|| (currentFrame.refereeMsg.getCommand() == Command.STOP))
			{
				log.debug("Change roles from active role to wait state");
				allowGetterShooterSwitch = false;
				allowHaltBallgetterSwitch = false;
				state = EState.WAIT;
				log.warn("New role cycle");
				haltrole = new MoveRole(MoveRole.EMoveBehavior.NORMAL);
				haltrole.updateDestination(getRoles().get(0).getPos());
				switchRoles(getRoles().get(0), haltrole, currentFrame);
			}
		}
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		// draw stuff.
		if (shooter != null)
		{
			float shotLineLength = GeoMath.distancePP(currentFrame.worldFrame.ball.getPos(), shooter.getTarget());
			int decreasingFactorSteps = (int) Math.floor(shotLineLength / 150.0f);
			for (int i = decreasingFactorSteps, j = 0; i > 0; i--, j++)
			{
				float decreaseFactor = (i + decreasingFactorSteps) / (decreasingFactorSteps * 2.0f);
				IVector2 newCirclePos = GeoMath.stepAlongLine(currentFrame.worldFrame.ball.getPos(), shooter.getTarget(),
						j * 150);
				currentFrame.addDebugShape(new DrawableCircle(new Circle(newCirclePos, INTERFERE_RADIUS * decreaseFactor),
						Color.BLACK));
				BotIDMap<TrackedBot> defender = new BotIDMap<TrackedBot>();
				for (TrackedBot tiger : currentFrame.worldFrame.tigerBotsVisible.values())
				{
					defender.put(tiger.getId(), tiger);
				}
				for (TrackedBot enemy : currentFrame.worldFrame.foeBots.values())
				{
					defender.put(enemy.getId(), enemy);
				}
				for (TrackedBot enemy : defender.values())
				{
					if (GeoMath.distancePP(enemy, newCirclePos) < (INTERFERE_RADIUS * decreaseFactor))
					{
						currentFrame.addDebugShape(new DrawableCircle(new Circle(enemy.getPos(), INTERFERE_RADIUS
								* decreaseFactor), Color.RED));
					}
				}
			}
			currentFrame.addDebugShape(new DrawableCircle(new Circle(shooter.getTarget(), INTERFERE_RADIUS / 1.4f),
					Color.BLACK));
		}
	}
	
	
	@Override
	protected void timedOut(AIInfoFrame frame)
	{
		log.warn("Failed timeout");
		changeToCanceled();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}