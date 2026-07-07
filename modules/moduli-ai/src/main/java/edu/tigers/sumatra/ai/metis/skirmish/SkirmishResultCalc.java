package edu.tigers.sumatra.ai.metis.skirmish;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.states.EAttackerState;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.EFontSize;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Objects;
import java.util.function.Supplier;


@RequiredArgsConstructor
public class SkirmishResultCalc extends ACalculator
{
	private enum ESkirmishTrackingState
	{
		NONE,
		OPPONENT_CONTROL_STARTED,
	}

	private final Supplier<BotDistance> tigerClosestToBall;
	private final Supplier<BotDistance> opponentClosestToBall;
	private final Supplier<BallPossession> ballPossession;
	private final Supplier<ESkirmishCategory> situation;

	private TimestampTimer opponentHasStableBallControlTimer = new TimestampTimer(0.1);
	private TimestampTimer tigerHasStableBallControlTimer = new TimestampTimer(0.05);

	private TimestampTimer tigerConqueredBallTimer = new TimestampTimer(0.25);

	private boolean opponentHadContactInLastFrame = false;

	private ESkirmishTrackingState skirmishTrackingState = ESkirmishTrackingState.NONE;

	@Getter
	private EBallContestStrategy strategyUsed = EBallContestStrategy.DRIBBLE;

	private int successFreeSkirmish = 0;
	private int failureFreeSkirmish = 0;
	private int successDribble = 0;
	private int failureDribble = 0;


	@Override
	public void doCalc()
	{
		if (opponentClosestToBall.get().getBotId().isUninitializedID()
				|| tigerClosestToBall.get().getBotId().isUninitializedID())
		{
			// if there are no bots, this call serves no purpose
			return;
		}

		ITrackedBot tiger = getWFrame().getTiger(tigerClosestToBall.get().getBotId());
		ITrackedBot opponent = getWFrame().getOpponentBot(opponentClosestToBall.get().getBotId());

		IVector2 ballPos = getBall().getPos();
		IVector2 opponentKickerPos = opponent.getBotKickerPos();

		if ((ballPossession.get().getEBallPossession() == EBallPossession.THEY
				|| ballPossession.get().getEBallPossession() == EBallPossession.BOTH)
				&& opponentKickerPos.distanceTo(ballPos) < Geometry.getBotRadius() / 2.0)
		{
			opponentHasStableBallControlTimer.update(getWFrame().getTimestamp());
		} else
		{
			opponentHasStableBallControlTimer.reset();
		}

		if ((ballPossession.get().getEBallPossession() == EBallPossession.WE
				|| ballPossession.get().getEBallPossession() == EBallPossession.BOTH)
				&& tiger.getBallContact().hasContact())
		{
			tigerHasStableBallControlTimer.update(getWFrame().getTimestamp());
		} else
		{
			tigerHasStableBallControlTimer.reset();
		}

		boolean tigerHasContact = false;
		if (tigerHasStableBallControlTimer.isTimeUp(getWFrame().getTimestamp()))
		{
			// tiger has stable control
			tigerHasContact = true;
		}

		boolean opponentHasContestedContact = false;
		if (opponentHasStableBallControlTimer.isTimeUp(getWFrame().getTimestamp())
				&& situation.get() == ESkirmishCategory.CONTESTED_CONTROL)
		{
			// opponent has stable control
			opponentHasContestedContact = true;
		}

		if (!opponentHadContactInLastFrame && opponentHasContestedContact)
		{
			// opponent has control situation started
			skirmishTrackingState = ESkirmishTrackingState.OPPONENT_CONTROL_STARTED;
		}

		if (skirmishTrackingState == ESkirmishTrackingState.OPPONENT_CONTROL_STARTED
				&& !opponentHasContestedContact)
		{
			// opponent has lost the ball. So we start tracking the result of the opponent contact phase
			tigerConqueredBallTimer.update(getWFrame().getTimestamp());
		}

		getShapes(EAiShapesLayer.AI_SKIRMISH_RESULT)
				.add(new DrawableAnnotation(
						ballPos, String.format("contested Ball: %b", opponentHasContestedContact), Color.BLACK));
		getShapes(EAiShapesLayer.AI_SKIRMISH_RESULT)
				.add(new DrawableAnnotation(
						ballPos.subtractNew(Vector2.fromY(-40)), String.format("tracking state: %s", skirmishTrackingState.toString()), Color.BLACK));
		getShapes(EAiShapesLayer.AI_SKIRMISH_RESULT)
				.add(new DrawableAnnotation(
						ballPos.subtractNew(Vector2.fromY(-80)), String.format("strategy: %s", strategyUsed.toString()),
						Color.BLACK
				));
		if (tiger.getBallContact().hadContact(0.25))
		{
			if (tigerConqueredBallTimer.isRunning() &&
					!tigerConqueredBallTimer.isTimeUp(getWFrame().getTimestamp()) && tigerHasContact)
			{
				// tiger has won the skirmish
				switch (strategyUsed)
				{
					case FREE_SKIRMISH -> successFreeSkirmish++;
					case DRIBBLE -> successDribble++;
				}

				tigerConqueredBallTimer.reset();
				skirmishTrackingState = ESkirmishTrackingState.NONE;

				// we choose a random free ball strategy, this strategy will be used by the attacker Role!
				strategyUsed = Math.random() > 0.5 ? EBallContestStrategy.FREE_SKIRMISH : EBallContestStrategy.DRIBBLE;
			} else if (tigerConqueredBallTimer.isTimeUp(getWFrame().getTimestamp()))
			{
				// tiger has lost the skirmish
				switch (strategyUsed)
				{
					case FREE_SKIRMISH -> failureFreeSkirmish++;
					case DRIBBLE -> failureDribble++;
				}

				tigerConqueredBallTimer.reset();
				skirmishTrackingState = ESkirmishTrackingState.NONE;
				// we choose a random free ball strategy, this strategy will be used by the attacker Role!
				strategyUsed = Math.random() > 0.5 ? EBallContestStrategy.FREE_SKIRMISH : EBallContestStrategy.DRIBBLE;
			}
		}

		double offset = getWFrame().getTeamColor() == ETeamColor.BLUE ? 0 : 2;
		getShapes(EAiShapesLayer.AI_SKIRMISH_RESULT)
				.add(new DrawableBorderText(
						Vector2.fromXY(1, 7 + offset),
						String.format("successDribble %d / %d", successDribble, successDribble + failureDribble)
				)
						.setFontSize(EFontSize.LARGE)
						.setColor(getWFrame().getTeamColor() == ETeamColor.BLUE ? Color.BLUE : Color.YELLOW));

		getShapes(EAiShapesLayer.AI_SKIRMISH_RESULT)
				.add(new DrawableBorderText(
						Vector2.fromXY(1, 8 + offset)
						, String.format(
						"successFreeSkirmish %d / %d", successFreeSkirmish, successFreeSkirmish + failureFreeSkirmish)
				)
						.setFontSize(EFontSize.LARGE)
						.setColor(getWFrame().getTeamColor() == ETeamColor.BLUE ? Color.BLUE : Color.YELLOW));

		opponentHadContactInLastFrame = opponentHasContestedContact;
	}
}
