/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import edu.tigers.sumatra.ai.data.BotDistance;
import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.offense.data.SkirmishInformation;
import edu.tigers.sumatra.ai.metis.offense.data.SkirmishInformation.ESkirmishStrategy;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.*;


/**
 * @author MarkG
 */
public class SkirmishDetectorCalc extends ACalculator
{
	private SkirmishDetector		detector		= new SkirmishDetector();
	
	private SkirmishInformation	information	= null;
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		information = new SkirmishInformation();
		detector.update(newTacticalField, baseAiFrame);
		
		DrawableAnnotation strategy = new DrawableAnnotation(
				baseAiFrame.getWorldFrame().getBall().getPos().addNew(Vector2.fromXY(300, 0)),
				information.getStrategy().name(), Color.BLACK);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.SKIRMISH_DETECTOR).add(strategy);
		
		ITrackedBot enemyBot = newTacticalField.getEnemyClosestToBall().getBot();
		if (enemyBot == null)
		{
			return;
		}
		
		information.setEnemyPos(new DynamicPosition(enemyBot));
		
		// fill Information here
		newTacticalField.setSkirmishInformation(information);
	}
	
	
	private enum EState
	{
		IDLE,
		TRANSITION,
		SKIRMISH
	}
	
	private enum EEvent implements IEvent
	{
		CLOSE_TO_BALL,
		NOT_CLOSE_TO_BALL,
		TIMEOUT,
		SKIRMISH_DETECTED,
		NONE
	}
	
	private class SkirmishDetector
	{
		private ASkirmishState currentState = null;
		
		
		/**
		 * 
		 */
		public SkirmishDetector()
		{
			// InitialState is IdleState
			currentState = new IdleSkirmishState();
		}
		
		
		/**
		 * @param newTacticalField
		 * @param baseAiFrame
		 */
		public void update(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
		{
			EEvent event = currentState.update(newTacticalField, baseAiFrame);
			switch (event)
			{
				case NONE:
					// Nothing to do here
					break;
				case CLOSE_TO_BALL:
					currentState.onExit(newTacticalField, baseAiFrame);
					currentState = new TransitionSkirmishState();
					currentState.onEntry(newTacticalField, baseAiFrame);
					break;
				case NOT_CLOSE_TO_BALL:
				case TIMEOUT:
					currentState.onExit(newTacticalField, baseAiFrame);
					currentState = new IdleSkirmishState();
					currentState.onEntry(newTacticalField, baseAiFrame);
					break;
				case SKIRMISH_DETECTED:
					currentState.onExit(newTacticalField, baseAiFrame);
					currentState = new SkirmishState();
					currentState.onEntry(newTacticalField, baseAiFrame);
					break;
			}
			
			IVector2 ballPos = baseAiFrame.getWorldFrame().getBall().getPos();
			DrawableAnnotation dt = new DrawableAnnotation(ballPos.addNew(Vector2.fromXY(200, 0)),
					currentState.getState().toString(), Color.BLACK);
			newTacticalField.getDrawableShapes().get(EAiShapesLayer.SKIRMISH_DETECTOR).add(dt);
		}
	}
	
	private abstract class ASkirmishState
	{
		private final EState	state;
		
		/**
		 * score [0,100].
		 * 100: very tight skirmish
		 * 0: loose skirmish with movement.
		 */
		protected double		score	= 50;
		
		
		/**
		 * @param state
		 */
		public ASkirmishState(final EState state)
		{
			this.state = state;
		}
		
		
		/**
		 * @return the state
		 */
		public EState getState()
		{
			return state;
		}
		
		
		/**
		 * @param newTacticalField
		 * @param baseAiFrame
		 * @return
		 */
		public abstract EEvent update(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame);
		
		
		/**
		 * @param newTacticalField
		 * @param baseAiFrame
		 */
		public abstract void onEntry(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame);
		
		
		/**
		 * @param newTacticalField
		 * @param baseAiFrame
		 */
		public abstract void onExit(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame);
		
		
		/**
		 * @param newTacticalField
		 * @param baseAiFrame
		 * @return
		 */
		protected boolean areBotsCloseToBall(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
		{
			final double minBotDistance = 300;
			double size = minBotDistance + score * 2.0;
			IVector2 ballPos = baseAiFrame.getWorldFrame().getBall().getPos();
			DrawableCircle dc = new DrawableCircle(Circle.createCircle(ballPos, size), Color.RED);
			newTacticalField.getDrawableShapes().get(EAiShapesLayer.SKIRMISH_DETECTOR).add(dc);
			BotDistance enemyBot = newTacticalField.getEnemyClosestToBall();
			BotDistance tigerBot = newTacticalField.getTigerClosestToBall();
			return (enemyBot.getDist() < size) && (tigerBot.getDist() < size);
			
		}
		
		
		protected boolean isSkirmish()
		{
			/*
			 * Additional Skirmish criteria here.
			 */
			return true;
		}
	}
	
	private class IdleSkirmishState extends ASkirmishState
	{
		/**
		 * 
		 */
		public IdleSkirmishState()
		{
			super(EState.IDLE);
		}
		
		
		@Override
		public EEvent update(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
		{
			if (areBotsCloseToBall(newTacticalField, baseAiFrame))
			{
				return EEvent.CLOSE_TO_BALL;
			}
			return EEvent.NONE;
		}
		
		
		@Override
		public void onEntry(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
		{
			//
		}
		
		
		@Override
		public void onExit(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
		{
			//
		}
	}
	
	private class TransitionSkirmishState extends ASkirmishState
	{
		
		private long start = 0;
		
		
		/**
		 * 
		 */
		public TransitionSkirmishState()
		{
			super(EState.TRANSITION);
		}
		
		
		@Override
		public EEvent update(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
		{
			if (start == 0)
			{
				start = baseAiFrame.getWorldFrame().getTimestamp();
			}
			
			if (!areBotsCloseToBall(newTacticalField, baseAiFrame))
			{
				return EEvent.NOT_CLOSE_TO_BALL;
			}
			
			final long transitionTime = 1_500_000_000L;
			if ((baseAiFrame.getWorldFrame().getTimestamp() - start) > transitionTime)
			{
				boolean criteria = isSkirmish();
				if (criteria)
				{
					return EEvent.SKIRMISH_DETECTED;
				}
				return EEvent.TIMEOUT;
			}
			
			return EEvent.NONE;
		}
		
		
		@Override
		public void onEntry(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
		{
			//
		}
		
		
		@Override
		public void onExit(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
		{
			//
		}
	}
	
	private class SkirmishState extends ASkirmishState
	{
		private long					oldTime			= 0;
		
		private ESkirmishStrategy	strategy			= ESkirmishStrategy.NONE;
		
		private boolean				oldStartState	= false;
		
		
		/**
		 * 
		 */
		public SkirmishState()
		{
			super(EState.SKIRMISH);
		}
		
		
		@Override
		public EEvent update(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
		{
			if (!isSkirmish() || !areBotsCloseToBall(newTacticalField, baseAiFrame))
			{
				return EEvent.NOT_CLOSE_TO_BALL;
			} else if (oldStartState && !baseAiFrame.getPrevFrame().getTacticalField().getSkirmishInformation().isStartCircleMove())
			{
				// move done, end skirmish
				return EEvent.TIMEOUT;
			}
			oldStartState = baseAiFrame.getPrevFrame().getTacticalField().getSkirmishInformation().isStartCircleMove();
			
			ITrackedBot bot = newTacticalField.getTigerClosestToBall().getBot();
			score += (bot.getVel().getLength() - 0.4) * (oldTime - baseAiFrame.getWorldFrame().getTimestamp()) * 1e-7;
			score = Math.min(100, Math.max(0, score));
			DrawableAnnotation dt = new DrawableAnnotation(bot.getPos(), "score: " + score, Color.black);
			newTacticalField.getDrawableShapes().get(EAiShapesLayer.SKIRMISH_DETECTOR).add(dt);
			
			if (score > 90)
			{
				strategy = ESkirmishStrategy.FREE_BALL;
				IVector2 movePos = calcCatchMovePos(baseAiFrame, bot.getPos());
				information.setSupportiveCircleCatchPos(movePos);
				DrawableCircle dc = new DrawableCircle(Circle.createCircle(movePos, 50), new Color(255, 46, 19, 125));
				dc.setFill(true);
				newTacticalField.getDrawableShapes().get(EAiShapesLayer.SKIRMISH_DETECTOR).add(dc);
			} else
			{
				strategy = ESkirmishStrategy.BLOCKING;
			}
			
			information.setSkirmishDetected(true);
			information.setSkirmishIntensity(score);
			information.setStrategy(strategy);
			oldTime = baseAiFrame.getWorldFrame().getTimestamp();
			return EEvent.NONE;
		}
		
		
		@Override
		public void onEntry(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
		{
			oldTime = baseAiFrame.getWorldFrame().getTimestamp();
			strategy = ESkirmishStrategy.BLOCKING;
		}
		
		
		@Override
		public void onExit(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
		{
			//
		}
		
		
		private IVector2 calcCatchMovePos(final BaseAiFrame baseAiFrame,
				IVector2 ourPos)
		{
			IVector2 ballPos = baseAiFrame.getWorldFrame().getBall().getPos();
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
