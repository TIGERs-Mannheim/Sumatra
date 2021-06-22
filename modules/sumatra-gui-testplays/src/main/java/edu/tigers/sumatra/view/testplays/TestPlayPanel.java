/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.view.testplays;

import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.testplays.ITestPlayDataObserver;
import edu.tigers.sumatra.testplays.TestPlayManager;
import edu.tigers.sumatra.testplays.commands.ACommand;
import edu.tigers.sumatra.testplays.commands.CommandList;
import edu.tigers.sumatra.testplays.commands.KickCommand;
import edu.tigers.sumatra.testplays.commands.PassCommand;
import edu.tigers.sumatra.testplays.commands.PathCommand;
import edu.tigers.sumatra.testplays.commands.ReceiveCommand;
import edu.tigers.sumatra.testplays.commands.RedirectCommand;
import edu.tigers.sumatra.testplays.commands.SynchronizeCommand;
import edu.tigers.sumatra.testplays.util.Point;
import edu.tigers.sumatra.view.testplays.panels.ACommandDetailsPanel;
import edu.tigers.sumatra.view.testplays.panels.CommandPanel;
import edu.tigers.sumatra.view.testplays.panels.KickCommandPanel;
import edu.tigers.sumatra.view.testplays.panels.PassCommandPanel;
import edu.tigers.sumatra.view.testplays.panels.PathCommandPanel;
import edu.tigers.sumatra.view.testplays.panels.ReceiveCommandPanel;
import edu.tigers.sumatra.view.testplays.panels.RedirectCommandPanel;
import edu.tigers.sumatra.view.testplays.panels.SyncCommandPanel;
import edu.tigers.sumatra.views.ISumatraView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class TestPlayPanel extends JPanel implements ISumatraView
{

	private static final String NO_ROLE_SELECTED = "You have to select a role first";

	private JList<CommandList> roleList;
	private JList<ACommand> commandList;
	private JComboBox<ACommand.CommandType> commandTypeChooser;
	private JFileChooser fileChooser;
	private CommandPanel commandPanel = new CommandPanel();
	private JTextField tfCommandListTitle;

	private final CommandListModel commandListModel = new CommandListModel();

	private static final Logger log = LogManager.getLogger(TestPlayManager.class.getName());


	/**
	 * Creates a new TestPlayPanel
	 */
	public TestPlayPanel()
	{

		setLayout(new BorderLayout());
		JPanel contentPanel = new JPanel(new GridBagLayout());

		GridBagConstraints gbc;

		JPanel commandQueuePanel = new JPanel(new BorderLayout());
		commandQueuePanel.setBorder(BorderFactory.createTitledBorder("Roles"));

		roleList = new JList<>();
		roleList.setModel(new RoleListModel());
		roleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		roleList.addListSelectionListener(new RoleSelectionListener());
		JScrollPane commandListScroll = new JScrollPane(roleList);
		commandQueuePanel.add(commandListScroll, BorderLayout.CENTER);

		JPanel commandListSouthContainer = new JPanel(new GridLayout(0, 1));

		JPanel commandListButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton btnDelCommandList = new JButton("Remove");
		btnDelCommandList.addActionListener(new RoleRemoveCommand());
		commandListButtonPanel.add(btnDelCommandList);

		JButton btnAddCommandList = new JButton("Add");
		btnAddCommandList.addActionListener(new RoleAddCommand());
		commandListButtonPanel.add(btnAddCommandList);

		commandListSouthContainer.add(commandListButtonPanel);

		JPanel commandListInputPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		tfCommandListTitle = new JTextField(8);
		commandListInputPanel.add(tfCommandListTitle);

		JButton btnRenameRole = new JButton("Rename");
		btnRenameRole.addActionListener(new RenameRoleCommand());
		commandListInputPanel.add(btnRenameRole);

		commandListSouthContainer.add(commandListInputPanel);
		commandQueuePanel.add(commandListSouthContainer, BorderLayout.SOUTH);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1;
		gbc.weightx = 1;
		contentPanel.add(commandQueuePanel, gbc);

		JPanel commandListPanel = new JPanel(new BorderLayout());
		commandListPanel.setBorder(BorderFactory.createTitledBorder("Commands"));

		commandList = new JList<>();
		commandList.setModel(commandListModel);
		commandList.addListSelectionListener(new CommandSelectionListener());
		commandList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane commandScroll = new JScrollPane(commandList);
		commandListPanel.add(commandScroll, BorderLayout.CENTER);

		JPanel commandButtonPanel = new JPanel(new GridLayout(0, 1));

		JPanel commandActionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton btnMoveCommandUp = new JButton("Up");
		btnMoveCommandUp.addActionListener(new CommandUpCommand());
		commandActionButtonPanel.add(btnMoveCommandUp);

		JButton btnMoveCommandDown = new JButton("Down");
		btnMoveCommandDown.addActionListener(new CommandDownCommand());
		commandActionButtonPanel.add(btnMoveCommandDown);

		commandButtonPanel.add(commandActionButtonPanel);

		JPanel commandCreationButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton btnDelCommand = new JButton("-");
		btnDelCommand.addActionListener(new CommandRemoveCommand());
		commandCreationButtonPanel.add(btnDelCommand);

		JButton btnAddCommand = new JButton("+");
		btnAddCommand.addActionListener(new CommandAddCommand());
		commandCreationButtonPanel.add(btnAddCommand);

		commandTypeChooser = new JComboBox<>(ACommand.CommandType.values());
		commandTypeChooser.setSelectedIndex(0);
		commandCreationButtonPanel.add(commandTypeChooser);

		commandButtonPanel.add(commandCreationButtonPanel);

		commandListPanel.add(commandButtonPanel, BorderLayout.SOUTH);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1;
		gbc.weightx = 1;
		contentPanel.add(commandListPanel, gbc);

		add(contentPanel, BorderLayout.WEST);

		add(commandPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		fileChooser = new JFileChooser(TestPlayManager.getPathFile());

		JButton btnReloadFile = new JButton("Reload from File");
		btnReloadFile.addActionListener(new ReloadFromFileCommand());
		buttonPanel.add(btnReloadFile);

		JButton btnSaveFile = new JButton("Save to file");
		btnSaveFile.addActionListener(new SaveToFileCommand());
		buttonPanel.add(btnSaveFile);

		JButton btnOpenFile = new JButton("Open File");
		btnOpenFile.addActionListener(new OpenFileCommand());
		buttonPanel.add(btnOpenFile);

		add(buttonPanel, BorderLayout.SOUTH);
	}


	private static class RoleListModel extends AbstractListModel<CommandList> implements ITestPlayDataObserver
	{

		public RoleListModel()
		{

			TestPlayManager.getInstance().addObserver(this);
		}


		@Override
		public int getSize()
		{
			return TestPlayManager.getInstance().getCommandQueue().size();
		}


		@Override
		public CommandList getElementAt(final int index)
		{
			return TestPlayManager.getInstance().getCommandQueue().get(index);
		}


		@Override
		public void onTestPlayDataUpdate()
		{

			fireContentsChanged(this, 0, getSize());
		}
	}

	private class CommandListModel extends AbstractListModel<ACommand>
	{

		@Override
		public int getSize()
		{
			TestPlayManager testPlayManager = TestPlayManager.getInstance();

			int index = roleList.getSelectedIndex();

			if (index == -1 || testPlayManager.getCommandQueue().isEmpty())
			{
				return 0;
			}

			return testPlayManager.getCommandQueue().get(index).getCommands().size();
		}


		@Override
		public ACommand getElementAt(final int index)
		{
			return TestPlayManager.getInstance().getCommandQueue().get(roleList.getSelectedIndex()).getCommands()
					.get(index);
		}


		/**
		 * Fire a contentChange event
		 */
		public void update()
		{

			fireContentsChanged(this, 0, getSize());
		}
	}

	class RoleSelectionListener implements ListSelectionListener
	{

		@Override
		public void valueChanged(final ListSelectionEvent e)
		{

			if (e.getValueIsAdjusting())
			{
				return;
			}

			commandListModel.update();
			commandList.clearSelection();
		}
	}

	class CommandSelectionListener implements ListSelectionListener
	{

		@Override
		public void valueChanged(final ListSelectionEvent e)
		{

			if (e.getValueIsAdjusting())
			{
				return;
			}

			if (commandList.getSelectedIndex() == -1)
			{
				commandPanel.clearCommandDetailsPanel();
				return;
			}

			ACommand selectedCommand = commandList.getSelectedValue();
			ACommandDetailsPanel detailsPanel;

			switch (selectedCommand.getCommandType())
			{
				case SYNCHRONIZE:
					detailsPanel = new SyncCommandPanel((SynchronizeCommand) selectedCommand);
					break;
				case PATH:
					detailsPanel = new PathCommandPanel((PathCommand) selectedCommand);
					break;
				case KICK:
					detailsPanel = new KickCommandPanel((KickCommand) selectedCommand);
					break;
				case PASS:
					detailsPanel = new PassCommandPanel((PassCommand) selectedCommand);
					break;
				case RECEIVE:
					detailsPanel = new ReceiveCommandPanel((ReceiveCommand) selectedCommand);
					break;
				case REDIRECT:
					detailsPanel = new RedirectCommandPanel((RedirectCommand) selectedCommand);
					break;
				default:
					return;
			}

			commandPanel.setCommandDetailsPanel(detailsPanel);

		}
	}

	private static class RoleAddCommand implements ActionListener
	{

		@Override
		public void actionPerformed(final ActionEvent e)
		{

			TestPlayManager.getInstance().addRole(new CommandList());
		}
	}

	class RoleRemoveCommand implements ActionListener
	{

		@Override
		public void actionPerformed(final ActionEvent e)
		{

			if (roleList.getSelectedIndex() != -1)
			{
				TestPlayManager.getInstance().removeRole(roleList.getSelectedValue());
			}
		}
	}

	class CommandAddCommand implements ActionListener
	{

		@Override
		public void actionPerformed(final ActionEvent e)
		{
			if (roleList.getSelectedIndex() == -1)
			{
				log.error(NO_ROLE_SELECTED);
				return;
			}

			ACommand command;
			final ACommand.CommandType selectedItem = (ACommand.CommandType) commandTypeChooser.getSelectedItem();
			if (selectedItem == null)
			{
				return;
			}
			switch (selectedItem)
			{
				case PATH:
					command = new PathCommand();
					break;
				case SYNCHRONIZE:
					command = new SynchronizeCommand();
					break;
				case KICK:
					command = new KickCommand(
							new Point(Geometry.getGoalTheir().getCenter().x(), Geometry.getGoalTheir().getCenter().y()), 2.0,
							EKickerDevice.STRAIGHT);
					break;
				case PASS:
					command = new PassCommand();
					break;
				case RECEIVE:
					command = new ReceiveCommand();
					break;
				case REDIRECT:
					command = new RedirectCommand();
					break;
				default:
					log.error("This command type is currently not supported.");
					return;
			}

			TestPlayManager.getInstance().getCommandQueue().get(roleList.getSelectedIndex()).add(command);
			commandListModel.update();
		}
	}

	class CommandRemoveCommand implements ActionListener
	{

		@Override
		public void actionPerformed(final ActionEvent e)
		{
			if (roleList.getSelectedIndex() == -1)
			{
				log.error(NO_ROLE_SELECTED);
				return;
			}

			TestPlayManager.getInstance().getCommandQueue().get(roleList.getSelectedIndex())
					.remove(commandList.getSelectedValue());
			commandListModel.update();
		}
	}

	class SaveToFileCommand implements ActionListener
	{

		@Override
		public void actionPerformed(final ActionEvent e)
		{

			int returnVal = fileChooser.showSaveDialog(TestPlayPanel.this);

			if (returnVal == JFileChooser.APPROVE_OPTION)
			{

				File file = fileChooser.getSelectedFile();
				TestPlayManager.setPathFile(file.getPath());

				try
				{
					TestPlayManager.getInstance().savePathToFile();
				} catch (IOException e1)
				{
					log.error("Error while saving file!", e1);
				}
			}
		}
	}

	class ReloadFromFileCommand implements ActionListener
	{

		@Override
		public void actionPerformed(final ActionEvent e)
		{

			TestPlayManager.getInstance().reloadFromFile();
			commandListModel.update();
		}
	}

	class OpenFileCommand implements ActionListener
	{

		@Override
		public void actionPerformed(final ActionEvent e)
		{

			int returnVal = fileChooser.showOpenDialog(TestPlayPanel.this);

			if (returnVal == JFileChooser.APPROVE_OPTION)
			{

				File file = fileChooser.getSelectedFile();
				TestPlayManager.setPathFile(file.getPath());

				TestPlayManager.getInstance().reloadFromFile();
				commandListModel.update();
			}
		}
	}

	private class CommandUpCommand implements ActionListener
	{


		@Override
		public void actionPerformed(final ActionEvent actionEvent)
		{
			int indexOld = commandList.getSelectedIndex();

			if (indexOld == 0)
			{
				return;
			}

			ACommand command = commandList.getSelectedValue();

			TestPlayManager.getInstance().getCommandQueue().get(roleList.getSelectedIndex()).getCommands()
					.remove(indexOld);
			TestPlayManager.getInstance().getCommandQueue().get(roleList.getSelectedIndex()).getCommands()
					.add(indexOld - 1, command);

			commandListModel.update();
			commandList.setSelectedIndex(indexOld - 1);
		}
	}

	private class CommandDownCommand implements ActionListener
	{

		@Override
		public void actionPerformed(final ActionEvent actionEvent)
		{
			int indexOld = commandList.getSelectedIndex();

			if (indexOld >= commandListModel.getSize() - 1)
			{
				return;
			}

			ACommand command = commandList.getSelectedValue();

			TestPlayManager.getInstance().getCommandQueue().get(roleList.getSelectedIndex()).getCommands()
					.remove(indexOld);
			TestPlayManager.getInstance().getCommandQueue().get(roleList.getSelectedIndex()).getCommands().add(
					indexOld + 1,
					command);

			commandListModel.update();
			commandList.setSelectedIndex(indexOld + 1);
		}
	}

	private class RenameRoleCommand implements ActionListener
	{

		@Override
		public void actionPerformed(final ActionEvent actionEvent)
		{
			if (roleList.getSelectedIndex() == -1)
			{
				log.error(NO_ROLE_SELECTED);
				return;
			}

			TestPlayManager.getInstance().getCommandQueue().get(roleList.getSelectedIndex())
					.setTitle(tfCommandListTitle.getText());
		}
	}
}
