/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.StateMachine;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.function.Supplier;


/**
 * Detect skirmish situations.
 */
@RequiredArgsConstructor
public class SkirmishDetectorCalc extends ACalculator
{
	private final StateMachine<ASkirmishState> stateMachine = new StateMachine<>(this.getClass().getSimpleName());
	private final IdleSkirmishState idleSkirmishState = new IdleSkirmishState();
	private final TransitionSkirmishState transitionSkirmishState = new TransitionSkirmishState();
	private final SkirmishState skirmishState = new SkirmishState();

	private final Supplier<BotDistance> tigerClosestToBall;
	private final Supplier<BotDistance> opponentClosestToBall;

	@Getter
	private SkirmishInformation skirmishInformation;


	@Override
	protected void start()
	{
		stateMachine.setInitialState(idleSkirmishState);
	}


	@Override
	public void doCalc()
	{
		skirmishInformation = new SkirmishInformation();
		stateMachine.update();

		DrawableAnnotation dt = new DrawableAnnotation(getBall().getPos().addNew(Vector2.fromXY(200, 0)),
				stateMachine.getCurrentState().getClass().getSimpleName(), Color.BLACK);
		getShapes(EAiShapesLayer.AI_SKIRMISH_DETECTOR).add(dt);

		DrawableAnnotation strategy = new DrawableAnnotation(
				getBall().getPos().addNew(Vector2.fromXY(300, 0)),
				skirmishInformation.getStrategy().name(), Color.BLACK);
		getShapes(EAiShapesLayer.AI_SKIRMISH_DETECTOR).add(strategy);
	}


	private abstract class ASkirmishState extends AState
	{
		/**
		 * score [0,100].
		 * 100: very tight skirmish
		 * 0: loose skirmish with movement.
		 */
		protected double score = 50;


		protected boolean areBotsCloseToBall()
		{
			final double minBotDistance = 300;
			double size = minBotDistance + score * 2.0;
			IVector2 ballPos = getBall().getPos();
			DrawableCircle dc = new DrawableCircle(Circle.createCircle(ballPos, size), Color.RED);
			getShapes(EAiShapesLayer.AI_SKIRMISH_DETECTOR).add(dc);
			BotDistance opponentBot = opponentClosestToBall.get();
			BotDistance tigerBot = tigerClosestToBall.get();
			return (opponentBot.getDist() < size) && (tigerBot.getDist() < size);
		}
	}

	private class IdleSkirmishState extends ASkirmishState
	{
		@Override
		public void doUpdate()
		{
			if (areBotsCloseToBall())
			{
				stateMachine.changeState(transitionSkirmishState);
			}
		}
	}

	private class TransitionSkirmishState extends ASkirmishState
	{
		TimestampTimer timer = new TimestampTimer(1.5);


		@Override
		public void doEntryActions()
		{
			timer.start(getWFrame().getTimestamp());
		}


		@Override
		public void doUpdate()
		{
			if (!areBotsCloseToBall())
			{
				stateMachine.changeState(idleSkirmishState);
			} else if (timer.isTimeUp(getWFrame().getTimestamp()))
			{
				stateMachine.changeState(skirmishState);
			}
		}
	}

	private class SkirmishState extends ASkirmishState
	{
		private long oldTime = 0;
		private boolean oldStartState = false;


		@Override
		public void doEntryActions()
		{
			oldTime = getWFrame().getTimestamp();
		}


		@Override
		public void doUpdate()
		{
			SkirmishInformation lastSkirmishInformation = getAiFrame().getPrevFrame().getTacticalField()
					.getSkirmishInformation();
			if (!areBotsCloseToBall() ||
					(oldStartState && !lastSkirmishInformation.isStartCircleMove()))
			{
				stateMachine.changeState(idleSkirmishState);
			}
			oldStartState = lastSkirmishInformation.isStartCircleMove();

			var botId = tigerClosestToBall.get().getBotId();
			var bot = getWFrame().getBot(botId);
			score += (bot.getVel().getLength() - 0.4) * (oldTime - getWFrame().getTimestamp()) * 1e-7;
			score = SumatraMath.cap(score, 0, 100);

			getShapes(EAiShapesLayer.AI_SKIRMISH_DETECTOR)
					.add(new DrawableAnnotation(bot.getPos(), "score: " + score, Color.black));

			if (score > 90)
			{
				skirmishInformation.setStrategy(ESkirmishStrategy.FREE_BALL);
				IVector2 movePos = calcCatchMovePos(bot.getPos());
				skirmishInformation.setSupportiveCircleCatchPos(movePos);
				getShapes(EAiShapesLayer.AI_SKIRMISH_DETECTOR)
						.add(new DrawableCircle(Circle.createCircle(movePos, 50), new Color(255, 46, 19, 125)).setFill(true));
			} else
			{
				skirmishInformation.setStrategy(ESkirmishStrategy.BLOCKING);
			}

			oldTime = getWFrame().getTimestamp();
		}


		private IVector2 calcCatchMovePos(IVector2 ourPos)
		{
			IVector2 ballPos = getBall().getPos();
			IVector2 weToBall = ballPos.subtractNew(ourPos);
			double sgnWanted = Math.signum(ballPos.x()) * Math.signum(ballPos.y());
			double sgnToUs = Math.signum(weToBall.x());
			IVector dir = ourPos.subtractNew(ballPos).normalizeNew().getNormalVector();
			dir = Vector2.fromXY(dir.x() * sgnWanted, dir.y() * sgnToUs);
			IVector2 unscaled = Vector2.fromXY(ballPos.x() - ((250 + Geometry.getBotRadius()) * sgnToUs) * dir.x(),
					ballPos.y() + (-450 * sgnWanted) * dir.y()).addNew(weToBall.scaleToNew(-300));
			return ballPos.addNew(unscaled.subtractNew(ballPos).scaleToNew(500));
		}
	}
}
