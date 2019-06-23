/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.RoleFactory;


/**
 * This panel can be used to control the actual bot-role assignment.
 * This must be a child panel of {@link ModuleControlPanel}.
 * 
 * @author Oliver, Malte, Gero
 * 
 */
public class RoleControlPanel extends JPanel implements IChangeGUIMode
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long								serialVersionUID	= -6902947971327765508L;
	
	private final JComboBox<ERole>						roleComboBox;
	
	private final JButton									addRoleButton;
	private final JButton									clearRolesButton;
	
	private final JCheckBox									checkUseBotId;
	private static final Color								ERROR_COLOR			= Color.RED;
	private static final Color								DEFAULT_COLOR		= Color.WHITE;
	private final JTextField								textBotId;
	
	private final List<IRoleControlPanelObserver>	observers			= new LinkedList<IRoleControlPanelObserver>();
	
	private static final String							ROLE_KEY				= RoleControlPanel.class.getName() + ".role";
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	  * 
	  */
	public RoleControlPanel()
	{
		setLayout(new MigLayout("fillx", "[]push"));
		setBorder(BorderFactory.createTitledBorder("Role Control Panel"));
		
		
		roleComboBox = new JComboBox<ERole>(ERole.values());
		
		addRoleButton = new JButton("add Role assignment");
		addRoleButton.addActionListener(new AddRoleListener());
		
		clearRolesButton = new JButton("clear all Roles");
		clearRolesButton.addActionListener(new ClearRolesListener());
		
		final JPanel botIdPanel = new JPanel();
		checkUseBotId = new JCheckBox();
		checkUseBotId.setSelected(true);
		checkUseBotId.addActionListener(new UseBotIdListener());
		
		textBotId = new JTextField(3);
		textBotId.addKeyListener(new BotIdChangeListener());
		
		botIdPanel.add(checkUseBotId);
		botIdPanel.add(textBotId);
		
		
		add(new JLabel("Role:"), "wrap, growx");
		add(roleComboBox, "wrap, growx");
		
		add(botIdPanel, "wrap, growx");
		add(addRoleButton, "wrap, growx");
		
		add(clearRolesButton, "growx");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param observer
	 */
	public void addObserver(IRoleControlPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param oddObserver
	 */
	public void removeObserver(IRoleControlPanelObserver oddObserver)
	{
		synchronized (observers)
		{
			observers.remove(oddObserver);
		}
	}
	
	
	@Override
	public void setPlayTestMode()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				setEnabled(false);
				
				roleComboBox.setEnabled(false);
				addRoleButton.setEnabled(false);
				clearRolesButton.setEnabled(false);
				
				textBotId.setEnabled(false);
				checkUseBotId.setEnabled(false);
			}
		});
	}
	
	
	@Override
	public void setRoleTestMode()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				setEnabled(true);
				
				List<ERole> availRoles = RoleFactory.getAvailableRoles();
				roleComboBox.setModel(new DefaultComboBoxModel<ERole>(availRoles.toArray(new ERole[availRoles.size()])));
				String roleStr = SumatraModel.getInstance().getUserProperty(ROLE_KEY);
				if (roleStr != null)
				{
					try
					{
						ERole role = ERole.valueOf(roleStr);
						roleComboBox.setSelectedItem(role);
					} catch (IllegalArgumentException err)
					{
						// ignore
					}
				}
				
				roleComboBox.setEnabled(true);
				addRoleButton.setEnabled(true);
				clearRolesButton.setEnabled(true);
				
				textBotId.setEnabled(true);
				checkUseBotId.setEnabled(true);
			}
		});
	}
	
	
	@Override
	public void setMatchMode()
	{
		disablePanel();
	}
	
	
	@Override
	public void setEmergencyMode()
	{
		disablePanel();
	}
	
	
	@Override
	public void onStart()
	{
	}
	
	
	@Override
	public void onStop()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				disablePanel();
				textBotId.setText("");
				textBotId.setBackground(DEFAULT_COLOR);
			}
		});
	}
	
	
	private void disablePanel()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				setEnabled(false);
				
				roleComboBox.setEnabled(false);
				addRoleButton.setEnabled(false);
				clearRolesButton.setEnabled(false);
				
				textBotId.setEnabled(false);
				checkUseBotId.setEnabled(false);
			}
		});
	}
	
	
	// --------------------------------------------------------------------------
	// --- Actions --------------------------------------------------------------
	// --------------------------------------------------------------------------
	private class AddRoleListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			synchronized (observers)
			{
				try
				{
					final boolean useId = checkUseBotId.isSelected();
					final ERole role = (ERole) roleComboBox.getSelectedItem();
					SumatraModel.getInstance().setUserProperty(ROLE_KEY, role.name());
					if (useId)
					{
						final BotID botId = new BotID(Integer.parseInt(textBotId.getText()));
						for (final IRoleControlPanelObserver o : observers)
						{
							o.addRole(role, botId);
						}
					} else
					{
						for (final IRoleControlPanelObserver o : observers)
						{
							o.addRole(role);
						}
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
	
	
	private class BotIdChangeListener extends KeyAdapter
	{
		@Override
		public void keyReleased(KeyEvent e)
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
		public void actionPerformed(ActionEvent e)
		{
			synchronized (observers)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						textBotId.setEnabled(!textBotId.isEnabled());
					}
				});
			}
		}
	}
	
	
	private class ClearRolesListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			synchronized (observers)
			{
				for (final IRoleControlPanelObserver o : observers)
				{
					o.clearRoles();
				}
			}
		}
	}
}
