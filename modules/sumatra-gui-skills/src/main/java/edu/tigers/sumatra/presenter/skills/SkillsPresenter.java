package edu.tigers.sumatra.presenter.skills;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import com.github.g3force.instanceables.IInstanceableObserver;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.bot.params.BotMovementLimits;
import edu.tigers.sumatra.botmanager.botskills.ABotSkill;
import edu.tigers.sumatra.botmanager.botskills.AMoveBotSkill;
import edu.tigers.sumatra.botmanager.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.botmanager.botskills.EBotSkill;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.ASkill;
import edu.tigers.sumatra.skillsystem.skills.BotSkillWrapperSkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.view.skills.MotorEnhancedInputPanel;
import edu.tigers.sumatra.view.skills.SkillsPanel;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;


public class SkillsPresenter extends ASumatraViewPresenter
		implements IInstanceableObserver, MotorEnhancedInputPanel.IMotorInputPanelObserver
{
	private static final String DEF_SKILL_KEY = SkillsPresenter.class.getCanonicalName() + ".defskill";
	private static final String DEF_BOT_SKILL_KEY = SkillsPresenter.class.getCanonicalName() + ".defbotskill";
	
	private final SkillsPanel skillsPanel = new SkillsPanel();
	private ASkillSystem skillSystem;
	private BotID botId;
	
	
	public SkillsPresenter()
	{
		skillsPanel.getCmbBots().addItemListener(new BotIdSelectedActionListener());
		skillsPanel.getBotSkillPanel().addObserver(this);
		skillsPanel.getSkillPanel().addObserver(this);
		skillsPanel.getEnhancedInputPanel().addObserver(this);
		skillsPanel.getResetButton().addActionListener(new Reset());
		
		setDefaultBotSkill();
		setDefaultSkill();
	}
	
	
	@Override
	public Component getComponent()
	{
		return skillsPanel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return skillsPanel;
	}
	
	
	@Override
	public void onStart()
	{
		skillSystem = SumatraModel.getInstance().getModule(ASkillSystem.class);
		BotID.getAll().forEach(b -> skillsPanel.getCmbBots().addItem(b));
	}
	
	
	@Override
	public void onStop()
	{
		skillSystem = null;
		skillsPanel.getCmbBots().removeAllItems();
	}
	
	
	private void executeSkill(ASkill skill)
	{
		if (skillSystem != null && botId != null && botId.isBot())
		{
			skillSystem.execute(botId, skill);
		}
	}
	
	
	private void setDefaultBotSkill()
	{
		String strDefBotSkill = SumatraModel.getInstance().getUserProperty(DEF_BOT_SKILL_KEY,
				EBotSkill.GLOBAL_POSITION.name());
		try
		{
			EBotSkill defSkill = EBotSkill.valueOf(strDefBotSkill);
			skillsPanel.getBotSkillPanel().setSelectedItem(defSkill);
		} catch (IllegalArgumentException err)
		{
			// ignore
		}
	}
	
	
	private void setDefaultSkill()
	{
		String strDefSkill = SumatraModel.getInstance().getUserProperty(DEF_SKILL_KEY, ESkill.TOUCH_KICK.name());
		try
		{
			ESkill defSkill = ESkill.valueOf(strDefSkill);
			skillsPanel.getSkillPanel().setSelectedItem(defSkill);
		} catch (IllegalArgumentException err)
		{
			// ignore
		}
	}
	
	
	@Override
	public void onNewInstance(final Object object)
	{
		if (object instanceof ASkill)
		{
			ASkill skill = (ASkill) object;
			SumatraModel.getInstance().setUserProperty(DEF_SKILL_KEY, skill.getType().name());
			executeSkill(skill);
		} else if (object instanceof ABotSkill)
		{
			ABotSkill skill = (ABotSkill) object;
			SumatraModel.getInstance().setUserProperty(DEF_BOT_SKILL_KEY, skill.getType().name());
			BotSkillWrapperSkill wrapperSkill = new BotSkillWrapperSkill(skill);
			executeSkill(wrapperSkill);
		}
	}
	
	
	@Override
	public void onSetSpeed(final double x, final double y, final double w)
	{
		final MoveConstraints mc = new MoveConstraints(new BotMovementLimits());
		AMoveBotSkill skill = new BotSkillLocalVelocity(Vector2.fromXY(x, y), w, mc);
		final BotSkillWrapperSkill wrapperSkill = new BotSkillWrapperSkill(skill);
		wrapperSkill.setKeepKickerDribbler(true);
		executeSkill(wrapperSkill);
	}
	
	
	private class Reset implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			executeSkill(new IdleSkill());
		}
	}
	
	private class BotIdSelectedActionListener implements ItemListener
	{
		@Override
		public void itemStateChanged(final ItemEvent e)
		{
			SkillsPresenter.this.botId = (BotID) e.getItem();
		}
	}
}
