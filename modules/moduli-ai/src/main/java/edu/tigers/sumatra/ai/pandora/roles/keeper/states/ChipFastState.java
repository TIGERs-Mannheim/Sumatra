/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper.states;

import java.util.Optional;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.keeper.EKeeperState;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ai.metis.support.PassTarget;
import edu.tigers.sumatra.ai.metis.targetrater.PassInterceptionRater;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.skillsystem.skills.RunUpChipSkill;
import edu.tigers.sumatra.skillsystem.skills.SingleTouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.ITrackedObject;


/**
 * If the ball is near the penalty area, the keeper should chip it to the best pass target 3000mm away
 *
 * @author ChrisC
 */
public class ChipFastState extends AKeeperState
{
	private IPassTarget pTarget = null;
	private DynamicPosition target;
	
	
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
		
		IVector2 chippingPosition = LineMath.stepAlongLine(getPos(), ballPos, 3000);
		if (ballPos.x() <= getPos().x())
		{
			chippingPosition = Geometry.getCenter();
		}
		final AMoveSkill skill;
		if (isBallDangerous())
		{
			target = new DynamicPosition(chippingPosition, 0.6);
			TouchKickSkill kickSkill = new TouchKickSkill(target,
					KickParams.chip(getKickSpeed(chippingPosition)));
			skill = kickSkill;
		} else if (KeeperRole.isChipFarToPassTarget())
		{
			Optional<ITrackedBot> fallBackReceiver = getWFrame().getTigerBotsVisible().values().stream()
					.filter(bot -> isPassTargetBehindPassLine(bot.getPos()))
					.min((a, b) -> (int) (a.getPos().x() - b.getPos().x()));
			IVector2 fallBack = fallBackReceiver.map(ITrackedObject::getPos).orElseGet(Geometry::getCenter);
			
			chippingPosition = findBestPassTargetForKeeper(ballPos).orElse(fallBack);
			target = new DynamicPosition(chippingPosition);
			skill = new RunUpChipSkill(target, getKickSpeed(chippingPosition));
		} else
		{
			// chippingPosition
			chippingPosition = getAiFrame().getTacticalField().getChipKickTarget();
			target = new DynamicPosition(chippingPosition);
			skill = new SingleTouchKickSkill(target,
					KickParams.chip(getKickSpeed(chippingPosition)));
			if (KeeperRole.isPassTargetSet())
			{
				ITrackedBot bot = getAiFrame().getTacticalField().getChipKickTargetBot();
				IVector2 newKickerPos = LineMath.stepAlongLine(ballPos, chippingPosition,
						chippingPosition.distanceTo(ballPos) + Geometry.getBotRadius());
				pTarget = bot != null ? new PassTarget(newKickerPos, bot.getBotId()) : null;
			}
		}
		skill.getMoveCon().setGoalPostObstacle(true);
		skill.getMoveCon().setPenaltyAreaAllowedOur(true);
		setNewSkill(skill);
	}
	
	
	private double getKickSpeed(final IVector2 chippingPosition)
	{
		double distance = chippingPosition.distanceTo(getWFrame().getBall().getPos());
		return OffensiveMath.passSpeedChip(distance);
	}
	
	
	@Override
	public void doUpdate()
	{
		if (!isBallDangerous() && !KeeperRole.isChipFarToPassTarget() && !isKeeperCloseToBall())
		{
			IVector2 chippingPos = getAiFrame().getTacticalField().getChipKickTarget();
			target.update(new DynamicPosition(chippingPos));
			if (KeeperRole.isPassTargetSet())
			{
				ITrackedBot bot = getAiFrame().getTacticalField().getChipKickTargetBot();
				IVector2 newKickerPos = LineMath.stepAlongLine(getWFrame().getBall().getPos(), chippingPos,
						chippingPos.distanceTo(getWFrame().getBall().getPos()) + Geometry.getBotRadius());
				pTarget = bot != null ? new PassTarget(newKickerPos, bot.getBotId()) : null;
			}
		} else if (!isBallDangerous() && !isKeeperCloseToBall())
		{
			Optional<ITrackedBot> fallBackReceiver = getWFrame().getTigerBotsVisible().values().stream()
					.filter(bot -> isPassTargetBehindPassLine(bot.getPos()))
					.min((a, b) -> (int) (a.getPos().x() - b.getPos().x()));
			IVector2 fallBack = fallBackReceiver.map(ITrackedObject::getPos).orElseGet(Geometry::getCenter);
			
			IVector2 chippingPosition = findBestPassTargetForKeeper(getWFrame().getBall().getPos()).orElse(fallBack);
			target = new DynamicPosition(chippingPosition);
			
			DrawableLine line = new DrawableLine(Line.fromPoints(getWFrame().getBall().getPos(), target));
			getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.AI_KEEPER).add(line);
			fallBackReceiver.ifPresent(iTrackedBot -> pTarget = new PassTarget(target, iTrackedBot.getBotId()));
		}
		if (pTarget != null)
		{
			getAiFrame().getTacticalField().getAiInfoForNextFrame().announcePassingTo(pTarget);
		}
	}
	
	
	private Optional<IVector2> findBestPassTargetForKeeper(IVector2 ballPos)
	{
		Optional<IVector2> chippingPosition = Optional.empty();
		for (IPassTarget passTarget : getAiFrame().getTacticalField().getPassTargetsRanked())
		{
			if (isPassTargetBehindPassLine(passTarget.getKickerPos()))
			{
				double chipScore = 1 - PassInterceptionRater.getChipScoreForLineSegment(getWFrame().getBall(),
						getWFrame().getFoeBots().values(), ballPos, passTarget.getKickerPos(), 0);
				if (chipScore < KeeperRole.getMaxChipScore())
				{
					chippingPosition = Optional.of(passTarget.getKickerPos());
					pTarget = passTarget;
					break;
				}
				
			}
		}
		return chippingPosition;
	}
	
	
	private boolean isKeeperCloseToBall()
	{
		return getPos().distanceTo(getWFrame().getBall().getPos()) < Geometry.getBallRadius() + Geometry.getBotRadius()
				+ 10;
	}
	
	
	private boolean isBallDangerous()
	{
		boolean isChipOK = !getAiFrame().getTacticalField().isBotInterferingKeeperChip();
		boolean isEnemyCloseToBall = getAiFrame().getTacticalField().getEnemyClosestToBall().getDist() < KeeperRole
				.getMinFOEBotDistToChill();
		boolean isBallInPE = Geometry.getPenaltyAreaOur().isPointInShape(getWFrame().getBall().getPos());
		return isChipOK && isEnemyCloseToBall && !isBallInPE;
	}
	
	
	private boolean isPassTargetBehindPassLine(IVector2 pos)
	{
		return pos.x() >= KeeperRole.getMinXPosOfPossiblePassTarget();
	}
	
}