/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.06.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bootloader;

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
import javax.swing.filechooser.FileFilter;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;


/**
 * Mass update all firmwares.
 * 
 * @author AndreR
 * 
 */
public class FirmwareUpdatePanel extends JPanel
{
	/** */
	public interface IFirmwareUpdatePanelObserver
	{
		/**
		 * 
		 * @param filePath
		 * @param main
		 */
		void onStartFirmwareUpdate(String filePath, boolean main);
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long									serialVersionUID	= -1561564204669395366L;
	
	private final JPanel											botContainer		= new JPanel();
	private final JTextField									mainFirmwarePath	= new JTextField();
	private final JTextField									mediaFirmwarePath	= new JTextField();
	private final Map<BotID, JPanel>							botPanels			= new TreeMap<BotID, JPanel>();
	private final JFileChooser									fileChooser			= new JFileChooser();
	
	private final List<IFirmwareUpdatePanelObserver>	observers			= new ArrayList<IFirmwareUpdatePanelObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public FirmwareUpdatePanel()
	{
		fileChooser.setFileFilter(new BinFilter());
		
		String mainCfgPath = SumatraModel.getInstance().getUserProperty("mainFirmwareFile");
		if (mainCfgPath != null)
		{
			mainFirmwarePath.setText(mainCfgPath);
		}
		String mediaCfgPath = SumatraModel.getInstance().getUserProperty("mediaFirmwareFile");
		if (mediaCfgPath != null)
		{
			mediaFirmwarePath.setText(mediaCfgPath);
		}
		
		setLayout(new MigLayout("wrap 4", "[60]10[250,fill]10[50]10[50]"));
		
		botContainer.setLayout(new MigLayout(""));
		
		JButton selectMainButton = new JButton("Select");
		selectMainButton.addActionListener(new ChooseBinFile(true));
		JButton selectMediaButton = new JButton("Select");
		selectMediaButton.addActionListener(new ChooseBinFile(false));
		JButton startMainButton = new JButton("Start");
		startMainButton.addActionListener(new StartUpdate(true));
		JButton startMediaButton = new JButton("Start");
		startMediaButton.addActionListener(new StartUpdate(false));
		
		add(new JLabel("Main:"));
		add(mainFirmwarePath);
		add(selectMainButton);
		add(startMainButton);
		add(new JLabel("Media:"));
		add(mediaFirmwarePath);
		add(selectMediaButton);
		add(startMediaButton);
		
		add(botContainer, "span 4");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param observer
	 */
	public void addObserver(IFirmwareUpdatePanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * 
	 * @param observer
	 */
	public void removeObserver(IFirmwareUpdatePanelObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifyStartFirmwareUpdate(String filePath, boolean main)
	{
		synchronized (observers)
		{
			for (IFirmwareUpdatePanelObserver observer : observers)
			{
				observer.onStartFirmwareUpdate(filePath, main);
			}
		}
	}
	
	
	/**
	 * @param botID
	 * @param panel
	 */
	public void addBotPanel(BotID botID, JPanel panel)
	{
		botPanels.put(botID, panel);
		
		updatePanels();
	}
	
	
	/**
	 * @param botID
	 */
	public void removeBotPanel(BotID botID)
	{
		botPanels.remove(botID);
		
		updatePanels();
	}
	
	
	/**
	 */
	public void removeAllBotPanels()
	{
		botPanels.clear();
		
		updatePanels();
	}
	
	
	private void updatePanels()
	{
		final JPanel panel = this;
		
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				botContainer.removeAll();
				
				for (final JPanel panel : botPanels.values())
				{
					botContainer.add(panel, "wrap, gapbottom 0");
				}
				
				botContainer.add(Box.createGlue(), "push");
				
				SwingUtilities.updateComponentTreeUI(panel);
			}
		});
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	protected class ChooseBinFile implements ActionListener
	{
		private final boolean	main;
		
		
		public ChooseBinFile(boolean main)
		{
			this.main = main;
		}
		
		
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			int retVal = fileChooser.showOpenDialog(FirmwareUpdatePanel.this);
			
			if (retVal == JFileChooser.APPROVE_OPTION)
			{
				if (main)
				{
					mainFirmwarePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
					SumatraModel.getInstance().setUserProperty("mainFirmwareFile", mainFirmwarePath.getText());
				} else
				{
					mediaFirmwarePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
					SumatraModel.getInstance().setUserProperty("mediaFirmwareFile", mediaFirmwarePath.getText());
				}
			}
		}
	}
	
	protected class StartUpdate implements ActionListener
	{
		private final boolean	main;
		
		
		public StartUpdate(boolean main)
		{
			this.main = main;
		}
		
		
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			if (main)
			{
				notifyStartFirmwareUpdate(mainFirmwarePath.getText(), true);
			} else
			{
				notifyStartFirmwareUpdate(mediaFirmwarePath.getText(), false);
			}
		}
		
	}
	
	private class BinFilter extends FileFilter
	{
		
		@Override
		public boolean accept(File file)
		{
			if (file.isDirectory())
			{
				return true;
			}
			
			if (!file.getName().contains("main") && !file.getName().contains("media"))
			{
				return false;
			}
			
			if (!file.getName().contains(".bin"))
			{
				return false;
			}
			
			return true;
		}
		
		
		@Override
		public String getDescription()
		{
			return "Binary Files";
		}
	}
	
}
