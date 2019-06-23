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

import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview.IAIModeChanged;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview.IPlayControlPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview.IRoleControlPanelObserver;


/**
 * The interface for the AI-center state-machine. It basically observes the view and reacts on every input given.
 * 
 * @author Gero
 */
public interface IAICenterObserver extends IPlayControlPanelObserver, IRoleControlPanelObserver, IAIModeChanged
{
}
