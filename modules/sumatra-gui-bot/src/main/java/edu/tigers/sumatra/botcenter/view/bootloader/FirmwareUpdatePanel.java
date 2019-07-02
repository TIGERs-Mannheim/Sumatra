/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botcenter.view.bootloader;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import net.miginfocom.swing.MigLayout;


/**
 * Mass update all firmwares.
 *
 * @author AndreR
 */
public class FirmwareUpdatePanel extends JPanel
{
	/**  */
	private static final String FIRMWARE_FOLDER = ".firmwareFolder";


	/** Firmware Update Panel Observer. */
	public interface IFirmwareUpdatePanelObserver
	{
		/**
		 * @param folderPath
		 */
		void onSelectFirmwareFolder(String folderPath);


		/** Start firmware update. */
		void onStartFirmwareUpdate();
	}

	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long serialVersionUID = -1561564204669395366L;

	private final JPanel botContainer = new JPanel();
	private final JTextField firmwarePath = new JTextField();
	private final Map<BotID, FirmwareBotPanel> botPanels = new TreeMap<>();
	private final JFileChooser fileChooser = new JFileChooser();

	private final List<IFirmwareUpdatePanelObserver> observers = new CopyOnWriteArrayList<>();


	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------

	/** Constructor. */
	public FirmwareUpdatePanel()
	{
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		setBorder(BorderFactory.createTitledBorder("Update Firmware"));

		String mainCfgPath = SumatraModel.getInstance().getUserProperty(
				FirmwareUpdatePanel.class.getCanonicalName() + FIRMWARE_FOLDER);
		if (mainCfgPath != null)
		{
			firmwarePath.setText(mainCfgPath);
		}
		firmwarePath.setEditable(false);

		setLayout(new MigLayout("wrap 4", "[60]10[250,fill]10[50]10[50]"));

		botContainer.setLayout(new MigLayout(""));

		JButton selectMainButton = new JButton("Select");
		selectMainButton.addActionListener(new ChooseBinFile());
		JButton startButton = new JButton("Start");
		startButton.addActionListener(new StartUpdate());

		add(new JLabel("Firmware project folder:"));
		add(firmwarePath);
		add(selectMainButton);
		add(startButton);

		add(botContainer, "span 4");
	}


	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param observer
	 */
	public void addObserver(final IFirmwareUpdatePanelObserver observer)
	{
		observers.add(observer);
	}


	/**
	 * @param observer
	 */
	public void removeObserver(final IFirmwareUpdatePanelObserver observer)
	{
		observers.remove(observer);
	}


	private void addBotPanel(final FirmwareBotPanel panel)
	{
		botContainer.add(panel, "wrap, gapbottom 0");
		SwingUtilities.updateComponentTreeUI(FirmwareUpdatePanel.this);
	}


	/**
	 * Remove all bot panels.
	 */
	public void removeAllBotPanels()
	{
		botPanels.clear();

		EventQueue.invokeLater(() -> {
			botContainer.removeAll();
			botContainer.add(Box.createGlue(), "push");
			SwingUtilities.updateComponentTreeUI(FirmwareUpdatePanel.this);
		});
	}


	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	protected class ChooseBinFile implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			fileChooser.setCurrentDirectory(new File(SumatraModel.getInstance().getUserProperty(
					FirmwareUpdatePanel.class.getCanonicalName() + FIRMWARE_FOLDER), ""));
			int retVal = fileChooser.showOpenDialog(FirmwareUpdatePanel.this);

			if (retVal == JFileChooser.APPROVE_OPTION)
			{
				firmwarePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
				SumatraModel.getInstance().setUserProperty(
						FirmwareUpdatePanel.class.getCanonicalName() + FIRMWARE_FOLDER, firmwarePath.getText());

				notifySelectFirmwareFolder(fileChooser.getSelectedFile().getAbsolutePath());
			}
		}


		private void notifySelectFirmwareFolder(final String folder)
		{
			for (IFirmwareUpdatePanelObserver observer : observers)
			{
				observer.onSelectFirmwareFolder(folder);
			}
		}
	}

	/**
	 */
	protected class StartUpdate implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			notifyStartFirmwareUpdate();
		}


		private void notifyStartFirmwareUpdate()
		{
			for (IFirmwareUpdatePanelObserver observer : observers)
			{
				observer.onStartFirmwareUpdate();
			}
		}
	}


	/**
	 * @param botId
	 * @return
	 */
	public FirmwareBotPanel getOrCreateBotPanel(final BotID botId)
	{
		FirmwareBotPanel panel = botPanels.get(botId);
		if (panel == null)
		{
			panel = new FirmwareBotPanel();
			botPanels.put(botId, panel);
			addBotPanel(panel);
		}

		return panel;
	}
}
