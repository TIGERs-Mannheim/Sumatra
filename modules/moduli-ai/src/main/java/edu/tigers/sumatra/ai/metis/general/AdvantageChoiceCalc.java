/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import java.util.Optional;

import edu.tigers.sumatra.SslGameControllerTeam.TeamToController.AdvantageResponse;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.metis.offense.action.moves.OffensiveAction;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.referee.data.ProposedGameEvent;
import edu.tigers.sumatra.referee.gameevent.AttackerTouchedOpponentInDefenseArea;
import edu.tigers.sumatra.referee.gameevent.BotCrashUnique;
import edu.tigers.sumatra.referee.gameevent.BotPushedBot;
import edu.tigers.sumatra.referee.gameevent.EGameEvent;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;


public class AdvantageChoiceCalc extends ACalculator
{
	@Override
	public void doCalc()
	{
		AdvantageResponse advantage = AdvantageResponse.STOP;

		final boolean ballInTheirHalf = getWFrame().getBall().getPos().x() > 0.0;
		final Optional<IGameEvent> foul = proposedGameEvent();
		final boolean newPositionMoreFavourable = foul.map(this::isNewPositionMoreFavourable).orElse(false);
		if ((ballInTheirHalf && !newPositionMoreFavourable) || decentChanceToScoreGoal())
		{
			advantage = AdvantageResponse.CONTINUE;
		}

		int y = getAiFrame().getTeamColor() == ETeamColor.YELLOW ? 200 : 215;
		getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.AI_ADVANTAGE_CHOICE).add(
				new DrawableBorderText(Vector2f.fromXY(10, y), "Advantage Choice: " + advantage,
						getAiFrame().getTeamColor().getColor()));

		getNewTacticalField().setAdvantageChoice(advantage);
	}


	private boolean decentChanceToScoreGoal()
	{
		final boolean ballOurs = getNewTacticalField().getBallPossession().getEBallPossession()
				.equals(EBallPossession.WE);
		final boolean ballInTheirHalf = getWFrame().getBall().getPos().x() > 0.0;

		if (ballOurs && ballInTheirHalf)
		{
			// Is there a decent chance to hit the goal?
			BotID botID = getNewTacticalField().getBallPossession().getTigersId();
			double score = getNewTacticalField().getBestGoalKickTarget().map(IRatedTarget::getScore).orElse(0.0);

			final OffensiveAction offensiveAction = getNewTacticalField().getOffensiveActions().get(botID);
			return score >= 0.1 || (offensiveAction != null && offensiveAction.getViability() > 0.2);
		}
		return false;
	}


	private Optional<IGameEvent> proposedGameEvent()
	{
		return getAiFrame().getRefereeMsg().getProposedGameEvents().stream()
				.map(ProposedGameEvent::getGameEvent)
				.filter(ge -> ge.getType() == EGameEvent.BOT_CRASH_UNIQUE
						|| ge.getType() == EGameEvent.BOT_PUSHED_BOT
						|| ge.getType() == EGameEvent.ATTACKER_TOUCHED_OPPONENT_IN_DEFENSE_AREA)
				.findFirst();
	}


	private boolean isNewPositionMoreFavourable(final IGameEvent foul)
	{
		IVector2 ballPlacementPosition = getPlacementPosition(foul);
		IVector2 theirGoalCenter = Geometry.getGoalTheir().getCenter();
		final double distanceToPlacementPos = theirGoalCenter.distanceTo(ballPlacementPosition);
		final double distanceToBall = theirGoalCenter.distanceTo(getWFrame().getBall().getPos());
		return distanceToPlacementPos < distanceToBall;
	}


	private IVector2 getPlacementPosition(final IGameEvent foul)
	{
		if (foul instanceof BotCrashUnique)
		{
			return ((BotCrashUnique) foul).getLocation();
		} else if (foul instanceof BotPushedBot)
		{
			return ((BotPushedBot) foul).getLocation();
		} else if (foul instanceof AttackerTouchedOpponentInDefenseArea)
		{
			return ((AttackerTouchedOpponentInDefenseArea) foul).getLocation();
		} else
		{
			throw new IllegalStateException("Unmapped game event: " + foul);
		}
	}
}
