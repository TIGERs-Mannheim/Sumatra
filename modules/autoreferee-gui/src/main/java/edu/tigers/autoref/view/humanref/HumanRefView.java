/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 4, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.humanref;

import edu.tigers.autoref.presenter.HumanRefViewPresenter;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * @author "Lukas Magel"
 */
public class HumanRefView extends ASumatraView
{
	/**
	 * 
	 */
	public HumanRefView()
	{
		super(ESumatraViewType.HUMAN_REF_VIEW);
	}
	
	
	@Override
	protected ISumatraViewPresenter createPresenter()
	{
		return new HumanRefViewPresenter();
	}
	
}
