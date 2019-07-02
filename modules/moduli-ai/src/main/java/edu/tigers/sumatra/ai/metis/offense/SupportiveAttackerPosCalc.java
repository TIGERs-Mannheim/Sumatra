/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.defense.DefenseMath;
import edu.tigers.sumatra.ai.metis.redirector.ERecommendedReceiverAction;
import edu.tigers.sumatra.ai.metis.redirector.RedirectorDetectionInformation;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class SupportiveAttackerPosCalc extends ACalculator
{
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		switch (newTacticalField.getSkirmishInformation().getStrategy())
		{
			case FREE_BALL:
				newTacticalField
						.setSupportiveAttackerMovePos(
								newTacticalField.getSkirmishInformation().getSupportiveCircleCatchPos());
				break;
			default:
				newTacticalField
						.setSupportiveAttackerMovePos(calcMovePosition(newTacticalField, baseAiFrame.getWorldFrame()));
				break;
		}


		RedirectorDetectionInformation rInfo = newTacticalField.getRedirectorDetectionInformation();
		if (!rInfo.isFriendlyBotReceiving() || !rInfo.isEnemyReceiving())
		{
			return;
		}

		boolean additionalInterceptorNeeded = rInfo.getRecommendedAction() == ERecommendedReceiverAction.DISRUPT_ENEMY ||
				rInfo.getRecommendedAction() == ERecommendedReceiverAction.DOUBLE_ATTACKER;
		if (additionalInterceptorNeeded)
		{
			IVector2 enemy = rInfo.getEnemyReceiverPos();
			IVector2 dir = Geometry.getGoalOur().getCenter().subtractNew(enemy);
			IVector2 targetPos = enemy.addNew(dir.scaleToNew(Geometry.getBotRadius() * 2.5));
			newTacticalField.setSupportiveAttackerMovePos(targetPos);
		}
	}


	private IVector2 calcMovePosition(TacticalField newTacticalField, WorldFrame worldFrame)
	{
		IVector2 ballPos = worldFrame.getBall().getPos();
		ITrackedBot bot = newTacticalField.getEnemyClosestToBall().getBot();

		final IVector2 dir;
		if (bot == null)
		{
			dir = Vector2f.fromX(-1);
		} else if (bot.getPos().x() > ballPos.x())
		{
			dir = ballPos.subtractNew(bot.getPos()).normalizeNew();
		} else
		{
			IVector2 goal = DefenseMath.getBisectionGoal(ballPos);
			dir = goal.subtractNew(ballPos).normalizeNew();
		}
		return ballPos.addNew(dir.multiplyNew(RuleConstraints.getStopRadius()
				+ (Geometry.getBotRadius() * 2)));
	}
}
