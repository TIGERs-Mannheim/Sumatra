/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.botcenter.bots;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.github.g3force.instanceables.IInstanceableObserver;
import com.github.g3force.instanceables.InstanceablePanel;

import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.ABotSkill;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.ASkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import net.miginfocom.swing.MigLayout;


/**
 * Control various skills from this panel.
 * 
 * @author AndreR, DanielW
 */
public class SkillsPanel extends JPanel implements IInstanceableObserver
{
	private static final long serialVersionUID = 5399293600256113771L;
	
	private static final String DEF_SKILL_KEY = SkillsPanel.class.getCanonicalName()
			+ ".defskill";
	private static final String DEF_BOT_SKILL_KEY = SkillsPanel.class.getCanonicalName()
			+ ".defbotskill";
	
	private JTextField moveToX;
	private JTextField moveToY;
	
	private JTextField rotateAndMoveToX;
	private JTextField rotateAndMoveToY;
	private JTextField rotateAndMoveToXyAngle;
	
	private JTextField lookX;
	private JTextField lookY;
	
	private JTextField dribbleRPM;
	
	private final List<ISkillsPanelObserver> observers = new CopyOnWriteArrayList<>();
	
	
	/** Constructor. */
	@SuppressWarnings("squid:S1166")
	public SkillsPanel()
	{
		setLayout(new MigLayout("fill"));
		
		InstanceablePanel skillCustomPanel = new InstanceablePanel(ESkill.values(), SumatraModel.getInstance()
				.getUserSettings());
		skillCustomPanel.addObserver(this);
		skillCustomPanel.setShowCreate(true);
		String strDefSkill = SumatraModel.getInstance().getUserProperty(DEF_SKILL_KEY, ESkill.TOUCH_KICK.name());
		try
		{
			ESkill defSkill = ESkill.valueOf(strDefSkill);
			skillCustomPanel.setSelectedItem(defSkill);
		} catch (IllegalArgumentException err)
		{
			// ignore
		}
		
		InstanceablePanel botSkillCustomPanel = new InstanceablePanel(EBotSkill.values());
		botSkillCustomPanel.addObserver(this);
		botSkillCustomPanel.setShowCreate(true);
		String strDefBotSkill = SumatraModel.getInstance().getUserProperty(DEF_BOT_SKILL_KEY,
				EBotSkill.GLOBAL_POSITION.name());
		try
		{
			EBotSkill defSkill = EBotSkill.valueOf(strDefBotSkill);
			botSkillCustomPanel.setSelectedItem(defSkill);
		} catch (IllegalArgumentException err)
		{
			// ignore
		}
		
		JPanel createSkillsPanel = new JPanel(new MigLayout("", ""));
		createSkillsPanel.add(skillCustomPanel);
		createSkillsPanel.add(botSkillCustomPanel);
		
		
		// MOVING TO XY
		final JPanel moveToXYPanel = new JPanel(new MigLayout("fill", "[]10[50,fill]20[]10[50,fill]20[100,fill]"));
		
		moveToX = new JTextField("0");
		moveToY = new JTextField("0");
		
		final JButton moveToXY = new JButton("Move to XY");
		moveToXY.addActionListener(new MoveToXY());
		
		moveToXYPanel.add(new JLabel("X:"));
		moveToXYPanel.add(moveToX);
		moveToXYPanel.add(new JLabel("Y:"));
		moveToXYPanel.add(moveToY);
		moveToXYPanel.add(moveToXY);
		
		moveToXYPanel.setBorder(BorderFactory.createTitledBorder("MoveToXY"));
		
		
		// LOOKING
		final JPanel aimPanel = new JPanel(new MigLayout("fill", "[]10[50,fill]20[]10[50,fill]20[100,fill]"));
		lookX = new JTextField("0");
		lookY = new JTextField("0");
		
		final JButton lookAt = new JButton("look xy");
		lookAt.addActionListener(new LookAt());
		
		
		aimPanel.add(new JLabel("x: "));
		aimPanel.add(lookX);
		aimPanel.add(new JLabel("y: "));
		aimPanel.add(lookY);
		aimPanel.add(lookAt);
		
		aimPanel.setBorder(BorderFactory.createTitledBorder("LookAt"));
		
		// DRIBBLE
		final JPanel dribblePanel = new JPanel(new MigLayout("fill", "[]10[50,fill]20[100,fill]"));
		
		dribbleRPM = new JTextField("10000");
		
		final JButton dribble = new JButton("Dribble");
		dribble.addActionListener(new Dribble());
		
		dribblePanel.add(new JLabel("rpm: "));
		dribblePanel.add(dribbleRPM);
		dribblePanel.add(dribble);
		
		dribblePanel.setBorder(BorderFactory.createTitledBorder("Dribble"));
		
		// RotateAndMoveToXY
		final JPanel rotateAndMoveToXYPanel = new JPanel(new MigLayout("fill",
				"[]10[50,fill]20[]10[50,fill]20[]10[50,fill]20[]"));
		
		rotateAndMoveToX = new JTextField("0");
		rotateAndMoveToY = new JTextField("0");
		rotateAndMoveToXyAngle = new JTextField("3");
		
		final JButton rotateAndMoveToXY = new JButton("Move to XY and rotate");
		rotateAndMoveToXY.addActionListener(new RotateAndMoveToXY());
		
		rotateAndMoveToXYPanel.add(new JLabel("X:"));
		rotateAndMoveToXYPanel.add(rotateAndMoveToX);
		rotateAndMoveToXYPanel.add(new JLabel("Y:"));
		rotateAndMoveToXYPanel.add(rotateAndMoveToY);
		rotateAndMoveToXYPanel.add(new JLabel("Angle:"));
		rotateAndMoveToXYPanel.add(rotateAndMoveToXyAngle);
		
		rotateAndMoveToXYPanel.add(rotateAndMoveToXY);
		
		JPanel resetPanel = new JPanel();
		resetPanel.setBorder(BorderFactory.createTitledBorder("Reset"));
		
		JButton btnReset = new JButton("Reset");
		resetPanel.add(btnReset);
		btnReset.addActionListener(new Reset());
		
		add(createSkillsPanel, "wrap");
		add(resetPanel, "wrap");
		add(moveToXYPanel, "wrap");
		add(aimPanel, "wrap");
		add(dribblePanel, "wrap");
		add(Box.createGlue(), "push");
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param observer
	 */
	public void addObserver(final ISkillsPanelObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final ISkillsPanelObserver observer)
	{
		observers.remove(observer);
	}
	
	
	/**
	 * @param skill
	 */
	private void notifySkill(final ASkill skill)
	{
		for (final ISkillsPanelObserver observer : observers)
		{
			observer.onSkill(skill);
		}
	}
	
	
	/**
	 * @param skill
	 */
	private void notifyBotSkill(final ABotSkill skill)
	{
		for (final ISkillsPanelObserver observer : observers)
		{
			observer.onBotSkill(skill);
		}
	}
	
	
	@Override
	public void onNewInstance(final Object object)
	{
		if (object instanceof ASkill)
		{
			ASkill skill = (ASkill) object;
			SumatraModel.getInstance().setUserProperty(DEF_SKILL_KEY, skill.getType().name());
			notifySkill(skill);
		} else if (object instanceof ABotSkill)
		{
			ABotSkill skill = (ABotSkill) object;
			SumatraModel.getInstance().setUserProperty(DEF_BOT_SKILL_KEY, skill.getType().name());
			notifyBotSkill(skill);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- actions --------------------------------------------------------------
	// --------------------------------------------------------------------------
	private class MoveToXY implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			double x;
			double y;
			
			try
			{
				x = Double.parseDouble(moveToX.getText());
				y = Double.parseDouble(moveToY.getText());
			} catch (final NumberFormatException e)
			{
				return;
			}
			
			notifyMoveToXY(x, y);
		}
		
		
		private void notifyMoveToXY(final double x, final double y)
		{
			for (final ISkillsPanelObserver observer : observers)
			{
				observer.onMoveToXY(x, y);
			}
		}
	}
	
	private class RotateAndMoveToXY implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			double x;
			double y;
			double angle;
			try
			{
				x = Double.parseDouble(rotateAndMoveToX.getText());
				y = Double.parseDouble(rotateAndMoveToY.getText());
				angle = Double.parseDouble(rotateAndMoveToXyAngle.getText());
			} catch (final NumberFormatException e)
			{
				return;
			}
			
			notifyRotateAndMoveToXY(x, y, angle);
		}
		
		
		private void notifyRotateAndMoveToXY(final double x, final double y, final double angle)
		{
			for (final ISkillsPanelObserver observer : observers)
			{
				observer.onRotateAndMoveToXY(x, y, angle);
			}
		}
	}
	
	
	private class LookAt implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			Double x;
			Double y;
			try
			{
				x = Double.valueOf(lookX.getText());
				y = Double.valueOf(lookY.getText());
			} catch (final NumberFormatException err)
			{
				return;
			}
			notifyLookAt(Vector2.fromXY(x, y));
		}
		
		
		private void notifyLookAt(final Vector2 lookAtTarget)
		{
			for (final ISkillsPanelObserver observer : observers)
			{
				observer.onLookAt(lookAtTarget);
			}
		}
	}
	
	
	private class Dribble implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			int rpm;
			try
			{
				rpm = Integer.valueOf(dribbleRPM.getText());
				dribbleRPM.setBackground(Color.WHITE);
			} catch (final NumberFormatException err)
			{
				dribbleRPM.setBackground(Color.RED);
				return;
			}
			notifyDribble(rpm);
		}
		
		
		private void notifyDribble(final int rpm)
		{
			for (final ISkillsPanelObserver observer : observers)
			{
				observer.onDribble(rpm);
			}
		}
	}
	
	private class Reset implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			notifySkill(new IdleSkill());
		}
	}
}
