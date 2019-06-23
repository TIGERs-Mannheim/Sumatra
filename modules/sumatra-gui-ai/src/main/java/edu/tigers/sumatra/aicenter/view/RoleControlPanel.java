/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.tigers.sumatra.aicenter.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.github.g3force.instanceables.IInstanceableObserver;
import com.github.g3force.instanceables.InstanceablePanel;

import edu.tigers.sumatra.ai.data.EAIControlState;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import net.miginfocom.swing.MigLayout;


/**
 * This panel can be used to control the actual bot-role assignment.
 * This must be a child panel of {@link AICenterPanel}.
 * 
 * @author Oliver, Malte, Gero
 */
public class RoleControlPanel extends JPanel
{
	private static Logger log = LogManager.getLogger(RoleControlPanel.class);
	
	private static final long serialVersionUID = -6902947971327765508L;
	
	private JList<ARole> activeRolesList = null;
	private DefaultListModel<ARole> activeRolesListModel = null;
	
	private final InstanceablePanel instanceablePanel;
	private final JButton clearRolesButton;
	private final JButton deleteRoleButton;
	
	private final List<JComponent> components = new ArrayList<>();
	
	private static final Color ERROR_COLOR = Color.RED;
	private static final Color DEFAULT_COLOR = Color.WHITE;
	private static final int LIST_SIZE_X = 250;
	private static final int LIST_SIZE_Y = 100;
	private final JSpinner textBotId;
	
	private final transient List<IRoleControlPanelObserver> observers = new LinkedList<>();
	
	private static final String ROLE_KEY = RoleControlPanel.class.getName() + ".role";
	
	
	/**
	 * Default constructor
	 */
	public RoleControlPanel()
	{
		setLayout(new MigLayout("insets 0", "[left][left]", "[top]"));
		
		activeRolesListModel = new DefaultListModel<>();
		activeRolesList = new JList<>(activeRolesListModel);
		activeRolesList.setPreferredSize(new Dimension(LIST_SIZE_X, LIST_SIZE_Y));
		activeRolesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		final JScrollPane scrollPaneActiveRoles = new JScrollPane();
		scrollPaneActiveRoles.getViewport().setView(activeRolesList);
		
		instanceablePanel = new InstanceablePanel(ERole.values(), SumatraModel.getInstance().getUserSettings());
		instanceablePanel.setShowCreate(false);
		instanceablePanel.addObserver(new NewInstanceObserver());
		
		JButton createRoleButton = new JButton("Create");
		createRoleButton.addActionListener(new CreateRoleListener());
		
		clearRolesButton = new JButton("clear");
		clearRolesButton.addActionListener(new ClearRolesListener());
		
		deleteRoleButton = new JButton("delete");
		deleteRoleButton.addActionListener(new DeleteRoleListener());
		
		String idStr = SumatraModel.getInstance().getUserProperty(RoleControlPanel.class.getCanonicalName() + ".id", "0");
		int id = Integer.parseInt(idStr);
		SpinnerModel botIdSpinnerModel = new SpinnerNumberModel(id, 0, 11, 1);
		textBotId = new JSpinner(botIdSpinnerModel);
		textBotId.addKeyListener(new BotIdChangeListener());
		
		JPanel controlPanel = new JPanel(new MigLayout("fill, insets 0"));
		controlPanel.add(instanceablePanel, "wrap, span 4");
		controlPanel.add(textBotId);
		controlPanel.add(createRoleButton);
		controlPanel.add(deleteRoleButton);
		controlPanel.add(clearRolesButton);
		
		add(scrollPaneActiveRoles);
		add(controlPanel);
		
		components.add(createRoleButton);
		components.add(activeRolesList);
		components.add(instanceablePanel);
		components.add(clearRolesButton);
		components.add(deleteRoleButton);
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
	
	
	public void setAiControlState(final EAIControlState mode)
	{
		clearRoles();
		
		if (mode == EAIControlState.TEST_MODE)
		{
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
					log.error(err);
				}
			}
		} else
		{
			setEnabled(false);
			activeRolesListModel.clear();
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
			EventQueue.invokeLater(() -> {
				if (textBotId.getBackground() == ERROR_COLOR)
				{
					textBotId.setBackground(DEFAULT_COLOR);
				}
			});
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
	
	private class CreateRoleListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			instanceablePanel.createInstance();
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
					final ARole role = (ARole) object;
					SumatraModel.getInstance().setUserProperty(ROLE_KEY, instanceablePanel.getSelectedItem().name());
					BotID botId = BotID.createBotId((int) textBotId.getValue(), ETeamColor.UNINITIALIZED);
					SumatraModel.getInstance().setUserProperty(RoleControlPanel.class.getCanonicalName() + ".id",
							String.valueOf(botId.getNumber()));
					for (final IRoleControlPanelObserver o : observers)
					{
						o.addRole(role, botId);
					}
				} catch (final NumberFormatException nfe)
				{
					// Don't do anything else
					EventQueue.invokeLater(() -> textBotId.setBackground(ERROR_COLOR));
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
