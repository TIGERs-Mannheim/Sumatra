

/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.states.kickstate;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.math.OffensiveMath;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.OffensiveRoleKickState;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.AKickSkill;
import edu.tigers.sumatra.skillsystem.skills.AKickSkill.EKickMode;
import edu.tigers.sumatra.skillsystem.skills.KickChillSkill;
import edu.tigers.sumatra.skillsystem.skills.KickNormalSkill;
import edu.tigers.sumatra.skillsystem.skills.RunUpChipSkill;
import edu.tigers.sumatra.wp.ball.prediction.IChipBallConsultant;
import edu.tigers.sumatra.wp.ball.trajectory.BallFactory;
import edu.tigers.sumatra.wp.data.DynamicPosition;

import java.awt.Color;


/**
 * @author MarkG
 */
public class NormalKickStateState extends AOffensiveRoleKickStateState
{
	
	private OffensiveAction currentAction = null;
	private AKickSkill kickSkill = null;
	private boolean finalStrategySet = false;
	private IVector2 finalStrategyInitBallPos = AVector2.ZERO_VECTOR;
	
	
	/**
	 * @param role the offensiveRole instance
	 */
	public NormalKickStateState(final OffensiveRoleKickState role)
	{
		super(role);
	}
	
	
	@Override
	public IVector2 getDestination()
	{
		return kickSkill.getMoveCon().getDestination();
	}
	
	
	@Override
	public void doEntryActions()
	{
		kickSkill = new KickNormalSkill(new DynamicPosition(Geometry.getGoalTheir().getCenter()));
		setNewSkill(kickSkill);
	}
	
	
	/**
	 * if bot is far away from the ball and strategy was set for 500ms, then the strategy should set
	 * again.
	 *
	 * @return
	 */
	private boolean isBotFarAway()
	{
		double traveledDistance = VectorMath.distancePP(finalStrategyInitBallPos,
				getWFrame().getBall().getPos());
		return traveledDistance > 700 || !isTimeForFinalStrategy();
	}
	
	
	@Override
	public void doUpdate()
	{
		handleGameState();
		
		// smarte cases here ?
		if (finalStrategySet && isBotFarAway())
		{
			// reset final Strategy
			finalStrategySet = false;
		}
		
		OffensiveAction potentialNewStrategy = getAiFrame().getTacticalField().getOffensiveActions().get(getBotID());
		String text;
		boolean keepStrategy = isKeepCurrentStrategy();
		if ((currentAction == null) || !finalStrategySet || !keepStrategy)
		{
			text = getDebugInformation(keepStrategy);
			currentAction = potentialNewStrategy;
			finalStrategyInitBallPos = getWFrame().getBall().getPos();
			finalStrategySet = isTimeForFinalStrategy();
		} else
		{
			text = "Dont Update";
		}
		DrawableBorderText dt = new DrawableBorderText(Vector2.fromXY(10, 90), text, Color.cyan);
		dt.setFontSize(12);
		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE).add(dt);
		
		IPassTarget passTarget = currentAction.getMoveAndTargetInformation().getPassTarget();
		updateSkill(passTarget);
		
		if (currentAction.getMoveAndTargetInformation().isReceiveActive())
		{
			triggerInnerEvent(EKickStateEvent.CATCH_BALL);
		} else if (OffensiveConstants.isEnableProtectionMode()
				&& isProtectionRequired(currentAction.getType())
				&& (Geometry.getGoalOur().getCenter().distanceTo(getWFrame().getBall().getPos()) > 5000))
		{
			triggerInnerEvent(EKickStateEvent.PROTECT_BALL);
		}
		
		visualizeActions();
	}
	
	
	private String getDebugInformation(final boolean keepStrategy)
	{
		String text = "Updating because: ";
		if (currentAction == null)
		{
			text += "currentA: null | ";
		}
		if (!finalStrategySet)
		{
			text += "!finalStrategySet | ";
		}
		if (!keepStrategy)
		{
			text += "!keepStrategy";
		}
		return text;
	}
	
	
	private void visualizeActions()
	{
		String str = "";
		if (currentAction != null)
		{
			str = currentAction.getType().name();
		}
		OffensiveAction upcomingAction = getAiFrame().getTacticalField().getOffensiveActions().get(getBotID());
		if (upcomingAction != null)
		{
			str += " (" + upcomingAction.getType().name() + ")";
		}
		DrawableAnnotation dAnno = new DrawableAnnotation(getPos(), str).setOffset(Vector2.fromX(-120));
		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE).add(dAnno);
	}
	
	
	private void useNormalKick()
	{
		if (kickSkill.getType().equals(ESkill.KICK_CHILL) || kickSkill.getType().equals(ESkill.RUN_UP_CHIP))
		{
			kickSkill = new KickNormalSkill(new DynamicPosition(Geometry.getGoalTheir().getCenter()));
			setNewSkill(kickSkill);
		}
	}
	
	
	private boolean isStandardCloseToOurFieldBorder()
	{
		GameState gameState = getAiFrame().getTacticalField().getGameState();
		return gameState.isStandardSituationForUs()
				&& (getWFrame().getBall().getPos().x() < -(Geometry.getFieldLength() / 2) + 500);
	}
	
	
	private void useChillKick()
	{
		if (kickSkill.getType().equals(ESkill.KICK_NORMAL))
		{
			if (isStandardCloseToOurFieldBorder())
			{
				kickSkill = new RunUpChipSkill(new DynamicPosition(Geometry.getGoalTheir().getCenter()), EKickMode.PASS);
			} else
			{
				kickSkill = new KickChillSkill(new DynamicPosition(Geometry.getGoalTheir().getCenter()));
			}
			setNewSkill(kickSkill);
		} else if (kickSkill.getType().equals(ESkill.KICK_CHILL) || kickSkill.getType().equals(ESkill.RUN_UP_CHIP))
		{
			// update ready 4 kick here
			kickSkill.setReadyForKick(
					getAiFrame().getTacticalField().getOffensiveActions().get(getBotID()).isRoleReadyToKick());
		}
	}
	
	
	/**
	 * @return an optional identifier for this state, defaults to the class name
	 */
	@Override
	public String getIdentifier()
	{
		return EKickStateState.NORMAL_KICK.name();
	}
	
	
	private boolean isTimeForFinalStrategy()
	{
		return VectorMath.distancePP(getPos(), getAiFrame().getWorldFrame().getBall().getPos()) < OffensiveConstants
				.getFinalKickStateDistance();
	}
	
	
	private void updateSkill(final IPassTarget passTarget)
	{
		// calc pass Speed
		IChipBallConsultant consultant = BallFactory.createChipConsultant();
		double passSpeedForChipDetection = consultant
				.getInitVelForDistAtTouchdown(getWFrame().getBall().getPos().distanceTo(passTarget.getKickerPos()), 4);
		boolean chip = OffensiveMath.isChipKickRequired(getWFrame(), getBotID(), passTarget, passSpeedForChipDetection,
				getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_IS_CHIP_NEEDED));
		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_ADDITIONAL)
				.add(new DrawableAnnotation(getPos(), chip ? "chip" : "straight", Color.orange)
						.setOffset(Vector2.fromX(150)));
		
		DynamicPosition dynTarget = new DynamicPosition(passTarget.getKickerPos());
		kickSkill.setReceiver(dynTarget);
		kickSkill.setKickMode(passTarget.getKickMode());
		
		if (chip)
		{
			kickSkill.setKickMode(EKickMode.PASS);
			kickSkill.setDevice(EKickerDevice.CHIP);
		} else
		{
			kickSkill.setDevice(EKickerDevice.STRAIGHT);
		}
		
		if (passTarget.getKickMode() == EKickMode.PASS)
		{
			if (!chip)
			{
				double timeToPassTarget = (passTarget.getTimeReached() - getWFrame().getTimestamp()) * 1e-9;
				double passSpeed = OffensiveMath.calcPassSpeedRedirect(timeToPassTarget, getBot().getBotKickerPos(),
						passTarget.getKickerPos(), Geometry.getGoalTheir().getCenter());
				kickSkill.setKickSpeed(passSpeed);
			}
			kickSkill.setReadyForKick(
					getAiFrame().getTacticalField().getOffensiveActions().get(getBotID()).isRoleReadyToKick());
		} else if (passTarget.getKickMode() == EKickMode.FIXED_SPEED)
		{
			kickSkill.setKickSpeed(2.3);
			kickSkill.setReadyForKick(true);
		} else
		{
			kickSkill.setReadyForKick(true);
		}
		
		if (finalStrategySet && currentAction.getType() == OffensiveAction.EOffensiveAction.PASS)
		{
			getAiFrame().getAICom().setPassTarget(passTarget);
		} else
		{
			getAiFrame().getAICom().setPassTarget(null);
		}
		visualizeTarget(passTarget.getKickerPos());
	}
	
	
	private boolean isKeepCurrentStrategy()
	{
		final double distToBallTolerance = 250;
		return getAiFrame().getGamestate().isStandardSituationForUs()
				&& finalStrategySet
				|| (getBot().getBotKickerPos().distanceTo(getWFrame().getBall().getPos())
						- Geometry.getBallRadius()) < distToBallTolerance;
	}
	
	
	private void handleGameState()
	{
		GameState state = getAiFrame().getTacticalField().getGameState();
		if (state.isStandardSituationForUs())
		{
			useChillKick();
		} else
		{
			useNormalKick();
		}
	}
}
