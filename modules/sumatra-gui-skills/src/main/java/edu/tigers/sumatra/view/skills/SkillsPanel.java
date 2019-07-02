package edu.tigers.sumatra.view.skills;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.github.g3force.instanceables.InstanceablePanel;

import edu.tigers.sumatra.botmanager.botskills.EBotSkill;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.views.ISumatraView;
import net.miginfocom.swing.MigLayout;


public class SkillsPanel extends JPanel implements ISumatraView
{
	private final JComboBox<BotID> cmbBots = new JComboBox<>();
	private final InstanceablePanel skillPanel;
	private final InstanceablePanel botSkillPanel;
	private final MotorEnhancedInputPanel enhancedInputPanel = new MotorEnhancedInputPanel();
	private final JButton resetButton = new JButton("Reset");
	
	
	public SkillsPanel()
	{
		setLayout(new MigLayout("fill"));
		
		JPanel selBotPanel = new JPanel();
		selBotPanel.add(new JLabel("Choose Bot: "));
		cmbBots.setPreferredSize(new Dimension(150, 25));
		selBotPanel.add(cmbBots);
		
		skillPanel = new InstanceablePanel(ESkill.values(), SumatraModel.getInstance().getUserSettings());
		skillPanel.setShowCreate(true);
		
		botSkillPanel = new InstanceablePanel(EBotSkill.values(), SumatraModel.getInstance().getUserSettings());
		botSkillPanel.setShowCreate(true);
		
		JPanel createSkillsPanel = new JPanel(new MigLayout("", ""));
		createSkillsPanel.add(skillPanel);
		createSkillsPanel.add(botSkillPanel);
		
		JPanel resetPanel = new JPanel();
		resetPanel.setBorder(BorderFactory.createTitledBorder("Reset"));
		
		resetPanel.add(resetButton);
		
		add(selBotPanel, "wrap");
		add(enhancedInputPanel, "aligny top, spany 2");
		add(createSkillsPanel, "wrap");
		add(resetPanel, "wrap");
	}
	
	
	public JComboBox<BotID> getCmbBots()
	{
		return cmbBots;
	}
	
	
	public InstanceablePanel getSkillPanel()
	{
		return skillPanel;
	}
	
	
	public InstanceablePanel getBotSkillPanel()
	{
		return botSkillPanel;
	}
	
	
	public MotorEnhancedInputPanel getEnhancedInputPanel()
	{
		return enhancedInputPanel;
	}
	
	
	public JButton getResetButton()
	{
		return resetButton;
	}
}
