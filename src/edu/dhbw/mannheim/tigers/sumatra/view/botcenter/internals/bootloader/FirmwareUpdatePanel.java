/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.06.2013
 * Author(s): AndreR
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
 */
public class FirmwareUpdatePanel extends JPanel
{
	/** */
	public interface IFirmwareUpdatePanelObserver
	{
		/**
		 * @param filePath
		 * @param target
		 */
		void onStartFirmwareUpdate(String filePath, int target);
		
		
		/**
		 */
		void onCancel();
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long									serialVersionUID	= -1561564204669395366L;
	
	private final JPanel											botContainer		= new JPanel();
	private final JTextField									mainFirmwarePath	= new JTextField();
	private final JTextField									mediaFirmwarePath	= new JTextField();
	private final JTextField									kdFirmwarePath		= new JTextField();
	private final JTextField									leftFirmwarePath	= new JTextField();
	private final JTextField									rightFirmwarePath	= new JTextField();
	private final Map<BotID, FirmwareBotPanel>			botPanels			= new TreeMap<BotID, FirmwareBotPanel>();
	private final JFileChooser									fileChooser			= new JFileChooser();
	
	private final List<IFirmwareUpdatePanelObserver>	observers			= new ArrayList<IFirmwareUpdatePanelObserver>();
	
	private final JButton										btnCancel			= new JButton("Cancel");
	private final JButton										startMainButton	= new JButton("Start");
	private final JButton										startMediaButton	= new JButton("Start");
	private final JButton										startKdButton		= new JButton("Start");
	private final JButton										startLeftButton	= new JButton("Start");
	private final JButton										startRightButton	= new JButton("Start");
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/** */
	public FirmwareUpdatePanel()
	{
		fileChooser.setFileFilter(new BinFilter());
		
		String mainCfgPath = SumatraModel.getInstance().getUserProperty(
				FirmwareUpdatePanel.class.getCanonicalName() + ".mainFirmwareFile");
		if (mainCfgPath != null)
		{
			mainFirmwarePath.setText(mainCfgPath);
		}
		String mediaCfgPath = SumatraModel.getInstance().getUserProperty(
				FirmwareUpdatePanel.class.getCanonicalName() + ".mediaFirmwareFile");
		if (mediaCfgPath != null)
		{
			mediaFirmwarePath.setText(mediaCfgPath);
		}
		String kdCfgPath = SumatraModel.getInstance().getUserProperty(
				FirmwareUpdatePanel.class.getCanonicalName() + ".kdFirmwareFile");
		if (kdCfgPath != null)
		{
			kdFirmwarePath.setText(kdCfgPath);
		}
		String leftCfgPath = SumatraModel.getInstance().getUserProperty(
				FirmwareUpdatePanel.class.getCanonicalName() + ".leftFirmwareFile");
		if (leftCfgPath != null)
		{
			leftFirmwarePath.setText(leftCfgPath);
		}
		String rightCfgPath = SumatraModel.getInstance().getUserProperty(
				FirmwareUpdatePanel.class.getCanonicalName() + ".rightFirmwareFile");
		if (rightCfgPath != null)
		{
			rightFirmwarePath.setText(rightCfgPath);
		}
		
		setLayout(new MigLayout("wrap 4", "[60]10[250,fill]10[50]10[50]"));
		
		botContainer.setLayout(new MigLayout(""));
		
		JButton selectMainButton = new JButton("Select");
		selectMainButton.addActionListener(new ChooseBinFile(0));
		JButton selectMediaButton = new JButton("Select");
		selectMediaButton.addActionListener(new ChooseBinFile(1));
		JButton selectKdButton = new JButton("Select");
		selectKdButton.addActionListener(new ChooseBinFile(2));
		JButton selectLeftButton = new JButton("Select");
		selectLeftButton.addActionListener(new ChooseBinFile(3));
		JButton selectRightButton = new JButton("Select");
		selectRightButton.addActionListener(new ChooseBinFile(4));
		startMainButton.addActionListener(new StartUpdate(0));
		startMediaButton.addActionListener(new StartUpdate(1));
		startKdButton.addActionListener(new StartUpdate(2));
		startLeftButton.addActionListener(new StartUpdate(3));
		startRightButton.addActionListener(new StartUpdate(4));
		
		add(new JLabel("Main:"));
		add(mainFirmwarePath);
		add(selectMainButton);
		add(startMainButton);
		add(new JLabel("Media:"));
		add(mediaFirmwarePath);
		add(selectMediaButton);
		add(startMediaButton);
		add(new JLabel("KD:"));
		add(kdFirmwarePath);
		add(selectKdButton);
		add(startKdButton);
		add(new JLabel("Left:"));
		add(leftFirmwarePath);
		add(selectLeftButton);
		add(startLeftButton);
		add(new JLabel("Right:"));
		add(rightFirmwarePath);
		add(selectRightButton);
		add(startRightButton);
		
		add(botContainer, "span 4");
		
		btnCancel.addActionListener(new CancelAction());
		btnCancel.setEnabled(false);
		add(btnCancel);
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
	
	
	private void notifyStartFirmwareUpdate(final String filePath, final int target)
	{
		synchronized (observers)
		{
			for (IFirmwareUpdatePanelObserver observer : observers)
			{
				observer.onStartFirmwareUpdate(filePath, target);
			}
		}
	}
	
	
	private void notifyCancel()
	{
		synchronized (observers)
		{
			for (IFirmwareUpdatePanelObserver observer : observers)
			{
				observer.onCancel();
			}
		}
	}
	
	
	/**
	 * @param panel
	 */
	public void addBotPanel(final FirmwareBotPanel panel)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				botContainer.add(panel, "wrap, gapbottom 0");
				SwingUtilities.updateComponentTreeUI(FirmwareUpdatePanel.this);
			}
		});
	}
	
	
	/**
	 * @param panel
	 */
	public void removeBotPanel(final JPanel panel)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				botContainer.remove(panel);
				SwingUtilities.updateComponentTreeUI(FirmwareUpdatePanel.this);
			}
		});
	}
	
	
	/**
	 */
	public void removeAllBotPanels()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				botContainer.removeAll();
				botContainer.add(Box.createGlue(), "push");
				SwingUtilities.updateComponentTreeUI(FirmwareUpdatePanel.this);
			}
		});
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private class CancelAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			notifyCancel();
		}
	}
	
	/**
	 */
	protected class ChooseBinFile implements ActionListener
	{
		private final int	target;
		
		
		/**
		 * @param target
		 */
		public ChooseBinFile(final int target)
		{
			this.target = target;
		}
		
		
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			int retVal = fileChooser.showOpenDialog(FirmwareUpdatePanel.this);
			
			if (retVal == JFileChooser.APPROVE_OPTION)
			{
				switch (target)
				{
					case 0:
					{
						mainFirmwarePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
						SumatraModel.getInstance().setUserProperty(
								FirmwareUpdatePanel.class.getCanonicalName() + ".mainFirmwareFile", mainFirmwarePath.getText());
						
					}
						break;
					case 1:
					{
						mediaFirmwarePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
						SumatraModel.getInstance().setUserProperty(
								FirmwareUpdatePanel.class.getCanonicalName() + ".mediaFirmwareFile",
								mediaFirmwarePath.getText());
					}
						break;
					case 2:
					{
						kdFirmwarePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
						SumatraModel.getInstance().setUserProperty(
								FirmwareUpdatePanel.class.getCanonicalName() + ".kdFirmwareFile", kdFirmwarePath.getText());
					}
						break;
					case 3:
					{
						leftFirmwarePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
						SumatraModel.getInstance().setUserProperty(
								FirmwareUpdatePanel.class.getCanonicalName() + ".leftFirmwareFile", leftFirmwarePath.getText());
					}
						break;
					case 4:
					{
						rightFirmwarePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
						SumatraModel.getInstance().setUserProperty(
								FirmwareUpdatePanel.class.getCanonicalName() + ".rightFirmwareFile",
								rightFirmwarePath.getText());
					}
						break;
				}
			}
		}
	}
	
	/**
	 */
	protected class StartUpdate implements ActionListener
	{
		private final int	target;
		
		
		/**
		 * @param target
		 */
		public StartUpdate(final int target)
		{
			this.target = target;
		}
		
		
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			switch (target)
			{
				case 0:
					notifyStartFirmwareUpdate(mainFirmwarePath.getText(), 0);
					break;
				case 1:
					notifyStartFirmwareUpdate(mediaFirmwarePath.getText(), 1);
					break;
				case 2:
					notifyStartFirmwareUpdate(kdFirmwarePath.getText(), 2);
					break;
				case 3:
					notifyStartFirmwareUpdate(leftFirmwarePath.getText(), 3);
					break;
				case 4:
					notifyStartFirmwareUpdate(rightFirmwarePath.getText(), 4);
					break;
			}
			
		}
		
	}
	
	private class BinFilter extends FileFilter
	{
		
		@Override
		public boolean accept(final File file)
		{
			if (file.isDirectory())
			{
				return true;
			}
			
			if (!file.getName().contains(".bin"))
			{
				return false;
			}
			
			return true;
		}
		
		
		/**
		 */
		@Override
		public String getDescription()
		{
			return "Binary Files";
		}
	}
	
	
	/**
	 * @return the botPanels
	 */
	public final Map<BotID, FirmwareBotPanel> getBotPanels()
	{
		return botPanels;
	}
	
	
	/**
	 * @param flashing
	 */
	public final void setFlashing(final boolean flashing)
	{
		startMainButton.setEnabled(!flashing);
		startMediaButton.setEnabled(!flashing);
		startKdButton.setEnabled(!flashing);
		startLeftButton.setEnabled(!flashing);
		startRightButton.setEnabled(!flashing);
		btnCancel.setEnabled(flashing);
	}
	
}
