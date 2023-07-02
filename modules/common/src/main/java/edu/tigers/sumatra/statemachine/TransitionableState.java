/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.statemachine;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;


@Log4j2
@RequiredArgsConstructor
public abstract class TransitionableState extends AState
{
	private final Consumer<IState> nextStateConsumer;
	private final List<Transition> transitions = new ArrayList<>();


	public final void addTransition(Transition transition)
	{
		transitions.add(transition);
	}


	public final void addTransition(BooleanSupplier evaluation, IState nextState)
	{
		transitions.add(new Transition(evaluation.toString(), evaluation, nextState));
	}


	public final void addTransition(String name, BooleanSupplier evaluation, IState nextState)
	{
		transitions.add(new Transition(name, evaluation, nextState));
	}


	protected void onInit()
	{
		// can be overwritten
	}


	protected void onExit()
	{
		// can be overwritten
	}


	protected void beforeUpdate()
	{
		// can be overwritten
	}


	protected void onUpdate()
	{
		// can be overwritten
	}


	@Override
	public void doEntryActions()
	{
		onInit();
	}


	@Override
	public void doExitActions()
	{
		onExit();
	}


	@Override
	public final void doUpdate()
	{
		beforeUpdate();
		for (Transition transition : transitions)
		{
			if (transition.getEvaluation().getAsBoolean())
			{
				log.trace(
						"Switch state from {} to {} for '{}'",
						this, transition.getNextState(), transition.getName()
				);
				nextStateConsumer.accept(transition.getNextState());
				return;
			}
		}
		onUpdate();
	}
}
