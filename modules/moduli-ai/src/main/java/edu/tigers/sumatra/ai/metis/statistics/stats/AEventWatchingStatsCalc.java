/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics.stats;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.statemachine.StateMachine;

import java.util.HashMap;
import java.util.Map;


public abstract class AEventWatchingStatsCalc<T> extends AStatsCalc
{
	private final Map<Long, T> occurrences = new HashMap<>();
	private final StateMachine<IState> stateMachine = new StateMachine<>(this.getClass().getSimpleName());
	private final IShapeLayerIdentifier shapeLayer;
	protected BaseAiFrame baseAiFrame;
	private T data;
	private T previousData;


	protected AEventWatchingStatsCalc(IShapeLayerIdentifier shapeLayer)
	{
		this.shapeLayer = shapeLayer;
		var noneState = new NoneState();
		var ongoingState = new OngoingState();
		var afterState = new AfterState();
		var newEventState = new NewEventState();

		stateMachine.setInitialState(noneState);
		stateMachine.addTransition(null, EEvents.NEW_EVENT_DETECTED, newEventState);
		stateMachine.addTransition(null, EEvents.EVENT_HAPPENED, noneState);

		stateMachine.addTransition(newEventState, EEvents.NEW_EVENT_VANISHED_IN_DEAD_TIME, noneState);
		stateMachine.addTransition(newEventState, EEvents.NEW_EVENT_ACCEPTED, ongoingState);
		stateMachine.addTransition(ongoingState, EEvents.START_AFTER, afterState);
		stateMachine.addTransition(afterState, EEvents.AFTER_TIMEOUT, noneState);
	}


	protected abstract T getNewData(T oldData);

	protected abstract T updateData(T oldData);

	protected abstract boolean hasEventHappened(T data);

	protected abstract void onEventHappened(T data);

	protected abstract void onEventNeverHappened(T data);

	protected abstract boolean canEventHappen();

	protected abstract Double getAfterWatchTime();

	protected abstract Double getDeadTime();


	@Override
	public void onStatisticUpdate(BaseAiFrame baseAiFrame)
	{
		this.baseAiFrame = baseAiFrame;
		clearOccurrences();
		stateMachine.update();
		drawBorderText(Vector2.fromXY(1.0, 8), stateMachine.getCurrentState().toString());
	}


	private void clearOccurrences()
	{
		var timestampNow = baseAiFrame.getWorldFrame().getTimestamp();
		occurrences.keySet().removeIf(then -> timestampNow - then >= getDeadTime() * 1e9);
	}


	private void checkEventHappened(T dataToCheck)
	{
		if (hasEventHappened(dataToCheck))
		{
			onEventHappened(dataToCheck);
			occurrences.put(baseAiFrame.getWorldFrame().getTimestamp(), dataToCheck);
			drawBorderText(Vector2.fromXY(1.0, 9), "EventHappened");
			stateMachine.triggerEvent(EEvents.EVENT_HAPPENED);
		}
	}


	private boolean isNewData()
	{
		return previousData == null || !previousData.equals(data);
	}


	private void updateDataInternal()
	{
		previousData = data;
		data = updateData(data);
	}


	protected void drawBorderText(Vector2 basePos, String text)
	{
		if (shapeLayer != null)
		{
			baseAiFrame.getShapes(shapeLayer)
					.add(new DrawableBorderText(basePos.addNew(Vector2.fromX(7.0 * baseAiFrame.getTeamColor().getId())),
							text).setColor(baseAiFrame.getTeamColor().getColor()));
		}
	}


	private enum EEvents implements IEvent
	{
		NEW_EVENT_DETECTED, NEW_EVENT_VANISHED_IN_DEAD_TIME, NEW_EVENT_ACCEPTED, START_AFTER, AFTER_TIMEOUT, EVENT_HAPPENED,
	}

	private class NewEventState extends AState
	{
		@Override
		public void doEntryActions()
		{
			getNewDataInternal();
		}


		@Override
		public void doUpdate()
		{
			if (!canEventHappen())
			{
				stateMachine.triggerEvent(EEvents.NEW_EVENT_VANISHED_IN_DEAD_TIME);
				return;
			}
			if (isNewData())
			{
				getNewDataInternal();
			}
			if (!isDeadTime(data))
			{
				stateMachine.triggerEvent(EEvents.NEW_EVENT_ACCEPTED);
			}
		}


		private void getNewDataInternal()
		{
			previousData = data;
			data = getNewData(data);
		}


		private boolean isDeadTime(T dataToCheck)
		{
			return occurrences.containsValue(dataToCheck);
		}
	}

	private class NoneState extends AState
	{
		@Override
		public void doEntryActions()
		{
			data = null;
		}


		@Override
		public void doUpdate()
		{
			if (canEventHappen())
			{
				stateMachine.triggerEvent(EEvents.NEW_EVENT_DETECTED);
			}
		}
	}

	private class OngoingState extends AState
	{
		@Override
		public void doEntryActions()
		{
			updateDataInternal();
		}


		@Override
		public void doUpdate()
		{
			if (!canEventHappen())
			{
				stateMachine.triggerEvent(EEvents.START_AFTER);
				return;
			}
			updateDataInternal();
			if (isNewData())
			{
				onEventNeverHappened(data);
				stateMachine.triggerEvent(EEvents.NEW_EVENT_DETECTED);
				return;
			}
			checkEventHappened(data);
		}

	}

	private class AfterState extends AState
	{
		long timestampStart;


		@Override
		public void doEntryActions()
		{
			timestampStart = baseAiFrame.getWorldFrame().getTimestamp();
		}


		@Override
		public void doUpdate()
		{
			if (baseAiFrame.getWorldFrame().getTimestamp() - timestampStart >= getAfterWatchTime() * 1e9)
			{
				onEventNeverHappened(data);
				stateMachine.triggerEvent(EEvents.AFTER_TIMEOUT);
				return;
			}
			if (canEventHappen())
			{
				onEventNeverHappened(data);
				stateMachine.triggerEvent(EEvents.NEW_EVENT_DETECTED);
				return;
			}
			updateDataInternal();
			checkEventHappened(data);
		}
	}

}
