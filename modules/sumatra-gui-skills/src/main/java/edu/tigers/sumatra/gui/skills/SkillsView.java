/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.skills;

import edu.tigers.sumatra.gui.skills.presenter.SkillsPresenter;
import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


public class SkillsView extends ASumatraView
{
	public SkillsView()
	{
		super(ESumatraViewType.SKILLS);
	}
	
	
	@Override
	protected ISumatraViewPresenter createPresenter()
	{
		return new SkillsPresenter();
	}
}
