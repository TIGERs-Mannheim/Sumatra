/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.EAIControlState;
import edu.dhbw.mannheim.tigers.sumatra.util.IInstanceableObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.commons.InstanceablePanel;


/**
 * This panel can be used to control the actual bot-role assignment.
 * This must be a child panel of {@link ModuleControlPanel}.
 * 
 * @author Oliver, Malte, Gero
 */
public class RoleControlPanel extends JPanel implements IAIModeChanged
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long								serialVersionUID		= -6902947971327765508L;
	
	private JList<ARole>										activeRolesList		= null;
	private DefaultListModel<ARole>						activeRolesListModel	= null;
	
	private final InstanceablePanel						instanceablePanel;
	private final JButton									clearRolesButton;
	private final JButton									deleteRoleButton;
	private final JCheckBox									checkUseBotId;
	
	private List<JComponent>								components				= new ArrayList<JComponent>();
	
	private static final Color								ERROR_COLOR				= Color.RED;
	private static final Color								DEFAULT_COLOR			= Color.WHITE;
	private static final int								LIST_SIZE_X				= 250;
	private static final int								LIST_SIZE_Y				= 100;
	private final JTextField								textBotId;
	
	private final List<IRoleControlPanelObserver>	observers				= new LinkedList<IRoleControlPanelObserver>();
	
	private static final String							ROLE_KEY					= RoleControlPanel.class.getName() + ".role";
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public RoleControlPanel()
	{
		setLayout(new MigLayout("fill, insets 0", "[left][left]", "[top]"));
		
		activeRolesListModel = new DefaultListModel<ARole>();
		activeRolesList = new JList<ARole>(activeRolesListModel);
		// activeRolesList.setCellRenderer(new PlayListRenderer());
		activeRolesList.setPreferredSize(new Dimension(LIST_SIZE_X, LIST_SIZE_Y));
		activeRolesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		final JScrollPane scrollPaneActiveRoles = new JScrollPane();
		scrollPaneActiveRoles.getViewport().setView(activeRolesList);
		
		instanceablePanel = new InstanceablePanel(ERole.values());
		instanceablePanel.addObserver(new NewInstanceObserver());
		
		clearRolesButton = new JButton("clear all Roles");
		clearRolesButton.addActionListener(new ClearRolesListener());
		
		deleteRoleButton = new JButton("Delete role");
		deleteRoleButton.addActionListener(new DeleteRoleListener());
		
		final JPanel botIdPanel = new JPanel();
		checkUseBotId = new JCheckBox("BotID: ");
		checkUseBotId.setSelected(false);
		checkUseBotId.addActionListener(new UseBotIdListener());
		
		textBotId = new JTextField("0", 3);
		textBotId.addKeyListener(new BotIdChangeListener());
		
		botIdPanel.add(checkUseBotId);
		botIdPanel.add(textBotId);
		
		JPanel controlPanel = new JPanel(new MigLayout("fill, insets 0"));
		controlPanel.add(instanceablePanel, "wrap, span 2, growx");
		controlPanel.add(botIdPanel, "wrap, growx, span 2");
		controlPanel.add(deleteRoleButton, "growx");
		controlPanel.add(clearRolesButton, "growx");
		
		add(scrollPaneActiveRoles);
		add(controlPanel);
		
		components.add(activeRolesList);
		components.add(instanceablePanel);
		components.add(clearRolesButton);
		components.add(deleteRoleButton);
		components.add(checkUseBotId);
		setEnabled(false);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param observer
	 */
	public void addObserver(final IRoleControlPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param oddObserver
	 */
	public void removeObserver(final IRoleControlPanelObserver oddObserver)
	{
		synchronized (observers)
		{
			observers.remove(oddObserver);
		}
	}
	
	
	@Override
	public void onAiModeChanged(final EAIControlState mode)
	{
		clearRoles();
		switch (mode)
		{
			case EMERGENCY_MODE:
			case MATCH_MODE:
			case MIXED_TEAM_MODE:
				setEnabled(false);
				activeRolesListModel.clear();
				break;
			case TEST_MODE:
				setEnabled(true);
				
				String roleStr = SumatraModel.getInstance().getUserProperty(ROLE_KEY);
				if (roleStr != null)
				{
					try
					{
						ERole role = ERole.valueOf(roleStr);
						instanceablePanel.setSelectedItem(role);
					} catch (IllegalArgumentException err)
					{
						// ignore
					}
				}
				
				textBotId.setEnabled(checkUseBotId.isSelected());
				break;
			default:
				break;
		
		}
	}
	
	
	/**
	 * @param roles
	 */
	public void setActiveRoles(final List<ARole> roles)
	{
		// Insert
		int i = 0;
		while ((activeRolesListModel.size() > i) && (roles.size() > i))
		{
			final ARole role = roles.get(i);
			if (!activeRolesListModel.get(i).equals(role))
			{
				activeRolesListModel.insertElementAt(role, i);
			}
			i++;
		}
		
		// Cut off
		final int modelSize = activeRolesListModel.size();
		final int diff = modelSize - roles.size();
		if (diff > 0)
		{
			activeRolesListModel.removeRange(modelSize - diff, modelSize - 1);
		} else if (diff < 0)
		{
			for (int j = i; j < roles.size(); j++)
			{
				activeRolesListModel.addElement(roles.get(j));
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- Actions --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private class BotIdChangeListener extends KeyAdapter
	{
		@Override
		public void keyReleased(final KeyEvent e)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					if (textBotId.getBackground() == ERROR_COLOR)
					{
						textBotId.setBackground(DEFAULT_COLOR);
					}
				}
			});
		}
	}
	
	
	private class UseBotIdListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			synchronized (observers)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						textBotId.setEnabled(checkUseBotId.isSelected());
					}
				});
			}
		}
	}
	
	
	private class ClearRolesListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			clearRoles();
		}
	}
	
	
	private void clearRoles()
	{
		for (int i = 0; i < activeRolesListModel.size(); i++)
		{
			ARole role = activeRolesListModel.elementAt(i);
			for (final IRoleControlPanelObserver o : observers)
			{
				o.removeRole(role);
			}
		}
		activeRolesListModel.clear();
	}
	
	
	private class DeleteRoleListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			ARole role = activeRolesList.getSelectedValue();
			if (role != null)
			{
				for (final IRoleControlPanelObserver o : observers)
				{
					o.removeRole(role);
				}
			}
		}
	}
	
	private class NewInstanceObserver implements IInstanceableObserver
	{
		@Override
		public void onNewInstance(final Object object)
		{
			synchronized (observers)
			{
				try
				{
					final boolean useId = checkUseBotId.isSelected();
					final ARole role = (ARole) object;
					SumatraModel.getInstance().setUserProperty(ROLE_KEY, role.getType().name());
					BotID botId = BotID.createBotId();
					if (useId)
					{
						botId = BotID.createBotId(Integer.parseInt(textBotId.getText()), ETeamColor.UNINITIALIZED);
					}
					for (final IRoleControlPanelObserver o : observers)
					{
						o.addRole(role, botId);
					}
				} catch (final NumberFormatException nfe)
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							textBotId.setBackground(ERROR_COLOR);
							// Don't do anything else
						}
					});
				}
			}
		}
	}
	
	
	@Override
	public void setEnabled(final boolean enabled)
	{
		super.setEnabled(enabled);
		for (Component comp : components)
		{
			comp.setEnabled(enabled);
		}
	}
}
