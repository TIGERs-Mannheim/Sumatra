/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.11.2011
 * Author(s): Sven Frank
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.rcm;

import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import net.java.games.input.Controller;
import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.presenter.rcm.AControllerPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.rcm.RCMPresenter;
import edu.dhbw.mannheim.tigers.sumatra.view.rcm.actions.ButtonSelectAction;
import edu.dhbw.mannheim.tigers.sumatra.view.rcm.actions.DefaultConfigAction;
import edu.dhbw.mannheim.tigers.sumatra.view.rcm.actions.LoadConfigAction;
import edu.dhbw.mannheim.tigers.sumatra.view.rcm.actions.SaveConfigAction;


/**
 * - Main panel of the module
 * 
 * @author Sven Frank
 * 
 */
public class ControllerPanel extends JPanel
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger				log					= Logger.getLogger(ControllerPanel.class.getName());
	
	private static final long					serialVersionUID	= -4425620303404436160L;
	
	// --- GUI Components ---
	private JPanel									robotChoicePanel;
	private JPanel									movementPanel;
	private JPanel									activityPanel;
	private JTextField							leftTextField;
	private JButton								defaultConfigButton;
	private JButton								loadConfigButton;
	private JButton								saveConfigButton;
	private JPanel									configManagePanel;
	private JLabel									leftLabel;
	private JTextField							backwardTextField;
	private JLabel									backwardLabel;
	private JTextField							forwardTextField;
	private JLabel									forwardLabel;
	private JTextField							forceTextField;
	private JTextField							armTextField;
	private JLabel									armLabel;
	private JTextField							disarmTextField;
	private JLabel									disarmLabel;
	private JTextField							dribbleTextField;
	private JLabel									dribbleLabel;
	private JTextField							rotateRightTextField;
	private JLabel									rotateRightLabel;
	private JTextField							rotateLeftTextField;
	private JLabel									rotateLeftLabel;
	private JTextField							chipKickTextField;
	private JLabel									chipKickLabel;
	private JTextField							chipArmTextField;
	private JLabel									chipArmLabel;
	private JLabel									forceLabel;
	private JTextField							passTextField;
	private JLabel									passLabel;
	private JTextField							rightTextField;
	private JLabel									rightLabel;
	private JLabel									botNumberLabel;
	/** */
	public JComboBox<String>					botComboBox;
	
	/**
	 * HashMap with all TextFields
	 * Key : Action (TextField Name)
	 * Value : TextField
	 */
	private final Map<String, JTextField>	tFMap					= new HashMap<String, JTextField>();
	
	// --- Controller Information
	private final Controller					controller;
	private final AControllerPresenter		controllerPresenter;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param newController
	 * @param newControllerPresenter
	 */
	public ControllerPanel(Controller newController, AControllerPresenter newControllerPresenter)
	{
		super();
		controller = newController;
		controllerPresenter = newControllerPresenter;
		initGUI();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	private void initGUI()
	{
		try
		{
			// --- set general preferences for controllerPanel ---
			final MigLayout thisLayout = new MigLayout();
			thisLayout.setRowConstraints("[135.0][fill]");
			thisLayout.setColumnConstraints("[297.0]");
			setLayout(thisLayout);
			setPreferredSize(new java.awt.Dimension(541, 291));
			{
				// --- robot choice panel with server and port ---
				robotChoicePanel = new JPanel();
				final MigLayout robotChoicePanelLayout = new MigLayout();
				robotChoicePanelLayout.setRowConstraints("30.0[25.0][25.0]");
				robotChoicePanelLayout.setColumnConstraints("[89.0][121.0]");
				robotChoicePanel.setLayout(robotChoicePanelLayout);
				this.add(robotChoicePanel, "cell 0 0");
				robotChoicePanel.setPreferredSize(new java.awt.Dimension(297, 199));
				robotChoicePanel.setBorder(BorderFactory.createTitledBorder("Connection Information"));
				{
					botNumberLabel = new JLabel();
					robotChoicePanel.add(botNumberLabel, "cell 0 0");
					botNumberLabel.setText("Botnumber:");
				}
				{
					// --- choose Bot ---
					botComboBox = new JComboBox<String>();
					botComboBox.setEditable(false);
					botComboBox.setPreferredSize(new Dimension(139, 20));
					robotChoicePanel.add(botComboBox, "cell 1 0");
					
					botComboBox.addPopupMenuListener(new PopupMenuListener()
					{
						
						@Override
						public void popupMenuWillBecomeVisible(PopupMenuEvent arg0)
						{
							botComboBox.removeAllItems();
							botComboBox.addItem("disabled");
							
							for (final ABot bot : RCMPresenter.getInstance().getAllBots())
							{
								final String bn = bot.getBotID().getNumber() + " - " + bot.getType();
								botComboBox.addItem(bn);
							}
						}
						
						
						@Override
						public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0)
						{
						}
						
						
						@Override
						public void popupMenuCanceled(PopupMenuEvent arg0)
						{
						}
					});
				}
			}
			{
				// --- panel with all movement commands ---
				movementPanel = new JPanel();
				final MigLayout movementPanelLayout = new MigLayout();
				movementPanelLayout.setRowConstraints("[fill][fill][fill][]");
				movementPanelLayout.setColumnConstraints("[70.0][80.0]");
				movementPanel.setLayout(movementPanelLayout);
				this.add(movementPanel, "cell 0 1");
				movementPanel.setPreferredSize(new java.awt.Dimension(262, 133));
				{
					forwardLabel = new JLabel();
					movementPanel.add(forwardLabel, "cell 0 0");
					forwardLabel.setText("Forward: ");
				}
				{
					/*
					 * just comment one ActionTextfield completly
					 * for all similar
					 */
					
					// --- forward action ---
					forwardTextField = new JTextField();
					// --- add Textfield to Panel first ---
					movementPanel.add(forwardTextField, "cell 1 0");
					// --- set Size ---
					forwardTextField.setMinimumSize(new java.awt.Dimension(87, 20));
					// --- TextField name = Action ---
					forwardTextField.setName("forward");
					// --- mouseListener to set configuration ---
					forwardTextField.addMouseListener(new ButtonSelectAction(forwardTextField, controller,
							controllerPresenter));
					// --- not editable by user ---
					forwardTextField.setEditable(false);
					// --- reset color to white (setEditable(false) turns textfield grey) ---
					forwardTextField.setBackground(Color.white);
					// --- put Action and Textfield on HashMap ---
					tFMap.put(forwardTextField.getName(), forwardTextField);
				}
				{
					backwardLabel = new JLabel();
					movementPanel.add(backwardLabel, "cell 0 1");
					backwardLabel.setText("Backwards: ");
				}
				{
					// --- backward action ---
					backwardTextField = new JTextField();
					movementPanel.add(backwardTextField, "cell 1 1");
					backwardTextField.setPreferredSize(new java.awt.Dimension(85, 20));
					backwardTextField.setName("backward");
					backwardTextField.addMouseListener(new ButtonSelectAction(backwardTextField, controller,
							controllerPresenter));
					backwardTextField.setEditable(false);
					backwardTextField.setBackground(Color.white);
					tFMap.put(backwardTextField.getName(), backwardTextField);
				}
				{
					leftLabel = new JLabel();
					movementPanel.add(leftLabel, "cell 0 2");
					leftLabel.setText("Left: ");
				}
				{
					// --- left action ---
					leftTextField = new JTextField();
					movementPanel.add(leftTextField, "cell 1 2");
					leftTextField.setPreferredSize(new java.awt.Dimension(85, 20));
					leftTextField.setName("left");
					leftTextField.addMouseListener(new ButtonSelectAction(leftTextField, controller, controllerPresenter));
					leftTextField.setEditable(false);
					leftTextField.setBackground(Color.white);
					tFMap.put(leftTextField.getName(), leftTextField);
				}
				{
					rightLabel = new JLabel();
					movementPanel.add(rightLabel, "cell 0 3");
					rightLabel.setText("Right:");
				}
				{
					// --- right action ---
					rightTextField = new JTextField();
					movementPanel.add(rightTextField, "cell 1 3");
					rightTextField.setPreferredSize(new java.awt.Dimension(86, 20));
					rightTextField.setName("right");
					rightTextField.addMouseListener(new ButtonSelectAction(rightTextField, controller, controllerPresenter));
					rightTextField.setEditable(false);
					rightTextField.setBackground(Color.white);
					tFMap.put(rightTextField.getName(), rightTextField);
				}
				{
					rotateLeftLabel = new JLabel();
					movementPanel.add(rotateLeftLabel, "cell 2 0");
					rotateLeftLabel.setText("Rotate Left:");
				}
				{
					// --- rotate left action ---
					rotateLeftTextField = new JTextField();
					movementPanel.add(rotateLeftTextField, "cell 3 0");
					rotateLeftTextField.setMinimumSize(new java.awt.Dimension(81, 20));
					rotateLeftTextField.setName("rotateLeft");
					rotateLeftTextField.addMouseListener(new ButtonSelectAction(rotateLeftTextField, controller,
							controllerPresenter));
					rotateLeftTextField.setEditable(false);
					rotateLeftTextField.setBackground(Color.white);
					tFMap.put(rotateLeftTextField.getName(), rotateLeftTextField);
				}
				{
					rotateRightLabel = new JLabel();
					movementPanel.add(rotateRightLabel, "cell 2 1");
					rotateRightLabel.setText("Rotate Right: ");
				}
				{
					// --- rotate right action ---
					rotateRightTextField = new JTextField();
					movementPanel.add(rotateRightTextField, "cell 3 1");
					rotateRightTextField.setPreferredSize(new java.awt.Dimension(83, 20));
					rotateRightTextField.setName("rotateRight");
					rotateRightTextField.addMouseListener(new ButtonSelectAction(rotateRightTextField, controller,
							controllerPresenter));
					rotateRightTextField.setEditable(false);
					rotateRightTextField.setBackground(Color.white);
					tFMap.put(rotateRightTextField.getName(), rotateRightTextField);
				}
			}
			{
				// --- panel with all activity commands ---
				activityPanel = new JPanel();
				final MigLayout actionPanelLayout = new MigLayout();
				actionPanelLayout.setColumnConstraints("[70.0][80.0]");
				actionPanelLayout.setRowConstraints("[][][]");
				activityPanel.setLayout(actionPanelLayout);
				this.add(activityPanel, "cell 1 1");
				activityPanel.setPreferredSize(new java.awt.Dimension(261, 135));
				{
					passLabel = new JLabel();
					activityPanel.add(passLabel, "cell 0 0");
					passLabel.setText("Pass:");
				}
				{
					// --- pass action ---
					passTextField = new JTextField();
					activityPanel.add(passTextField, "cell 1 0");
					passTextField.setPreferredSize(new java.awt.Dimension(87, 20));
					passTextField.setName("pass");
					passTextField.addMouseListener(new ButtonSelectAction(passTextField, controller, controllerPresenter));
					passTextField.setEditable(false);
					passTextField.setBackground(Color.white);
					tFMap.put(passTextField.getName(), passTextField);
				}
				{
					forceLabel = new JLabel();
					activityPanel.add(forceLabel, "cell 0 1");
					forceLabel.setText("Force" + ": ");
				}
				{
					// --- force action ---
					forceTextField = new JTextField();
					activityPanel.add(forceTextField, "cell 1 1");
					forceTextField.setPreferredSize(new java.awt.Dimension(85, 20));
					forceTextField.setName("force");
					forceTextField.addMouseListener(new ButtonSelectAction(forceTextField, controller, controllerPresenter));
					forceTextField.setEditable(false);
					forceTextField.setBackground(Color.white);
					tFMap.put(forceTextField.getName(), forceTextField);
				}
				{
					chipKickLabel = new JLabel();
					activityPanel.add(chipKickLabel, "cell 0 2");
					chipKickLabel.setText("Chip Kick: ");
				}
				{
					// --- chip kick action ---
					chipKickTextField = new JTextField();
					activityPanel.add(chipKickTextField, "cell 1 2");
					chipKickTextField.setPreferredSize(new java.awt.Dimension(90, 20));
					chipKickTextField.setName("chipKick");
					chipKickTextField.addMouseListener(new ButtonSelectAction(chipKickTextField, controller,
							controllerPresenter));
					chipKickTextField.setEditable(false);
					chipKickTextField.setBackground(Color.white);
					tFMap.put(chipKickTextField.getName(), chipKickTextField);
				}
				{
					dribbleLabel = new JLabel();
					activityPanel.add(dribbleLabel, "cell 0 3");
					dribbleLabel.setText("Dribble: ");
				}
				{
					// --- dribble action ---
					dribbleTextField = new JTextField();
					activityPanel.add(dribbleTextField, "cell 1 3");
					dribbleTextField.setPreferredSize(new java.awt.Dimension(85, 20));
					dribbleTextField.setName("dribble");
					dribbleTextField.addMouseListener(new ButtonSelectAction(dribbleTextField, controller,
							controllerPresenter));
					dribbleTextField.setEditable(false);
					dribbleTextField.setBackground(Color.white);
					tFMap.put(dribbleTextField.getName(), dribbleTextField);
				}
				{
					armLabel = new JLabel();
					activityPanel.add(armLabel, "cell 0 4");
					armLabel.setText("Arm: ");
				}
				{
					// --- arm action ---
					armTextField = new JTextField();
					activityPanel.add(armTextField, "cell 1 4");
					armTextField.setPreferredSize(new java.awt.Dimension(83, 20));
					armTextField.setName("arm");
					armTextField.addMouseListener(new ButtonSelectAction(armTextField, controller, controllerPresenter));
					armTextField.setEditable(false);
					armTextField.setBackground(Color.white);
					tFMap.put(armTextField.getName(), armTextField);
				}
				{
					disarmLabel = new JLabel();
					activityPanel.add(disarmLabel, "cell 0 5");
					disarmLabel.setText("Disarm: ");
				}
				{
					// --- arm action ---
					disarmTextField = new JTextField();
					activityPanel.add(disarmTextField, "cell 1 5");
					disarmTextField.setPreferredSize(new java.awt.Dimension(83, 20));
					disarmTextField.setName("disarm");
					disarmTextField
							.addMouseListener(new ButtonSelectAction(disarmTextField, controller, controllerPresenter));
					disarmTextField.setEditable(false);
					disarmTextField.setBackground(Color.white);
					tFMap.put(disarmTextField.getName(), disarmTextField);
				}
				{
					chipArmLabel = new JLabel();
					activityPanel.add(chipArmLabel, "cell 0 6");
					chipArmLabel.setText("Chip Arm: ");
				}
				{
					// --- chip kick action ---
					chipArmTextField = new JTextField();
					activityPanel.add(chipArmTextField, "cell 1 6");
					chipArmTextField.setPreferredSize(new java.awt.Dimension(90, 20));
					chipArmTextField.setName("chipArm");
					chipArmTextField.addMouseListener(new ButtonSelectAction(chipArmTextField, controller,
							controllerPresenter));
					chipArmTextField.setEditable(false);
					chipArmTextField.setBackground(Color.white);
					tFMap.put(chipArmTextField.getName(), chipArmTextField);
				}
			}
			{
				// --- panel to manage configurations ---
				configManagePanel = new JPanel();
				final MigLayout configManagePanelLayout = new MigLayout();
				configManagePanelLayout.setColumnConstraints("[60.0][64.0][75.0]");
				configManagePanel.setLayout(configManagePanelLayout);
				this.add(configManagePanel, "cell 1 0");
				configManagePanel.setPreferredSize(new java.awt.Dimension(261, 35));
				configManagePanel.setBorder(BorderFactory.createTitledBorder("Config"));
				{
					// --- save configuration ---
					saveConfigButton = new JButton();
					// --- add save action ---
					saveConfigButton.addActionListener(new SaveConfigAction(this, controllerPresenter));
					configManagePanel.add(saveConfigButton, "cell 0 0");
					saveConfigButton.setText("Save");
				}
				{
					// --- load configuration ---
					loadConfigButton = new JButton();
					// --- add load action ---
					loadConfigButton.addActionListener(new LoadConfigAction(this, controllerPresenter));
					configManagePanel.add(loadConfigButton, "cell 0 0");
					loadConfigButton.setText("Load");
				}
				{
					// --- default configuration ---
					defaultConfigButton = new JButton();
					// --- add default action ---
					defaultConfigButton.addActionListener(new DefaultConfigAction(controllerPresenter));
					configManagePanel.add(defaultConfigButton, "cell 0 0");
					defaultConfigButton.setText("Default");
				}
			}
		} catch (final Exception e)
		{
			log.error("Exception", e);
		}
	}
	
	
	/**
	 * set loaded or default configuration to TextFields
	 * @param conf HashMap(ComponentString, ActionString)
	 */
	public void showConfig(Map<String, String> conf)
	{
		for (Map.Entry<String, String> entry : conf.entrySet())
		{
			// --- put component string in textfield ---
			JTextField field = tFMap.get(entry.getValue());
			if (field == null)
			{
				log.warn("Textfield " + entry.getValue() + " not found");
				continue;
			}
			field.setText(entry.getKey());
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * returns the HashMap with all Actions & TextFields
	 * @return TextField HashMap<String, JTextField>
	 */
	public Map<String, JTextField> getTextFieldMap()
	{
		return tFMap;
	}
	
	
	/**
	 * returns server address string given by user
	 * @return ServerAddress
	 */
	public String getServerAdress()
	{
		return "127.0.0.1";
	}
	
	
	/**
	 * returns bot number
	 * @return bot number
	 */
	public int getBotNumber()
	{
		if (botComboBox.getSelectedItem() != null)
		{
			final String tempStore = botComboBox.getSelectedItem().toString();
			try
			{
				return Integer.parseInt(tempStore.substring(0, 2).trim());
			} catch (final NumberFormatException e)
			{
				log.warn("Could not read bot id: " + tempStore);
				return BotID.UNINITIALIZED_ID;
			}
		}
		return BotID.UNINITIALIZED_ID;
		
	}
}
