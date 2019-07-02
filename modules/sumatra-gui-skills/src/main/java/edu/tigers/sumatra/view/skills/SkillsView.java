package edu.tigers.sumatra.view.skills;

import edu.tigers.sumatra.presenter.skills.SkillsPresenter;
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
