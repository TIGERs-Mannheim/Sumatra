/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.04.2011
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.aicenter;

import edu.tigers.sumatra.ai.athena.IAIModeChanged;
import edu.tigers.sumatra.aicenter.view.IPlayControlPanelObserver;
import edu.tigers.sumatra.aicenter.view.IRoleControlPanelObserver;


/**
 * The interface for the AI-center state-machine. It basically observes the view and reacts on every input given.
 * 
 * @author Gero
 */
public interface IAICenterObserver extends IPlayControlPanelObserver, IRoleControlPanelObserver, IAIModeChanged
{
}
