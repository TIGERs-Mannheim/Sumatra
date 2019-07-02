/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper.states;

import java.util.Optional;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.keeper.EKeeperState;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.support.passtarget.IPassTarget;
import edu.tigers.sumatra.ai.metis.support.passtarget.PassTarget;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.skillsystem.skills.RunUpChipSkill;
import edu.tigers.sumatra.skillsystem.skills.SingleTouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * If the ball is near the penalty area, the keeper should chip it to the best pass target 3000mm away
 *
 * @author ChrisC
 */
public class ChipFastState extends AKeeperState
{
	private IPassTarget pTarget = null;
	private DynamicPosition target;
	private final TimestampTimer timeoutTimer = new TimestampTimer(10);
	
	
	/**
	 * @param parent the parent role
	 */
	public ChipFastState(final KeeperRole parent)
	{
		super(parent, EKeeperState.CHIP_FAST);
	}
	
	
	@Override
	public void doEntryActions()
	{
		final IVector2 ballPos = getWFrame().getBall().getPos();
		final AMoveSkill skill;
		IVector2 chippingPosition = LineMath.stepAlongLine(ballPos, getPos(), -3000);
		if (ballPos.x() <= getPos().x())
		{
			chippingPosition = ballPos.addNew(Vector2.fromX(4000));
		}
		if (isBallDangerous())
		{
			target = new DynamicPosition(chippingPosition, 0.6);
			TouchKickSkill kickSkill = new TouchKickSkill(target,
					KickParams.chip(getKickSpeed(chippingPosition)));
			skill = kickSkill;
		} else
		{
			IVector2 fallBack = getAiFrame().getTacticalField().getChipKickTarget();
			chippingPosition = findBestPassTargetForKeeper().orElse(fallBack);
			target = new DynamicPosition(chippingPosition);
			skill = KeeperRole.isUseRunUpChipSkill() ? new RunUpChipSkill(target, getKickSpeed(chippingPosition))
					: new SingleTouchKickSkill(target, KickParams.chip(getKickSpeed(chippingPosition)));
		}
		skill.getMoveCon().setGoalPostObstacle(true);
		skill.getMoveCon().setPenaltyAreaAllowedOur(true);
		setNewSkill(skill);
		timeoutTimer.reset();
		timeoutTimer.update(getWFrame().getTimestamp());
	}
	
	
	private double getKickSpeed(final IVector2 chippingPosition)
	{
		double distance = chippingPosition.distanceTo(getWFrame().getBall().getPos());
		final double maxChipSpeed = getRole().getBot().getRobotInfo().getBotParams().getKickerSpecs()
				.getMaxAbsoluteChipVelocity();
		return OffensiveMath.passSpeedChip(distance, maxChipSpeed);
	}
	
	
	@Override
	public void doUpdate()
	{
		if (!isBallDangerous() && isKeeperFarFromBall() && !timeoutTimer.isTimeUp(getWFrame().getTimestamp()))
		{
			ITrackedBot fallBackReceiver = getAiFrame().getTacticalField().getChipKickTargetBot();
			IVector2 fallBack = getAiFrame().getTacticalField().getChipKickTarget();
			pTarget = fallBackReceiver != null ? new PassTarget(new DynamicPosition(fallBack), fallBackReceiver.getBotId()) : null;
			IVector2 chippingPosition = findBestPassTargetForKeeper().orElse(fallBack);
			target.setPos(chippingPosition);
			DrawableLine line = new DrawableLine(Line.fromPoints(getWFrame().getBall().getPos(), target.getPos()));
			getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.AI_KEEPER).add(line);
		}
		if (pTarget != null)
		{
			getAiFrame().getTacticalField().getAiInfoForNextFrame().announcePassingTo(pTarget);
		}
	}
	
	
	private Optional<IVector2> findBestPassTargetForKeeper()
	{
		Optional<IVector2> chippingPosition = Optional.empty();
		for (IPassTarget passTarget : getAiFrame().getTacticalField().getRatedPassTargetsRanked())
		{
			if (isPassTargetBehindPassCircle(passTarget.getPos()))
			{
				chippingPosition = Optional.of(passTarget.getPos());
				pTarget = passTarget;
				break;
				
			}
		}
		return chippingPosition;
	}
	
	
	private boolean isKeeperFarFromBall()
	{
		return getPos().distanceTo(getWFrame().getBall().getPos()) > Geometry.getBallRadius() + Geometry.getBotRadius()
				+ 10;
	}
	
	
	private boolean isBallDangerous()
	{
		boolean isEnemyCloseToBall = getAiFrame().getTacticalField().getEnemyClosestToBall().getDist() < KeeperRole
				.getMinFOEBotDistToChill();
		boolean isBallInPE = Geometry.getPenaltyAreaOur().isPointInShape(getWFrame().getBall().getPos());
		return isEnemyCloseToBall && !isBallInPE;
	}
	
	
	private boolean isPassTargetBehindPassCircle(IVector2 pos)
	{
		
		ICircle passCircle = Circle.createCircle(Geometry.getGoalOur().getCenter(), KeeperRole.getPassCircleRadius());
		if (Geometry.getPenaltyAreaOur().isPointInShape(getWFrame().getBall().getPos()))
		{
			getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.AI_KEEPER)
					.add(new DrawableCircle(passCircle));
		}
		return !passCircle.isPointInShape(pos);
	}
	
}