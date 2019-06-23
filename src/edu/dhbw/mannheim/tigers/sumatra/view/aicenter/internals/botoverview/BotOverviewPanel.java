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
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition.EConditionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
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
	private static final long							serialVersionUID		= 1881197595736545167L;
	
	// Id
	private BotID									botId						= BotID.createBotId();
	
	// Current role
	private final JTextField							roleText;
	private transient ARole								lastRole;
	
	// Condition summary
	private final JTextField							conditionStatusText;
	private Boolean										lastConditionStatus	= null;
	
	// Destination
	private final JTextField							destinationText;
	private IVector2										lastDest;
	/** Memories the current color for caching-purposes ({@link #setDestColorTo(Color)}) */
	private Color											lastDestColor			= null;
	
	// ball contact
	private final JTextField							ballContactText;
	
	// Skills
	private final SkillSwitch							skillSwitch;
	private static final int							HISTORY_SIZE			= 10;
	private final DefaultListModel<ESkillName>	skillHistoryModel;
	
	private final JTextField							txtSkill;
	// State
	private final JTextField							stateText;
	
	// Conditions
	private final ConditionSwitch						conditionSwitch;
	private boolean										roleChanged				= true;
	private final ConditionTable						conditionTable;
	private final ConditionTableModel				conditionModel;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param botId
	 * @param name
	 */
	public BotOverviewPanel(BotID botId, String name)
	{
		setBotId(botId);
		
		setLayout(new MigLayout("fill, wrap 1", "[]", "top"));
		setBorder(BorderFactory.createTitledBorder(name));
		
		
		// Overview and Conditions
		// - Build
		JTextField botIdfield = new JTextField();
		botIdfield.setEditable(false);
		botIdfield.setBackground(Color.WHITE);
		botIdfield.setText("ID: " + Integer.toString(this.botId.getNumber()));
		
		roleText = new JTextField();
		roleText.setEditable(false);
		roleText.setBackground(Color.WHITE);
		roleText.setToolTipText("Role");
		
		conditionStatusText = new JTextField();
		conditionStatusText.setEditable(false);
		conditionStatusText.setBackground(Color.WHITE);
		conditionStatusText.setToolTipText("All conditions fullfilled?");
		
		destinationText = new JTextField();
		destinationText.setEditable(false);
		destinationText.setBackground(Color.WHITE);
		destinationText.setToolTipText("Destination target");
		
		ballContactText = new JTextField();
		ballContactText.setEditable(false);
		ballContactText.setBackground(Color.WHITE);
		ballContactText.setToolTipText("Ball contact");
		
		stateText = new JTextField();
		stateText.setEditable(false);
		stateText.setBackground(Color.WHITE);
		stateText.setToolTipText("State of role");
		
		txtSkill = new JTextField();
		txtSkill.setEditable(false);
		txtSkill.setBackground(Color.WHITE);
		txtSkill.setToolTipText("Skill");
		
		
		// - Assembly
		conditionTable = new ConditionTable();
		conditionModel = conditionTable.getConditionModel();
		
		conditionSwitch = new ConditionSwitch(conditionTable, true);
		
		
		// Skills
		// - Build
		final JPanel botIdPanel = new JPanel(new MigLayout("fill, wrap 1, ins 0", "[100!, fill]", "[][]15[][]15[][]"));
		botIdPanel.add(botIdfield);
		botIdPanel.add(roleText);
		botIdPanel.add(conditionStatusText);
		botIdPanel.add(destinationText);
		botIdPanel.add(ballContactText);
		
		// State
		botIdPanel.add(stateText);
		
		botIdPanel.add(new JLabel("Conditions:"), "split 2");
		botIdPanel.add(conditionSwitch);
		botIdPanel.add(conditionTable);
		add(botIdPanel);
		
		
		// Skills
		// - Build
		skillSwitch = new SkillSwitch(false);
		skillHistoryModel = new DefaultListModel<ESkillName>();
		JList<ESkillName> skillHistory = new JList<ESkillName>(skillHistoryModel);
		
		// - Assembly
		final JPanel skillPanel = new JPanel(new MigLayout("fill, wrap 1, ins 0", "[100!, fill]"));
		skillPanel.add(new JLabel("Skills:"), "split 2");
		skillPanel.add(skillSwitch);
		skillPanel.add(txtSkill);
		
		skillPanel.add(new JLabel("History:"));
		skillPanel.add(skillHistory, "grow");
		add(skillPanel);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param botID
	 */
	public final void setBotId(final BotID botID)
	{
		botId = botID;
	}
	
	
	/**
	 * @return
	 */
	public final BotID getBotID()
	{
		return botId;
	}
	
	
	/**
	 * @param newStatus
	 * @param newConditions
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
		}
	}
	
	
	private void setOverallConditionStatus(boolean newStatus)
	{
		if ((lastConditionStatus == null) || (lastConditionStatus != newStatus))
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
		if ((lastRole == null) || !lastRole.equals(newRole))
		{
			roleText.setText(newRole.getType().name());
			lastRole = newRole;
			roleChanged = true;
		}
	}
	
	
	/**
	 * Set the actual state of the bot
	 * @param state
	 */
	public void setState(Enum<?> state)
	{
		if ((state != null))
		{
			stateText.setText(state.toString());
		} else
		{
			stateText.setText("UNKNOWN");
		}
	}
	
	
	/**
	 * @param destination
	 * @param conStatus
	 */
	public void calcDestinationStatus(IVector2 destination, EConditionState conStatus)
	{
		setDestination(destination);
		
		if (conStatus == EConditionState.FULFILLED)
		{
			setDestColorTo(Color.GREEN);
		} else if (conStatus == EConditionState.DISABLED)
		{
			setDestColorTo(Color.white);
		}
		{
			setDestColorTo(Color.RED);
		}
	}
	
	
	/**
	 * Set the background-color of {@link #destinationText} to color
	 * @param color
	 * @return Whether something has been changed or not
	 */
	private boolean setDestColorTo(Color color)
	{
		if (color.equals(lastDestColor))
		{
			destinationText.setBackground(color);
			lastDestColor = color;
			
			return true;
		}
		return false;
	}
	
	
	private void setDestination(IVector2 pos)
	{
		if ((pos != null) && ((lastDest == null) || !lastDest.equals(pos)))
		{
			final IVector2 rdPos = pos.roundNew(0);
			destinationText.setText(Float.toString(rdPos.x()) + ", " + Float.toString(rdPos.y()));
			lastDest = rdPos;
		}
	}
	
	
	/**
	 * @param ballContact
	 */
	public void setBallContact(final boolean ballContact)
	{
		ballContactText.setText(String.valueOf(ballContact));
	}
	
	
	/**
	 * @param name
	 */
	public void setSkill(final ESkillName name)
	{
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				if (skillSwitch.updateSkills())
				{
					txtSkill.setText(name.toString());
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
	
	
	/**
	 */
	public void unSetSkill()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				txtSkill.setText("");
			}
		});
	}
	
	
	/**
	 */
	public void clearSkills()
	{
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				skillHistoryModel.clear();
				unSetSkill();
			}
		});
	}
	
	
	/**
	 */
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
		
		ballContactText.setText("");
		ballContactText.setBackground(Color.WHITE);
		
		// Clear conditions table
		conditionModel.resetContent();
		
		// Skills
		clearSkills();
	}
	
	
	private static final class ConditionSwitch extends JCheckBox implements ActionListener
	{
		private static final long		serialVersionUID	= 2232882099444089285L;
		
		private final ConditionTable	table;
		private boolean					updateConditions	= false;
		
		
		private ConditionSwitch(ConditionTable table, boolean initiallyUpdateConditions)
		{
			this.table = table;
			updateConditions = initiallyUpdateConditions;
			
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
	
	
	private final class SkillSwitch extends JCheckBox implements ActionListener
	{
		private static final long	serialVersionUID	= 2232882099444089285L;
		
		private boolean				updateSkills		= false;
		
		
		private SkillSwitch(boolean initiallyUpdateSkills)
		{
			updateSkills = initiallyUpdateSkills;
			
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
