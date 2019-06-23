/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 26, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.offense.states;

import java.awt.Color;
import java.util.List;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.OffensiveStrategy.EOffensiveStrategy;
import edu.tigers.sumatra.ai.data.SpecialMoveCommand;
import edu.tigers.sumatra.ai.data.math.OffensiveMath;
import edu.tigers.sumatra.ai.data.math.RedirectMath;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.pandora.roles.offense.OffensiveRole;
import edu.tigers.sumatra.ai.sisyphus.TrajectoryGenerator;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableText;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.ValuePoint;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.paramoptimizer.redirect.RedirectParamCalc;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.AReceiveSkill;
import edu.tigers.sumatra.skillsystem.skills.ReceiverSkill;
import edu.tigers.sumatra.skillsystem.skills.ReceiverSkill.EReceiverMode;
import edu.tigers.sumatra.skillsystem.skills.RedirectSkill;
import edu.tigers.sumatra.statemachine.IRoleState;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveRoleRedirectCatchSpecialMovementState extends AOffensiveRoleState implements IRoleState
{
	
	// -------------------------------------------------------------------------- //
	// --- variables and constants ---------------------------------------------- //
	// -------------------------------------------------------------------------- //
	
	@Configurable(comment = "Distance in mm for the search steps")
	private static double			redirectSearchStepDistance	= 100;
	
	private static final double	STATE_SWITCH_MAX_BALL_DIST	= 300;
	
	
	/**
	 * @param role
	 */
	public OffensiveRoleRedirectCatchSpecialMovementState(final OffensiveRole role)
	{
		super(role);
	}
	
	private enum ERedirectCatchSpecialMoveState
	{
		/**  */
		SPECIAL_MOVE,
		/**  */
		DEFAULT;
	}
	
	// ----------------------------------------------------------------------- //
	// -------------------- functions ---------------------------------------- //
	// ----------------------------------------------------------------------- //
	
	
	private SpecialMoveCommand					command			= null;
	private AMoveToSkill							move				= null;
	private IVector2								movePos			= null;
	private ERedirectCatchSpecialMoveState	state				= null;
	private int										idx				= 0;
	private AReceiveSkill						redirectSkill	= null;
	
	private IVector2								initialReceivePosition;
	
	// private long initTime = 0;
	
	
	@Override
	public void doExitActions()
	{
		getAiFrame().getAICom().setResponded(false);
	}
	
	
	@Override
	public void doEntryActions()
	{
		move = AMoveToSkill.createMoveToSkill();
		idx = getAiFrame().getPrevFrame().getAICom().getSpecialMoveCounter();
		
		if (idx < getAiFrame().getTacticalField().getOffensiveStrategy().getSpecialMoveCommands().size())
		{
			command = getAiFrame().getTacticalField().getOffensiveStrategy().getSpecialMoveCommands()
					.get(idx);
			movePos = command.getMovePosition().get(0);
			setNewSkill(move);
			getAiFrame().getPrevFrame().getAICom().setSpecialMoveCounter(idx + 1);
			state = ERedirectCatchSpecialMoveState.SPECIAL_MOVE;
		} else
		{
			IVector2 target = calcRedirectTarget();
			if (OffensiveMath.isBallRedirectReasonable(getWFrame(), getPos(), target))
			{
				redirectSkill = new RedirectSkill(new DynamicPosition(target));
				setNewSkill(redirectSkill);
			} else
			{
				redirectSkill = new ReceiverSkill(EReceiverMode.KEEP_DRIBBLING);
				setNewSkill(redirectSkill);
			}
			state = ERedirectCatchSpecialMoveState.DEFAULT;
			initialReceivePosition = getPos();
		}
	}
	
	
	@Override
	public void doUpdate()
	{
		switch (state)
		{
			case DEFAULT:
				if (getCurrentSkill().getType() == ESkill.RECEIVER)
				{
					IVector2 target = calcRedirectTarget();
					DynamicPosition dtarget = new DynamicPosition(target);
					if (OffensiveMath.isBallRedirectReasonable(getWFrame(), getPos(), dtarget))
					{
						if (GeoMath.distancePP(getPos(), getWFrame().getBall().getPos()) > STATE_SWITCH_MAX_BALL_DIST)
						{
							redirectSkill = new RedirectSkill(dtarget);
							setNewSkill(redirectSkill);
						}
					}
					drawShapes(getWFrame().getBall().getPos());
				} else
				{
					IVector2 target = calcRedirectTarget();
					DynamicPosition dtarget = new DynamicPosition(target);
					if (redirectSkill.getType() == ESkill.REDIRECT)
					{
						// ((RedirectSkill) redirectSkill)
						// .setDevice(calcIsChip(dtarget, null) ? EKickerDevice.CHIP : EKickerDevice.STRAIGHT);
						((RedirectSkill) redirectSkill).setTarget(dtarget);
					}
					if (!OffensiveMath.isBallRedirectReasonable(getWFrame(), getPos(), dtarget))
					{
						if (GeoMath.distancePP(getPos(), getWFrame().getBall().getPos()) > STATE_SWITCH_MAX_BALL_DIST)
						{
							redirectSkill = new ReceiverSkill(EReceiverMode.KEEP_DRIBBLING);
							setNewSkill(redirectSkill);
						}
					}
					drawShapes(dtarget);
				}
				if (OffensiveMath.getPotentialRedirectors(getWFrame(), getWFrame().getTigerBotsAvailable())
						.contains(getBotID()))
				{
					List<IDrawableShape> shapes = getAiFrame().getTacticalField().getDrawableShapes()
							.get(EShapesLayer.REDIRECT_SKILL);
					IVector2 betterPos = RedirectMath.calculateBetterPosition(getWFrame(), getBot(), initialReceivePosition,
							shapes);
					redirectSkill.setDesiredDestination(betterPos);
				} else
				{
					redirectSkill.setDesiredDestination(initialReceivePosition);
				}
				break;
			case SPECIAL_MOVE:
				if (idx < getAiFrame().getTacticalField().getOffensiveStrategy().getSpecialMoveCommands().size())
				{
					command = getAiFrame().getTacticalField().getOffensiveStrategy().getSpecialMoveCommands()
							.get(idx);
					movePos = command.getMovePosition().get(0);
					
					if (Geometry.getPenaltyAreaTheir()
							.isPointInShape(movePos, Geometry.getBotRadius() * 1.2))
					{
						movePos = new ValuePoint(Geometry.getPenaltyAreaTheir()
								.nearestPointOutside(movePos, Geometry.getBotRadius() * 1.2));
					} else if (Geometry.getPenaltyAreaOur()
							.isPointInShape(movePos, OffensiveConstants.getDistanceToPenaltyArea()))
					{
						movePos = new ValuePoint(Geometry.getPenaltyAreaOur()
								.nearestPointOutside(movePos, OffensiveConstants.getDistanceToPenaltyArea()));
					}
					
					IVector2 otarget = getAiFrame().getTacticalField().getBestDirectShootTarget();
					if (otarget == null)
					{
						otarget = Geometry.getGoalTheir().getGoalCenter();
					}
					IVector2 target = getAiFrame().getTacticalField()
							.getBestDirectShootTarget();
					double orientation = 0;
					if (OffensiveMath.isBallRedirectReasonable(getWFrame(), getPos(), otarget))
					{
						if (target == null)
						{
							target = Geometry.getGoalTheir().getGoalCenter();
						}
						IVector3 poss = RedirectParamCalc.forBot(getBot().getBot()).calcRedirectPose(getBot(), movePos,
								target.subtractNew(movePos)
										.getAngle(),
								getWFrame().getBall(),
								target,
								4.0f);
						orientation = poss.z();
					} else
					{
						orientation = getWFrame().getBall().getPos().subtractNew(getPos()).getAngle();
					}
					
					TrajectoryGenerator generator = new TrajectoryGenerator();
					double botTime = generator.generatePositionTrajectory(getBot(), movePos).getTotalTime()
							+ OffensiveConstants.getNeededTimeForPassReceivingBotOffset();
					
					double ballArrivalTime = command.getTimeUntilPassArrives();
					DrawableText dt = new DrawableText(movePos.addNew(new Vector2(300, 0)),
							"botTime: " + botTime, Color.red);
					getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dt);
					
					boolean forceStop = false;
					if ((ballArrivalTime > botTime) && OffensiveConstants.isEnableRedirectorStopMove())
					{
						movePos = getPos();
						forceStop = true; // forceStop = true, to activate timed passing
					}
					
					move.getMoveCon().updateTargetAngle(orientation);
					move.getMoveCon().updateDestination(movePos);
					
					DrawableLine dl = new DrawableLine(Line.newLine(getPos(), movePos), Color.blue);
					getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dl);
					
					if (((GeoMath.distancePP(getPos(), movePos) < 30) && !forceStop)
							|| (OffensiveMath.getBestRedirector(getWFrame(),
									getWFrame().tigerBotsAvailable) == getBotID()))
					{
						initialReceivePosition = movePos;
						redirectSkill = new RedirectSkill(new DynamicPosition(target));
						setNewSkill(redirectSkill);
						state = ERedirectCatchSpecialMoveState.DEFAULT;
					}
				} else
				{
					IVector2 target = getAiFrame().getTacticalField()
							.getBestDirectShootTarget();
					redirectSkill = new RedirectSkill(new DynamicPosition(target));
					setNewSkill(redirectSkill);
					initialReceivePosition = movePos;
					state = ERedirectCatchSpecialMoveState.DEFAULT;
				}
				break;
			default:
				log.error("This is impossible, call Mark!");
				break;
			
		}
	}
	
	
	@Override
	public Enum<? extends Enum<?>> getIdentifier()
	{
		return EOffensiveStrategy.REDIRECT_CATCH_SPECIAL_MOVE;
	}
	
	
	private IVector2 calcRedirectTarget()
	{
		IVector2 target = getAiFrame().getTacticalField().getBestDirectShootTarget();
		if (getAiFrame().getTacticalField().getBestDirectShotTargetsForTigerBots().containsKey(getBotID()))
		{
			target = getAiFrame().getTacticalField().getBestDirectShotTargetsForTigerBots().get(getBotID());
		}
		if (target == null)
		{
			target = Geometry.getGoalTheir().getGoalCenter();
		}
		return target;
	}
	
	
	private void drawShapes(final IVector2 target)
	{
		getAiFrame().getTacticalField()
				.getDrawableShapes()
				.get(EShapesLayer.OFFENSIVE)
				.add(
						new DrawableLine(Line.newLine(getPos(), target), Color.red));
		visualizeTarget(target);
	}
}
