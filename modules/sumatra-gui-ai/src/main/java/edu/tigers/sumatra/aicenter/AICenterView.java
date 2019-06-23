/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.aicenter;

import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AICenterView extends ASumatraView
{
	/**
	 * Default
	 */
	public AICenterView()
	{
		super(ESumatraViewType.AI_CENTER);
	}
	
	
	@Override
	public ISumatraViewPresenter createPresenter()
	{
		return new AICenterPresenter();
	}
}
