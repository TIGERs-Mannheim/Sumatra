/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.aicenter.view;

import com.github.g3force.instanceables.IInstanceableObserver;
import com.github.g3force.instanceables.InstanceablePanel;
import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.model.SumatraModel;
import lombok.extern.log4j.Log4j2;
import net.miginfocom.swing.MigLayout;

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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * This panel can be used to control the actual bot-role assignment.
 * This must be a child panel of {@link AICenterPanel}.
 */
@Log4j2
public class RoleControlPanel extends JPanel
{
	private static final long serialVersionUID = -6902947971327765508L;
	private static final Color ERROR_COLOR = Color.RED;
	private static final Color DEFAULT_COLOR = Color.WHITE;
	private static final int LIST_SIZE_X = 250;
	private static final int LIST_SIZE_Y = 100;

	private final JList<ARole> activeRolesList;
	private final DefaultListModel<ARole> activeRolesListModel;
	private final InstanceablePanel instanceablePanel;

	private final List<JComponent> components = new ArrayList<>();

	private final JSpinner textBotId;

	private final List<IRoleControlPanelObserver> observers = new CopyOnWriteArrayList<>();


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

		final JButton clearRolesButton = new JButton("clear");
		clearRolesButton.addActionListener(new ClearRolesListener());

		final JButton deleteRoleButton = new JButton("delete");
		deleteRoleButton.addActionListener(new DeleteRoleListener());

		String idStr = SumatraModel.getInstance().getUserProperty(RoleControlPanel.class.getCanonicalName() + ".id", "0");
		int id = Integer.parseInt(idStr);
		SpinnerModel botIdSpinnerModel = new SpinnerNumberModel(id, 0, AObjectID.BOT_ID_MAX, 1);
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


	/**
	 * @param observer
	 */
	public void addObserver(final IRoleControlPanelObserver observer)
	{
		observers.add(observer);
	}


	/**
	 * @param oddObserver
	 */
	public void removeObserver(final IRoleControlPanelObserver oddObserver)
	{
		observers.remove(oddObserver);
	}


	public void setAiControlState(final EAIControlState mode)
	{
		if (mode == EAIControlState.TEST_MODE)
		{
			setEnabled(true);
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
			for (final IRoleControlPanelObserver o : observers)
			{
				o.clearRoles();
			}
		}
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
			try
			{
				final ARole role = (ARole) object;
				int botId = (int) textBotId.getValue();
				SumatraModel.getInstance().setUserProperty(RoleControlPanel.class.getCanonicalName() + ".id",
						String.valueOf(botId));
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
