/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 4, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.observer;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.StateMachine;


/**
 * Register here to get notified about new events from skills
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public interface ISkillEventObserver
{
	/**
	 * A new Event was thrown. This events should be declared in according skills
	 * and inserted into the {@link StateMachine} transition
	 * 
	 * @param event
	 */
	void onNewEvent(Enum<? extends Enum<?>> event);
}
