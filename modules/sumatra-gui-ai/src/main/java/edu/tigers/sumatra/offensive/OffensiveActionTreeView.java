/*
 * *********************************************************
 * Project: TIGERS - Sumatra
 * Author(s): Marius Messerschmidt <marius.messerschmidt@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.offensive;

import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * This view shows information about offensiveActionTree
 *
 * @author Marius Messerschmidt <marius.messerschmidt@dlr.de>
 */
public class OffensiveActionTreeView extends ASumatraView
{

    // --------------------------------------------------------------------------
    // --- variables and constants ----------------------------------------------
    // --------------------------------------------------------------------------


    // --------------------------------------------------------------------------
    // --- constructors ---------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     *
     */
    public OffensiveActionTreeView()
    {
        super(ESumatraViewType.OFFENSIVE_ACTION_TREES);
    }


    // --------------------------------------------------------------------------
    // --- methods --------------------------------------------------------------
    // --------------------------------------------------------------------------
    @Override
    protected ISumatraViewPresenter createPresenter()
    {
        return new OffensiveActionTreePresenter();
    }
    // --------------------------------------------------------------------------
    // --- getter/setter --------------------------------------------------------
    // --------------------------------------------------------------------------
}
