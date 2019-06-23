/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.09.2010
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.ApollonControl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.IApollonControlHandler;


/**
 * This panel can be used to control the the learning Play Finder.
 * This must be a child panel of {@link ModuleControlPanel}.
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public class ApollonControlPanel extends JPanel implements IChangeGUIMode, IApollonControlHandler
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger								log								= Logger
																													.getLogger(ApollonControlPanel.class
																															.getName());
	
	private static final long									serialVersionUID				= -2647962360775299686L;
	
	private static final String								SELECT							= "select";
	
	private final List<IApollonControlPanelObserver>	observers						= new LinkedList<IApollonControlPanelObserver>();
	
	private JComboBox<String>									kbNames							= null;
	private JTextField											acceptableMatchText			= null;
	private JTextField											dbFilePath						= null;
	private JButton												selectDbButton					= null;
	private JCheckBox												mergeDbCheckbox				= null;
	private JCheckBox												saveOnCloseCheckBox			= null;
	
	private String													currentKnowledgeBaseName	= "";
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public ApollonControlPanel()
	{
		setLayout(new MigLayout());
		setBorder(BorderFactory.createTitledBorder("Learning PlayFinder Control Panel"));
		
		kbNames = new JComboBox<String>(new String[] { SELECT });
		KbNameListener kbNameListener = new KbNameListener();
		kbNames.addActionListener(kbNameListener);
		kbNames.addKeyListener(kbNameListener);
		kbNames.setEditable(true);
		final JPanel kbNamePanel = new JPanel(new MigLayout("fill", "[]10[40,fill]"));
		kbNamePanel.add(new JLabel("Knowledge Base Name:"));
		kbNamePanel.add(kbNames);
		
		acceptableMatchText = new JTextField("80", 2);
		acceptableMatchText.setToolTipText("Must be a value between 0 and 100");
		acceptableMatchText.getDocument().addDocumentListener(new AcceptableMatchChangedListener());
		final JPanel acceptableMatchPanel = new JPanel(new MigLayout("fill", "[]10[40,fill]"));
		acceptableMatchPanel.add(new JLabel("Acceptable Match in % (0-100):"));
		acceptableMatchPanel.add(acceptableMatchText, "width 50!");
		
		dbFilePath = new JTextField(30);
		dbFilePath.addKeyListener(new DbFilePathChangedListener());
		selectDbButton = new JButton("Select DB");
		selectDbButton.addActionListener(new SelectDbActionListener());
		
		final JPanel dbFilePathPanel = new JPanel(new MigLayout("fill", "[]10[40,fill]"));
		dbFilePathPanel.add(new JLabel("DB File Path:"));
		dbFilePathPanel.add(dbFilePath, "width 300!");
		dbFilePathPanel.add(selectDbButton, "width 100!");
		
		mergeDbCheckbox = new JCheckBox("Merge", false);
		mergeDbCheckbox.addItemListener(new PersistStrategyActionListener());
		
		final JPanel persistStrategyPanel = new JPanel(new MigLayout("fill", "[]10[40,fill]"));
		persistStrategyPanel.add(new JLabel("Persist Strategy:"));
		persistStrategyPanel.add(mergeDbCheckbox);
		
		saveOnCloseCheckBox = new JCheckBox("Save KB on close", true);
		saveOnCloseCheckBox.addActionListener(new SaveOnCloseListener());
		
		JButton saveKb = new JButton("Save KB");
		saveKb.addActionListener(new SaveListener());
		
		JButton cleanKb = new JButton("Clean KB");
		cleanKb.addActionListener(new CleanListener());
		
		JButton stats = new JButton("Statistics");
		stats.addActionListener(new StatisticsListener());
		
		final JPanel controlPanel = new JPanel(new MigLayout("fill"));
		controlPanel.add(kbNamePanel, "wrap");
		controlPanel.add(acceptableMatchPanel, "wrap");
		controlPanel.add(dbFilePathPanel, "wrap");
		controlPanel.add(persistStrategyPanel, "wrap");
		controlPanel.add(saveOnCloseCheckBox, "wrap");
		controlPanel.add(saveKb, "");
		controlPanel.add(stats, "wrap");
		
		this.add(controlPanel);
	}
	
	
	/**
	 * Load kb names from path and put them in dd box
	 */
	private void loadKbNames(String newPath)
	{
		ObjectDbFileFilter filter = new ObjectDbFileFilter();
		File dir = new File(newPath);
		File files[] = dir.listFiles(filter);
		kbNames.removeAllItems();
		kbNames.addItem(SELECT);
		
		for (File file : files)
		{
			String item = file.getName().replace(".odb", "");
			kbNames.addItem(item);
			if (item.equals(currentKnowledgeBaseName))
			{
				kbNames.setSelectedItem(item);
			}
		}
	}
	
	
	@Override
	public void setPlayTestMode()
	{
		setEnabled(true);
		acceptableMatchText.setEnabled(true);
	}
	
	
	@Override
	public void setRoleTestMode()
	{
		setEnabled(false);
		acceptableMatchText.setEnabled(true);
	}
	
	
	@Override
	public void setMatchMode()
	{
		setEnabled(false);
		acceptableMatchText.setEnabled(false);
	}
	
	
	@Override
	public void setEmergencyMode()
	{
		setEnabled(false);
		acceptableMatchText.setEnabled(false);
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(IApollonControlPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param oddObserver
	 */
	public void removeObserver(IApollonControlPanelObserver oddObserver)
	{
		synchronized (observers)
		{
			observers.remove(oddObserver);
		}
	}
	
	
	@Override
	public void onStart()
	{
		setEnabled(true);
	}
	
	
	@Override
	public void onStop()
	{
		setEnabled(false);
	}
	
	
	private void kbNameChanged()
	{
		final String newName = (String) kbNames.getSelectedItem();
		if ((newName != null) && !SELECT.equals(newName))
		{
			log.info("New knowledge base name: " + newName);
			onKnowledgeBaseNameChanged(newName);
		}
	}
	
	
	private void dbFilePathChanged()
	{
		String newPath = dbFilePath.getText();
		if (new File(newPath).isDirectory())
		{
			log.info("New db path is: " + newPath);
			SumatraModel.getInstance().setUserProperty(ApollonControl.PATH_KEY, newPath);
			onDatabasePathChanged(newPath);
		} else
		{
			log.error("New db path is not a directory: " + newPath);
		}
	}
	
	
	private synchronized void onKnowledgeBaseNameChanged(String newName)
	{
		for (IApollonControlPanelObserver observer : observers)
		{
			observer.onKnowledgeBaseNameChanged(newName);
		}
	}
	
	
	private void acceptableMatchChanged()
	{
		try
		{
			int newAccMatch = Integer.valueOf(acceptableMatchText.getText());
			log.info("Acceptable Match is now: " + newAccMatch);
			onAcceptableMatchChanged(newAccMatch);
		} catch (final NumberFormatException e)
		{
			log.error(acceptableMatchText.getText() + " is not a number");
		}
		
	}
	
	
	private synchronized void onAcceptableMatchChanged(int newAccMatch)
	{
		for (IApollonControlPanelObserver observer : observers)
		{
			observer.onAcceptableMatchChanged(newAccMatch);
		}
	}
	
	
	private synchronized void onDatabasePathChanged(String newPath)
	{
		loadKbNames(newPath);
		for (IApollonControlPanelObserver observer : observers)
		{
			observer.onDatabasePathChanged(newPath);
		}
	}
	
	
	private void persistStrategyChanged()
	{
		boolean merge = mergeDbCheckbox.isSelected();
		log.info("Merge: " + mergeDbCheckbox.isSelected());
		onPersistStrategyChanged(merge);
	}
	
	
	private synchronized void onPersistStrategyChanged(boolean merge)
	{
		for (IApollonControlPanelObserver observer : observers)
		{
			observer.onPersistStrategyChanged(merge);
		}
	}
	
	
	private void saveOnCloseChanged()
	{
		boolean saveOnClose = saveOnCloseCheckBox.isSelected();
		onSaveOnCloseChanged(saveOnClose);
		if (saveOnClose)
		{
			log.info("Save on close is selected");
		} else
		{
			log.info("Save on close is not selected");
		}
	}
	
	
	private void onSaveKb()
	{
		for (IApollonControlPanelObserver observer : observers)
		{
			observer.onSaveKbNow();
		}
	}
	
	
	private void onCleanKb()
	{
		for (IApollonControlPanelObserver observer : observers)
		{
			observer.onCleanKbNow();
		}
	}
	
	
	private synchronized void onSaveOnCloseChanged(boolean saveOnClose)
	{
		for (IApollonControlPanelObserver observer : observers)
		{
			observer.onSaveOnCloseChanged(saveOnClose);
		}
	}
	
	
	private String selectFile() throws FileChooserException
	{
		final JFileChooser fChooser = new JFileChooser("Choose Database");
		fChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		fChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fChooser.setCurrentDirectory(new File(dbFilePath.getText()));
		fChooser.setVisible(true);
		final int result = fChooser.showOpenDialog(null);
		fChooser.setVisible(false);
		
		switch (result)
		{
			case JFileChooser.APPROVE_OPTION:
				return fChooser.getSelectedFile().getPath();
			case JFileChooser.CANCEL_OPTION:
			case JFileChooser.ERROR_OPTION:
			default:
				throw new FileChooserException("Aborted File Chooser");
		}
	}
	
	private static class FileChooserException extends Exception
	{
		private static final long	serialVersionUID	= -440947739536879198L;
		
		
		/**
		 * @param msg
		 */
		public FileChooserException(String msg)
		{
			super(msg);
		}
	}
	
	private static class ObjectDbFileFilter implements FilenameFilter
	{
		
		@Override
		public boolean accept(File dir, String name)
		{
			File file = new File(dir, name);
			if (file.isFile())
			{
				return file.getName().toLowerCase().endsWith(".odb");
			}
			return false;
		}
		
	}
	
	
	private class KbNameListener implements ActionListener, KeyListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			kbNameChanged();
		}
		
		
		@Override
		public void keyTyped(KeyEvent e)
		{
		}
		
		
		@Override
		public void keyPressed(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_ENTER)
			{
				kbNameChanged();
			}
		}
		
		
		@Override
		public void keyReleased(KeyEvent e)
		{
		}
	}
	
	private class AcceptableMatchChangedListener implements DocumentListener
	{
		
		@Override
		public void changedUpdate(DocumentEvent de)
		{
			acceptableMatchChanged();
		}
		
		
		@Override
		public void insertUpdate(DocumentEvent de)
		{
			acceptableMatchChanged();
		}
		
		
		@Override
		public void removeUpdate(DocumentEvent de)
		{
			acceptableMatchChanged();
		}
		
	}
	
	private class SelectDbActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent ae)
		{
			try
			{
				dbFilePath.setText(selectFile());
				dbFilePathChanged();
			} catch (final FileChooserException e)
			{
				// do nothing because nothign was changed
			}
		}
	}
	
	private class DbFilePathChangedListener implements KeyListener
	{
		
		@Override
		public void keyTyped(KeyEvent e)
		{
		}
		
		
		@Override
		public void keyPressed(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_ENTER)
			{
				dbFilePathChanged();
			}
		}
		
		
		@Override
		public void keyReleased(KeyEvent e)
		{
		}
	}
	
	private class PersistStrategyActionListener implements ItemListener
	{
		@Override
		public void itemStateChanged(ItemEvent e)
		{
			persistStrategyChanged();
		}
	}
	
	private class SaveOnCloseListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			saveOnCloseChanged();
		}
	}
	
	private class SaveListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			onSaveKb();
			loadKbNames(dbFilePath.getText());
		}
	}
	
	private class CleanListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			onCleanKb();
		}
	}
	
	private static class StatisticsListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			log.error("Not implemented yet");
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- Actions --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void onNewApollonControl(ApollonControl newControl)
	{
		dbFilePath.setText(newControl.getDatabasePath());
		currentKnowledgeBaseName = newControl.getKnowledgeBaseName();
		loadKbNames(newControl.getDatabasePath());
	}
	
	
	@Override
	public void onSaveKnowledgeBase()
	{
		// nothing to do
	}
	
	
	// --------------------------------------------------------------------------
	// --- Listeners --------------------------------------------------------------
	// --------------------------------------------------------------------------
}
