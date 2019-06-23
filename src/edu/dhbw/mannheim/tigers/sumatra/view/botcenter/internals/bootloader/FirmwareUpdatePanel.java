/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.06.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bootloader;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;


/**
 * Mass update all firmwares.
 * 
 * @author AndreR
 */
public class FirmwareUpdatePanel extends JPanel
{
	/** */
	public interface IFirmwareUpdatePanelObserver
	{
		/**
		 * @param folderPath
		 */
		void onSelectFirmwareFolder(String folderPath);
		
		
		/** */
		void onStartFirmwareUpdate();
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long									serialVersionUID	= -1561564204669395366L;
	
	private final JPanel											botContainer		= new JPanel();
	private final JTextField									firmwarePath		= new JTextField();
	private final Map<BotID, FirmwareBotPanel>			botPanels			= new TreeMap<BotID, FirmwareBotPanel>();
	private final JFileChooser									fileChooser			= new JFileChooser();
	
	private final List<IFirmwareUpdatePanelObserver>	observers			= new ArrayList<IFirmwareUpdatePanelObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/** */
	public FirmwareUpdatePanel()
	{
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		// fileChooser.setFileFilter(new BinFilter());
		
		String mainCfgPath = SumatraModel.getInstance().getUserProperty(
				FirmwareUpdatePanel.class.getCanonicalName() + ".firmwareFolder");
		if (mainCfgPath != null)
		{
			firmwarePath.setText(mainCfgPath);
		}
		
		setLayout(new MigLayout("wrap 4", "[60]10[250,fill]10[50]10[50]"));
		
		botContainer.setLayout(new MigLayout(""));
		
		JButton selectMainButton = new JButton("Select");
		selectMainButton.addActionListener(new ChooseBinFile());
		JButton startButton = new JButton("Start");
		startButton.addActionListener(new StartUpdate());
		
		add(new JLabel("Main:"));
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
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IFirmwareUpdatePanelObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifyStartFirmwareUpdate()
	{
		synchronized (observers)
		{
			for (IFirmwareUpdatePanelObserver observer : observers)
			{
				observer.onStartFirmwareUpdate();
			}
		}
	}
	
	
	private void notifySelectFirmwareFolder(final String folder)
	{
		for (IFirmwareUpdatePanelObserver observer : observers)
		{
			observer.onSelectFirmwareFolder(folder);
		}
	}
	
	
	private void addBotPanel(final FirmwareBotPanel panel)
	{
		botContainer.add(panel, "wrap, gapbottom 0");
		SwingUtilities.updateComponentTreeUI(FirmwareUpdatePanel.this);
	}
	
	
	/**
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
		/** */
		public ChooseBinFile()
		{
		}
		
		
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			fileChooser.setCurrentDirectory(new File(SumatraModel.getInstance().getUserProperty(
					FirmwareUpdatePanel.class.getCanonicalName() + ".firmwareFolder"), ""));
			int retVal = fileChooser.showOpenDialog(FirmwareUpdatePanel.this);
			
			if (retVal == JFileChooser.APPROVE_OPTION)
			{
				firmwarePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
				SumatraModel.getInstance().setUserProperty(
						FirmwareUpdatePanel.class.getCanonicalName() + ".firmwareFolder", firmwarePath.getText());
				
				notifySelectFirmwareFolder(fileChooser.getSelectedFile().getAbsolutePath());
			}
		}
	}
	
	/**
	 */
	protected class StartUpdate implements ActionListener
	{
		/** */
		public StartUpdate()
		{
		}
		
		
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			notifyStartFirmwareUpdate();
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
