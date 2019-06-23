/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.04.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter;

import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview.IPlayControlPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview.IRoleControlPanelObserver;


/**
 * The interface for the AI-center state-machine. It basically observes the view and reacts on every input given.
 * 
 * The three implementations are:
 * <ul>
 * <li> {@link MatchModeState} ( {@link EAIControlState#MATCH_MODE} )</li>
 * <li> {@link PlayTestState} ( {@link EAIControlState#PLAY_TEST_MODE} )</li>
 * <li> {@link RoleTestState} ( {@link EAIControlState#ROLE_TEST_MODE} )</li>
 * </ul>
 * 
 * @author Gero
 */
public interface IAICenterState extends IPlayControlPanelObserver, IRoleControlPanelObserver
{
}
