/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 12, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2.EKickerMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.ChipParams;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.EKickDevice;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.TigerDevices;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.CSVExporter;


/**
 * Perform straight (and chip) kicks with static and moving balls
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KickSkill extends PositionSkill
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log							= Logger.getLogger(KickSkill.class
																					.getName());
	
	// ###### general params ###########
	@Configurable(comment = "Vel [m/s] of the ball when hitting pass receiver. Used for calculation of shoot duration when passing.")
	private static float				passEndVel					= 0.5f;
	@Configurable(comment = "Time [ms] after which the skill will time out")
	private static int				runtimeTimeout				= 5000;
	@Configurable(comment = "Bool - Export move data to a csv file")
	private static boolean			exportMoveData				= false;
	
	
	// ###### "check if complete" params #########
	@Configurable(comment = "Tol [mm] - Max. distance between initial ball pos and current ball pos before completing skill when ball was stopped")
	private static int				ballToInitPosTol			= 200;
	@Configurable(comment = "Tol [m/s] - Offset for initial ball velocity. If current ball vel is higher than the sum, skill will be completed")
	private static float				ballVelHigherTol			= 0.8f;
	
	// ###### parameters ##########
	@Configurable(comment = "Delay [s] to wait before sending kick command, starting when kicking is possible")
	private static float				workingKickDelay			= 0.0f;
	@Configurable(comment = "Delay [s] to wait before sending kick command, starting when kicking is possible")
	private static float				brokenKickDelay			= 0.7f;
	private float						kickDelay					= workingKickDelay;
	
	
	@Configurable(comment = "Vel [m/s] that is assumed for driving to ball when calculating future ball pos")
	private static float				driveSpeed4BallSpeed		= 0.8f;
	@Configurable(comment = "Dist [mm] that the bot should always keep to ball (plus radius)", speziType = EBotType.class, spezis = { "GRSIM" })
	private static float				securityDistance2Ball	= 100;
	@Configurable(comment = "Dist [mm] for preposition that the bot should keep to ball (plus radius)", speziType = EBotType.class, spezis = { "GRSIM" })
	private static float				preDistance2Ball			= 120;
	
	@Configurable(comment = "Dist [mm] tolerance when switching from turn to kick mode.", speziType = EBotType.class, spezis = { "GRSIM" })
	private static float				switchDistanceTol			= 50;
	
	@Configurable(comment = "If bot is within this distance behind the ball, it will arm and kick even tough ball is moving in its direction")
	private static float				behindBallDistTol			= 400;
	
	@Configurable(comment = "Max angle [rad] between velocity vector and ball2Bot vector for switching to kick mode")
	private static float				angleBallVelTol			= 0.2f;
	
	@Configurable(comment = "Which type of command should be send to the bot?", speziType = EBotType.class, spezis = { "GRSIM" })
	private static ECommandMode	cmdMode						= ECommandMode.VEL;
	
	@Configurable(comment = "Dist [mm] that the destination is set behind ball hit point")
	private static int				distBehindBallHitTarget	= 100;
	
	// ######### from construction #########
	private final DynamicPosition	receiver;
	private final EKickMode			kickMode;
	
	// ########## state variables ############
	private boolean					kickSent						= false;
	protected long						kickTimeStart				= 0;
	
	private IVector2					direction;
	private IVector2					initBallPos					= null;
	private IVector2					initBallVel					= null;
	
	private EState						state							= EState.TURN;
	
	private CSVExporter				csvExporter					= new CSVExporter("kick", "kick", true);
	
	
	private enum EState
	{
		TURN,
		KICK,
	}
	
	
	/**
	 */
	public enum EKickMode
	{
		/**  */
		PASS,
		/**  */
		MAX,
		/**  */
		POINT
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * If receiver is a vector, kick in this direction with max force,
	 * if receiver is a botId, pass to this bot with appropriate force
	 * 
	 * @param receiver
	 * @param kickMode
	 */
	public KickSkill(final DynamicPosition receiver, final EKickMode kickMode)
	{
		this(ESkillName.KICK, receiver, kickMode);
	}
	
	
	/**
	 * If receiver is a vector, kick in this direction with max force,
	 * if receiver is a botId, pass to this bot with appropriate force
	 * 
	 * @param skillName
	 * @param receiver
	 */
	protected KickSkill(final ESkillName skillName, final DynamicPosition receiver, final EKickMode kickMode)
	{
		super(skillName);
		this.receiver = receiver;
		this.kickMode = kickMode;
		startTimeout(runtimeTimeout);
		setCommandMode(cmdMode);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public List<ACommand> calcEntryActions(final List<ACommand> cmds)
	{
		if (getBot().getBotFeatures().get(EFeature.BARRIER) == EFeatureState.KAPUT)
		{
			kickDelay = brokenKickDelay;
		} else
		{
			kickDelay = workingKickDelay;
		}
		
		initBallPos = getWorldFrame().getBall().getPos();
		initBallVel = getWorldFrame().getBall().getVel();
		
		return cmds;
	}
	
	
	@Override
	public void doCalcActions(final List<ACommand> cmds)
	{
		// arm or force kicker
		if ((kickTimeStart != 0) && !kickSent
				&& (TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - kickTimeStart) > (kickDelay * 1000)))
		{
			generateKickCmd(cmds);
			kickSent = true;
		}
		
		// movement
		updateDirection();
		calcDestAndOrient(cmds);
		
		if (exportMoveData)
		{
			List<Float> exportValues = new ArrayList<Float>();
			exportValues.add((float) System.nanoTime());
			exportValues.add(getDestination().x());
			exportValues.add(getDestination().y());
			exportValues.add(getOrientation());
			exportValues.add(getPos().x());
			exportValues.add(getPos().y());
			exportValues.add(getWorldFrame().getBall().getPos().x());
			exportValues.add(getWorldFrame().getBall().getPos().y());
			csvExporter.addValues(exportValues);
		}
		
		// check for completion
		if (getWorldFrame().getBall().getVel().getLength2() < 0.1)
		{
			initBallVel = AVector2.ZERO_VECTOR;
			initBallPos = getWorldFrame().getBall().getPos();
		}
		
		if ((initBallVel.getLength2() < 0.01)
				&& !initBallPos.equals(getWorldFrame().getBall().getPos(), ballToInitPosTol))
		{
			log.debug("Completed due to ball did not move initially and is not at initial pos anymore. Init vel: "
					+ initBallVel.getLength2());
			complete();
		}
		
		if ((initBallVel.getLength2() > 0.1)
				&& (getWorldFrame().getBall().getVel().getLength2() > (initBallVel.getLength2() + ballVelHigherTol)))
		{
			log.debug("Completed due to ball moving faster than initially: "
					+ getWorldFrame().getBall().getVel().getLength2() + ">" + initBallVel.getLength2() + "+"
					+ ballVelHigherTol);
			complete();
		}
	}
	
	
	@Override
	public List<ACommand> calcExitActions(final List<ACommand> cmds)
	{
		getDevices().allOff(cmds);
		if (exportMoveData)
		{
			csvExporter.close();
		}
		return cmds;
	}
	
	
	protected void generateKickCmd(final List<ACommand> cmds)
	{
		float kickLength = receiver.subtractNew(getWorldFrame().getBall().getPos()).getLength2();
		EKickDevice device = EKickDevice.STRAIGHT;
		EKickerMode mode = EKickerMode.ARM;
		float duration = TigerDevices.getStraightKickMaxDuration();
		
		if (getBot().getBotFeatures().get(EFeature.BARRIER) == EFeatureState.KAPUT)
		{
			mode = EKickerMode.FORCE;
			startTimeout(200);
		}
		
		if (getBot().getBotFeatures().get(EFeature.STRAIGHT_KICKER) == EFeatureState.WORKING)
		{
			duration = calcDuration(kickLength);
			
		} else if (getBot().getBotFeatures().get(EFeature.CHIP_KICKER) == EFeatureState.WORKING)
		{
			// kicker is broken, lets chip with 1/2 length :)
			ChipParams chipValues = TigerDevices.calcChipFastParams(kickLength);
			duration = chipValues.getDuration();
			device = EKickDevice.CHIP;
		} else
		{
			log.warn("Kicker of bot " + getBot().getBotID() + " is configured broken!");
			complete();
		}
		
		getDevices().kickGeneral(cmds, mode, device, duration, 0);
	}
	
	
	private void updateDirection()
	{
		receiver.update(getWorldFrame());
		IVector2 receiverTarget = receiver;
		if (receiver.getTrackedId().isBot())
		{
			TrackedTigerBot bot = getWorldFrame().getBot((BotID) receiver.getTrackedId());
			receiverTarget = AiMath.getBotKickerPos(bot.getPos(), bot.getAngle());
		}
		direction = receiverTarget.subtractNew(getWorldFrame().getBall().getPos()).normalize();
	}
	
	
	private void calcDestAndOrient(final List<ACommand> cmds)
	{
		setOrientation(getDirection().getAngle());
		
		IVector2 ballPos = getWorldFrame().getBall().getPos();
		IVector2 ball2Bot = getPos().subtractNew(getWorldFrame().getBall().getPos());
		IVector2 ballVel = getWorldFrame().getBall().getVel();
		if (ballVel.getLength2() > 0.1f)
		{
			float angleDiff = Math.abs(AngleMath.getShortestRotation(ball2Bot.getAngle(), ballVel.getAngle()));
			if ((angleDiff > angleBallVelTol) || (ball2Bot.getLength2() > behindBallDistTol))
			{
				// get future ballPos
				float time2Ball = GeoMath.distancePP(getPos(), getWorldFrame().getBall().getPos())
						/ (driveSpeed4BallSpeed * 1000);
				ballPos = getWorldFrame().getBall().getPosAt(time2Ball);
			}
			if ((angleDiff < angleBallVelTol))
			{
				// ball is moving in our direction, arm!
				float kickLength = receiver.subtractNew(getWorldFrame().getBall().getPos()).getLength2();
				getDevices().kickGeneral(cmds, EKickerMode.ARM, EKickDevice.STRAIGHT, calcDuration(kickLength), 0);
			}
		}
		
		float preDist2Ball = preDistance2Ball + AIConfig.getGeometry().getBotRadius()
				+ AIConfig.getGeometry().getBallRadius();
		float secDist2Ball = securityDistance2Ball + AIConfig.getGeometry().getBotRadius()
				+ AIConfig.getGeometry().getBallRadius();
		IVector2 preDest = ballPos.addNew(getDirection().scaleToNew(
				-preDist2Ball));
		
		switch (state)
		{
			case TURN:
				// check if ball is in our way
				if (GeoMath.p2pVisibilityBall(getWorldFrame(), getPos(), preDest, secDist2Ball))
				{
					setDestination(preDest);
				} else
				{
					// set destination secDist away from ball
					IVector2 lp = GeoMath.leadPointOnLine(ballPos, preDest, getPos());
					// +20 to make sure we do not toggle between the two if-else branches too much
					setDestination(GeoMath.stepAlongLine(ballPos, lp, secDist2Ball + 20));
				}
				
				float dist2Dest = GeoMath.distancePP(getPos(), preDest);
				if (dist2Dest < switchDistanceTol)
				{
					state = EState.KICK;
				}
				break;
			case KICK:
				if (kickTimeStart == 0)
				{
					// start time for kick
					kickTimeStart = System.nanoTime();
				}
				
				// postDest == point behind ball, so that bot would theoretically touch the ball
				IVector2 postDest = ballPos.subtractNew(getDirection().scaleToNew(
						AIConfig.getGeometry().getBotCenterToDribblerDist()));
				if (GeoMath.distancePP(postDest, getPos()) > 20)
				{
					// make sure we hit the ball correctly in the middle of the kicker,
					// if we are not directly behind the ball
					setDestination(GeoMath.stepAlongLine(postDest, getPos(), -distBehindBallHitTarget));
				} else
				{
					setDestination(ballPos.addNew(getDirection().scaleToNew(distBehindBallHitTarget)));
				}
				
				
				break;
			default:
				throw new IllegalStateException("Should not be here!");
		}
	}
	
	
	protected int calcDuration(final float length)
	{
		int duration;
		switch (kickMode)
		{
			case MAX:
				duration = TigerDevices.getStraightKickMaxDuration();
				break;
			case PASS:
				duration = (int) TigerDevices.calcStraightDuration(length, passEndVel);
				break;
			case POINT:
				duration = (int) TigerDevices.calcStraightDuration(length, 0);
				break;
			default:
				throw new IllegalStateException();
		}
		
		return duration;
	}
	
	
	/**
	 * @return the receiver
	 */
	public final DynamicPosition getReceiver()
	{
		return receiver;
	}
	
	
	/**
	 * @return the direction
	 */
	protected final IVector2 getDirection()
	{
		return direction;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
