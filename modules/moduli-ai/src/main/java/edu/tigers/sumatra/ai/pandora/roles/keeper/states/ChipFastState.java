/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper.states;

import java.util.Optional;

import edu.tigers.sumatra.ai.math.kick.PassInterceptionRater;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ai.metis.support.PassTarget;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.AKickSkill;
import edu.tigers.sumatra.skillsystem.skills.KickChillSkill;
import edu.tigers.sumatra.skillsystem.skills.KickNormalSkill;
import edu.tigers.sumatra.skillsystem.skills.RunUpChipSkill;
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
	private AKickSkill skill;
	private IPassTarget pTarget = null;
	
	
	/**
	 * @param parent the parent role
	 */
	public ChipFastState(final KeeperRole parent)
	{
		super(parent);
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
		if (isBallDangerous())
		{
			KickNormalSkill kickSkill = new KickNormalSkill(new DynamicPosition(chippingPosition));
			kickSkill.setPanic(true);
			skill = kickSkill;
		} else if (KeeperRole.isChipFarToPassTarget())
		{
			Optional<ITrackedBot> fallBackReceiver = getWFrame().getTigerBotsVisible().values().stream()
					.filter(bot -> isPassTargetBehindPassLine(bot.getPos()))
					.sorted((a, b) -> (int) (a.getPos().x() - b.getPos().x())).findFirst();
			IVector2 fallBack = fallBackReceiver.map(ITrackedObject::getPos).orElseGet(Geometry::getCenter);
			
			chippingPosition = findBestPassTargetForKeeper(ballPos).orElse(fallBack);
			skill = new RunUpChipSkill(new DynamicPosition(chippingPosition), AKickSkill.EKickMode.POINT);
			skill.setKickMode(AKickSkill.EKickMode.PASS);
		} else
		{
			// chippingPosition
			chippingPosition = getAiFrame().getTacticalField().getChipKickTarget();
			skill = new KickChillSkill(new DynamicPosition(chippingPosition));
			skill.setKickMode(AKickSkill.EKickMode.POINT);
			if (KeeperRole.isPassTargetSet())
			{
				ITrackedBot bot = getAiFrame().getTacticalField().getChipKickTargetBot();
				IVector2 newKickerPos = LineMath.stepAlongLine(ballPos, chippingPosition,
						chippingPosition.distanceTo(ballPos) + Geometry.getBotRadius());
				pTarget = bot != null ? new PassTarget(newKickerPos, bot.getBotId()) : null;
			}
		}
		skill.setDevice(EKickerDevice.CHIP);
		skill.getMoveCon().setGoalPostObstacle(true);
		skill.getMoveCon().setPenaltyAreaAllowedOur(true);
		setNewSkill(skill);
	}
	
	
	@Override
	public void doUpdate()
	{
		if (!isBallDangerous() && !KeeperRole.isChipFarToPassTarget() && !isKeeperCloseToBall())
		{
			IVector2 chippingPos = getAiFrame().getTacticalField().getChipKickTarget();
			skill.setReceiver(new DynamicPosition(chippingPos));
			if (KeeperRole.isPassTargetSet())
			{
				ITrackedBot bot = getAiFrame().getTacticalField().getChipKickTargetBot();
				IVector2 newKickerPos = LineMath.stepAlongLine(getWFrame().getBall().getPos(), chippingPos,
						chippingPos.distanceTo(getWFrame().getBall().getPos()) + Geometry.getBotRadius());
				pTarget = bot != null ? new PassTarget(newKickerPos, bot.getBotId()) : null;
			}
		}
		if (pTarget != null)
		{
			getAiFrame().getAICom().setPassTarget(pTarget);
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