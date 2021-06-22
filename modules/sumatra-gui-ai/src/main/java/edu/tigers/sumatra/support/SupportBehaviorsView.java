/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

/*
 * *********************************************************
 * Project: TIGERS - Sumatra
 * Author(s): Marius Messerschmidt <marius.messerschmidt@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.support;

import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * This view shows information about all active support Behaviors
 *
 */
public class SupportBehaviorsView extends ASumatraView
{
    public SupportBehaviorsView()
    {
        super(ESumatraViewType.SUPPORT_BEHAVIORS);
    }

    // --------------------------------------------------------------------------
    // --- methods --------------------------------------------------------------
    // --------------------------------------------------------------------------
    @Override
    protected ISumatraViewPresenter createPresenter()
    {
        return new SupportBehaviorsPresenter();
    }
    // --------------------------------------------------------------------------
    // --- getter/setter --------------------------------------------------------
    // --------------------------------------------------------------------------
}
