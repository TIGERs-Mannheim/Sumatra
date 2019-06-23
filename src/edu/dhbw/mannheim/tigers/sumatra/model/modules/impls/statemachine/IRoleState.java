/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 29, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ISkillSystemObserver;


/**
 * State of a state machine with skill observer
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public interface IRoleState extends ISkillSystemObserver, IState
{
}
