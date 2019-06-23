/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.botoverview;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.botoverview.ConditionTable.ConditionTableModel;


/**
 * This panel gives an ai overview of a bot.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class BotOverviewPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long				serialVersionUID		= 1881197595736545167L;
	
	// Id
	private int									botId						= -1;
	private final JTextField				botIdfield;
	
	// Current role
	private final JTextField				roleText;
	private ARole								lastRole;
	
	// Condition summary
	private final JTextField				conditionStatusText;
	private Boolean							lastConditionStatus	= null;
	
	// Destination
	private final JTextField				destinationText;
	private IVector2							lastDest;
	/** Memories the current color for caching-purposes ({@link #setDestColorTo(Color)}) */
	private Color								lastDestColor			= null;
	

	// private ESkillName lastMove = null;
	private boolean							shooter					= false;
	private boolean							dribbler					= false;
	
	// Skills
	private final SkillSwitch				skillSwitch;
	private static final int				HISTORY_SIZE			= 10;
	private final JList						skillHistory;
	private final DefaultListModel		skillHistoryModel;
	
	private final JTextField				skillMove;
	private final JTextField				skillShooter;
	private final JTextField				skillDribble;
	
	// Conditions
	private final ConditionSwitch			conditionSwitch;
	private boolean							roleChanged				= true;
	private final ConditionTable			conditionTable;
	private final ConditionTableModel	conditionModel;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param botId
	 * @param name
	 */
	public BotOverviewPanel(int botId, String name)
	{
		setBotId(botId);
		
		setLayout(new MigLayout("fill, wrap 1", "[]", "top"));
		setBorder(BorderFactory.createTitledBorder(name));
		

		// Overview and Conditions
		// - Build
		botIdfield = new JTextField();
		botIdfield.setEditable(false);
		botIdfield.setBackground(Color.WHITE);
		botIdfield.setText("ID: " + Integer.toString(this.botId));
		
		roleText = new JTextField();
		roleText.setEditable(false);
		roleText.setBackground(Color.WHITE);
		
		conditionStatusText = new JTextField();
		conditionStatusText.setEditable(false);
		conditionStatusText.setBackground(Color.WHITE);
		
		destinationText = new JTextField();
		destinationText.setEditable(false);
		destinationText.setBackground(Color.WHITE);
		
		skillMove = new JTextField();
		skillMove.setEditable(false);
		skillMove.setBackground(Color.WHITE);
		
		skillShooter = new JTextField();
		skillShooter.setEditable(false);
		skillShooter.setBackground(Color.WHITE);
		
		skillDribble = new JTextField();
		skillDribble.setEditable(false);
		skillDribble.setBackground(Color.WHITE);
		

		// - Assembly
		conditionTable = new ConditionTable();
		conditionModel = conditionTable.getConditionModel();
		
		conditionSwitch = new ConditionSwitch(conditionTable, true);
		

		// Skills
		// - Build
		JPanel botIdPanel = new JPanel(new MigLayout("fill, wrap 1, ins 0", "[100!, fill]", "[][]15[][]15[][]"));
		botIdPanel.add(botIdfield);
		// add(play);
		botIdPanel.add(roleText);
		botIdPanel.add(conditionStatusText);
		botIdPanel.add(destinationText);
		
		botIdPanel.add(new JLabel("Conditions:"), "split 2");
		botIdPanel.add(conditionSwitch);
		botIdPanel.add(conditionTable);
		add(botIdPanel);
		

		// Skills
		// - Build
		skillSwitch = new SkillSwitch(false);
		skillHistoryModel = new DefaultListModel();
		skillHistory = new JList(skillHistoryModel);
		
		// - Assembly
		JPanel skillPanel = new JPanel(new MigLayout("fill, wrap 1, ins 0", "[100!, fill]"));
		skillPanel.add(new JLabel("Skills:"), "split 2");
		skillPanel.add(skillSwitch);
		skillPanel.add(skillMove);
		skillPanel.add(skillShooter);
		skillPanel.add(skillDribble);
		
		skillPanel.add(new JLabel("History:"));
		skillPanel.add(skillHistory, "grow");
		add(skillPanel);
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	void setBotId(int BotId)
	{
		this.botId = BotId;
	}
	

	int getBotId()
	{
		return botId;
	}
	

	/**
	 * @param newStatus
	 * @param newConditions
	 * @param newWf
	 */
	public void setConditions(boolean newStatus, List<ACondition> newConditions)
	{
		if (conditionSwitch.updateConditions())
		{
			// 1. Set overall status
			setOverallConditionStatus(newStatus);
			
			// 2. Check each condition: Has role (and thus its conditions) changed completely?)
			if (roleChanged)
			{
				conditionModel.updateEntireTable(newConditions);
				roleChanged = false;
			}
			// else
			// {
			// conditionModel.updateConditionsStatus();
			// }
			
			conditionTable.repaint();
		}
	}
	

	private void setOverallConditionStatus(boolean newStatus)
	{
		if (lastConditionStatus == null || lastConditionStatus != newStatus)
		{
			if (newStatus)
			{
				conditionStatusText.setBackground(Color.GREEN);
				conditionStatusText.setText("true");
			} else
			{
				conditionStatusText.setBackground(Color.RED);
				conditionStatusText.setText("false");
			}
			lastConditionStatus = newStatus;
		}
	}
	

	/**
	 * @param newRole
	 */
	public void setRole(ARole newRole)
	{
		if (lastRole != newRole)
		{
			roleText.setText(newRole.getType().name());
			lastRole = newRole;
			roleChanged = true;
		}
	}
	

	public void calcDestinationStatus(IVector2 destination, Boolean conStatus)
	{
		setDestination(destination);
		
		if (conStatus != null && conStatus == true)
		{
			setDestColorTo(Color.GREEN);
		} else
		{
			setDestColorTo(Color.RED);
		}
	}
	

	/**
	 * Set the background-color of {@link #destinationX} and {@link #destinationY} to color
	 * @param color
	 * @return Whether something has been changed or not
	 */
	private boolean setDestColorTo(Color color)
	{
		if (color != lastDestColor)
		{
			destinationText.setBackground(color);
			lastDestColor = color;
			
			return true;
		}
		return false;
	}
	

	private void setDestination(IVector2 pos)
	{
		if (lastDest == null || !lastDest.equals(pos))
		{
			final IVector2 rdPos = pos.roundNew(0);
			destinationText.setText(Float.toString(rdPos.x()) + ", " + Float.toString(rdPos.y()));
			lastDest = rdPos;
		}
	}
	
	public void setSkillDribble(ESkillName name)
	{
		if (!dribbler && skillSwitch.updateSkills())
		{
			skillDribble.setText(name.toString());
			dribbler = true;
		}
	}
	

	public void setSkillShooter(ESkillName name)
	{
		if (!shooter && skillSwitch.updateSkills())
		{
			skillShooter.setText(name.toString());
			shooter = true;
		}
	}
	

	public void setSkillMove(final ESkillName name)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				if (skillSwitch.updateSkills())
				{
					// Add element
					skillHistoryModel.insertElementAt(name, 0);
					
					// Remove last?
					final int modelSize = skillHistoryModel.getSize();
					if (modelSize > HISTORY_SIZE)
					{
						skillHistoryModel.removeRange(HISTORY_SIZE - 1, modelSize - 1);
					}
				}
			}
		});
	}
	

	public void unSetSkillDribble()
	{
		if (dribbler)
		{
			skillDribble.setText("");
			dribbler = false;
		}
	}
	

	public void unSetSkillShooter()
	{
		if (shooter)
		{
			skillShooter.setText("");
			shooter = false;
		}
	}
	

	public void unSetSkillMove()
	{
		skillMove.setText("");
		// lastMove = null;
	}
	

	public void clearSkills()
	{
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				skillHistoryModel.clear();
				unSetSkillDribble();
				unSetSkillMove();
				unSetSkillShooter();
			}
		});
	}
	

	public void clearView()
	{
		roleText.setText("");
		lastRole = null;
		
		conditionStatusText.setText("");
		conditionStatusText.setBackground(Color.WHITE);
		lastConditionStatus = null;
		
		destinationText.setText("");
		destinationText.setBackground(Color.WHITE);
		lastDest = null;
		
		// Clear conditions table
		conditionModel.resetContent();
		
		// Skills
		clearSkills();
	}
	
	
	private class ConditionSwitch extends JCheckBox implements ActionListener
	{
		private static final long		serialVersionUID	= 2232882099444089285L;
		
		private final ConditionTable	table;
		private boolean					updateConditions	= false;
		
		
		private ConditionSwitch(ConditionTable table, boolean initiallyUpdateConditions)
		{
			this.table = table;
			this.updateConditions = initiallyUpdateConditions;
			
			setSelected(initiallyUpdateConditions);
			addActionListener(this);
		}
		

		@Override
		public synchronized void actionPerformed(ActionEvent e)
		{
			updateConditions = !updateConditions;
			if (!updateConditions)
			{
				table.getConditionModel().resetContent();
				table.repaint();
			}
		}
		

		private synchronized boolean updateConditions()
		{
			return updateConditions;
		}
	}
	
	
	private class SkillSwitch extends JCheckBox implements ActionListener
	{
		private static final long		serialVersionUID	= 2232882099444089285L;
		
		private boolean					updateSkills	= false;
		
		
		private SkillSwitch(boolean initiallyUpdateSkills)
		{
			this.updateSkills = initiallyUpdateSkills;
			
			setSelected(initiallyUpdateSkills);
			addActionListener(this);
		}
		

		@Override
		public synchronized void actionPerformed(ActionEvent e)
		{
			updateSkills = !updateSkills;
			if (!updateSkills)
			{
				clearSkills();
			}
		}
		

		private synchronized boolean updateSkills()
		{
			return updateSkills;
		}
	}
}
