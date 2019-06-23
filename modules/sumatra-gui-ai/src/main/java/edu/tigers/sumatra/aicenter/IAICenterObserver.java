/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.aicenter;

import edu.tigers.sumatra.ai.athena.IAIModeChanged;
import edu.tigers.sumatra.aicenter.view.IRoleControlPanelObserver;


/**
 * The interface for the AI-center state-machine. It basically observes the view and reacts on every input given.
 * 
 * @author Gero
 */
public interface IAICenterObserver extends IRoleControlPanelObserver, IAIModeChanged
{
}
