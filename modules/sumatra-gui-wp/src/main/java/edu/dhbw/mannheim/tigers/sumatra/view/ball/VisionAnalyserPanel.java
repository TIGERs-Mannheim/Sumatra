/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 21, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.ball;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.filetree.FileTree;
import edu.tigers.sumatra.filetree.FileTree.IFileTreeObserver;
import edu.tigers.sumatra.views.ISumatraView;
import net.miginfocom.swing.MigLayout;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VisionAnalyserPanel extends JPanel implements ISumatraView
{
	
	/**  */
	private static final long								serialVersionUID	= 2543451767176872886L;
																							
	private FileTree											fileTree				= null;
	private final JPanel										fileTreePanel		= new JPanel();
	private final JButton									btnSave				= new JButton("Save");
	private final JButton									btnDelete			= new JButton("Delete");
	private final JButton									btnPlot				= new JButton("Plot");
	private final JButton									btnBallModel		= new JButton("Update Ball Model");
	private final JButton									btnKickModel		= new JButton("Update Kick Model");
	private final JButton									btnKalman			= new JButton("run kalman");
	private final JButton									btnBallCorrector	= new JButton("ball corrector");
	private final JButton									btnCopy				= new JButton("Copy path");
	private final JToggleButton							btnRecord			= new JToggleButton("Record");
	private final JTextArea									txtDescription		= new JTextArea("Describe your data", 5, 80);
	private final JLabel										lblNumSamples		= new JLabel("Num samples: ");
	private final JCheckBox									chkStopAuto			= new JCheckBox("Stop");
	private final JPanel										keyValuePanel		= new JPanel(new MigLayout("fillx, wrap 1"));
	private final JComboBox<EBotType>					cmbBotType			= new JComboBox<EBotType>(EBotType.values());
																							
	private List<String>										selectedFiles		= new ArrayList<>();
																							
	private final List<IBallAnalyserPanelObserver>	observers			= new CopyOnWriteArrayList<IBallAnalyserPanelObserver>();
																							
																							
	/**
	 * 
	 */
	public VisionAnalyserPanel()
	{
		super(new BorderLayout());
		
		JButton btnRefresh = new JButton("Refresh");
		
		JPanel rightPanel = new JPanel(new MigLayout("fill, wrap 1"));
		rightPanel.add(btnRefresh, "growx");
		rightPanel.add(btnCopy, "growx");
		rightPanel.add(btnSave, "growx");
		rightPanel.add(btnDelete, "growx");
		rightPanel.add(btnPlot, "growx");
		rightPanel.add(btnBallModel, "growx");
		rightPanel.add(btnKickModel, "growx");
		rightPanel.add(btnBallCorrector, "growx");
		rightPanel.add(btnKalman, "growx");
		add(rightPanel, BorderLayout.EAST);
		
		JPanel recordPanel = new JPanel(new MigLayout("fill"));
		recordPanel.add(chkStopAuto, "");
		recordPanel.add(btnRecord, "");
		
		JPanel centerPanel = new JPanel(new MigLayout("fillx"));
		centerPanel.add(recordPanel, "growx, wrap");
		centerPanel.add(cmbBotType, "growx, wrap");
		centerPanel.add(lblNumSamples, "growx, wrap");
		centerPanel.add(txtDescription, "growx, wrap");
		centerPanel.add(keyValuePanel, "grow, wrap, push");
		add(centerPanel, BorderLayout.CENTER);
		
		add(fileTreePanel, BorderLayout.WEST);
		
		btnRecord.addActionListener(new RecordAction());
		btnSave.addActionListener(new SaveAction());
		btnDelete.addActionListener(new DeleteAction());
		btnPlot.addActionListener(new PlotAction());
		btnBallModel.addActionListener(new BallModelAction());
		btnKickModel.addActionListener(new KickModelAction());
		btnBallCorrector.addActionListener(new BallCorrectorAction());
		btnKalman.addActionListener(new KalmanAction());
		btnCopy.addActionListener(new CopyAction());
		btnRefresh.addActionListener(new RefreshAction());
		
		setValidFileSelected(false);
		txtDescription.addKeyListener(new DescriptionKeyListener());
		fileTreePanel.setLayout(new BorderLayout());
		
		updateFiles();
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IBallAnalyserPanelObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IBallAnalyserPanelObserver observer)
	{
		observers.remove(observer);
	}
	
	
	/**
	 */
	public final void updateFiles()
	{
		fileTree = new FileTree(new File("data/vision"), fileTree);
		fileTree.addObserver(new FileTreeObserver());
		// fileTree.expandPaths(4);
		fileTreePanel.removeAll();
		fileTreePanel.add(new JScrollPane(fileTree), BorderLayout.CENTER);
		fileTreePanel.repaint();
		fileTree.repaint();
	}
	
	
	/**
	 * @param num
	 */
	public void setCurrentNumSamples(final int num)
	{
		btnRecord.setText(String.format("Record (%d)", num));
	}
	
	
	/**
	 * @param desc
	 */
	public void setDescription(final String desc)
	{
		txtDescription.setText(desc);
	}
	
	
	/**
	 * @return
	 */
	public String getDescription()
	{
		return txtDescription.getText();
	}
	
	
	/**
	 * @param num
	 */
	public void setNumSamples(final int num)
	{
		lblNumSamples.setText("Num samples: " + num);
	}
	
	
	/**
	 * @param recording
	 */
	public void setRecording(final boolean recording)
	{
		btnRecord.setSelected(recording);
		if (!recording)
		{
			btnRecord.setText("Record");
		}
	}
	
	
	/**
	 * @param enabled
	 */
	public void setValidFileSelected(final boolean enabled)
	{
		btnDelete.setEnabled(enabled);
		btnBallModel.setEnabled(enabled);
		btnKickModel.setEnabled(enabled);
		btnPlot.setEnabled(enabled);
		btnBallCorrector.setEnabled(enabled);
		btnKalman.setEnabled(enabled);
		txtDescription.setEditable(enabled);
		markDirty(false);
	}
	
	
	/**
	 * @param dirty
	 */
	public void markDirty(final boolean dirty)
	{
		btnSave.setEnabled(dirty && btnDelete.isEnabled());
	}
	
	
	/**
	 * @return the selectedFile
	 */
	public final List<String> getSelectedFile()
	{
		return selectedFiles;
	}
	
	
	/**
	 * @param key
	 * @param value
	 */
	public void setKeyValue(final String key, final String value)
	{
		keyValuePanel.add(new JLabel(key + ": " + value), "growx");
	}
	
	
	/**
	 */
	public void clearKeyValue()
	{
		keyValuePanel.removeAll();
		keyValuePanel.repaint();
	}
	
	
	@Override
	public List<JMenu> getCustomMenus()
	{
		return new ArrayList<>(0);
	}
	
	
	@Override
	public void onShown()
	{
	}
	
	
	@Override
	public void onHidden()
	{
	}
	
	
	@Override
	public void onFocused()
	{
	}
	
	
	@Override
	public void onFocusLost()
	{
	}
	
	
	private void save()
	{
		for (IBallAnalyserPanelObserver o : observers)
		{
			o.onSave(selectedFiles.get(0));
		}
		markDirty(false);
	}
	
	private class RecordAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IBallAnalyserPanelObserver o : observers)
			{
				o.onRecord(btnRecord.isSelected(), chkStopAuto.isSelected());
			}
		}
	}
	
	private class SaveAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			save();
		}
	}
	
	
	private class DeleteAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IBallAnalyserPanelObserver o : observers)
			{
				o.onDelete(selectedFiles);
			}
		}
	}
	
	private class PlotAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IBallAnalyserPanelObserver o : observers)
			{
				o.onPlot(selectedFiles);
			}
		}
	}
	
	private class BallModelAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IBallAnalyserPanelObserver o : observers)
			{
				o.onCreateBallModel(selectedFiles);
			}
		}
	}
	
	private class KickModelAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IBallAnalyserPanelObserver o : observers)
			{
				o.onCreateKickModel(selectedFiles, (EBotType) cmbBotType.getSelectedItem());
			}
		}
	}
	
	private class RefreshAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			updateFiles();
		}
	}
	
	private class KalmanAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IBallAnalyserPanelObserver o : observers)
			{
				o.onKalman(selectedFiles);
			}
		}
	}
	
	private class BallCorrectorAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IBallAnalyserPanelObserver o : observers)
			{
				o.onBallCorrector(selectedFiles);
			}
		}
	}
	
	private class CopyAction implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IBallAnalyserPanelObserver o : observers)
			{
				o.onCopy(selectedFiles);
			}
		}
	}
	
	private class FileTreeObserver implements IFileTreeObserver
	{
		@Override
		public void onFileSelected(final List<String> filenames)
		{
			selectedFiles = filenames;
			for (IBallAnalyserPanelObserver o : observers)
			{
				o.onNewSelectedFile(selectedFiles);
			}
		}
	}
	
	private class DescriptionKeyListener implements KeyListener
	{
		@Override
		public void keyTyped(final KeyEvent e)
		{
			if (!e.isActionKey() && !e.isControlDown())
			{
				markDirty(true);
			}
		}
		
		
		@Override
		public void keyPressed(final KeyEvent e)
		{
			if ((e.getKeyCode() == KeyEvent.VK_S) && e.isControlDown())
			{
				save();
			}
		}
		
		
		@Override
		public void keyReleased(final KeyEvent e)
		{
		}
	}
}
