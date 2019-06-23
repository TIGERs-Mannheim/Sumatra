/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.04.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsoleCommand.ConsoleCommandTarget;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsolePrint.ConsolePrintSource;
import edu.dhbw.mannheim.tigers.sumatra.view.log.internals.TextPane;


/**
 * Command line interface to a bot.
 * 
 * @author AndreR
 * 
 */
public class ConsolePanel extends JPanel
{
	/** */
	public static interface IConsolePanelObserver
	{
		/**
		 * 
		 * @param cmd
		 * @param target
		 */
		void onConsoleCommand(String cmd, ConsoleCommandTarget target);
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long						serialVersionUID	= 6126587090532483501L;
	private TextPane									textPane				= new TextPane(1100);
	private JTextField								cmdInput				= new JTextField();
	private JRadioButton								targetMain			= new JRadioButton("Main");
	private JRadioButton								targetMedia			= new JRadioButton("Media");
	
	private final List<IConsolePanelObserver>	observers			= new ArrayList<IConsolePanelObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public ConsolePanel()
	{
		setLayout(new MigLayout("wrap 1, fill", "[fill]", "[fill,grow][][]"));
		
		ButtonGroup group = new ButtonGroup();
		group.add(targetMain);
		group.add(targetMedia);
		
		targetMain.setSelected(true);
		
		cmdInput.addActionListener(new SendCommand());
		
		JPanel targetPanel = new JPanel(new MigLayout(""));
		targetPanel.add(targetMain);
		targetPanel.add(targetMedia);
		
		add(textPane);
		add(cmdInput);
		add(targetPanel);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param observer
	 */
	public void addObserver(IConsolePanelObserver observer)
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
	public void removeObserver(IConsolePanelObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifyConsoleCommand(String cmd, ConsoleCommandTarget target)
	{
		synchronized (observers)
		{
			for (IConsolePanelObserver observer : observers)
			{
				observer.onConsoleCommand(cmd, target);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Add a print command to log pane.
	 * 
	 * @param print
	 */
	public void addConsolePrint(final TigerSystemConsolePrint print)
	{
		Color color = new Color(0, 0, 0);
		if (print.getSource() == ConsolePrintSource.MEDIA)
		{
			color = new Color(0, 0, 128);
		}
		
		final StyleContext sc = StyleContext.getDefaultStyleContext();
		final AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);
		
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				textPane.append(print.getText() + "\n", aset);
			}
		});
	}
	
	private class SendCommand implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			ConsoleCommandTarget target = ConsoleCommandTarget.MAIN;
			Color color = new Color(0, 0, 0);
			if (targetMedia.isSelected())
			{
				target = ConsoleCommandTarget.MEDIA;
				color = new Color(0, 0, 255);
			}
			
			final StyleContext sc = StyleContext.getDefaultStyleContext();
			AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);
			aset = sc.addAttribute(aset, StyleConstants.Bold, true);
			
			String text = cmdInput.getText();
			
			textPane.append(text + "\n", aset);
			
			cmdInput.setText("");
			
			notifyConsoleCommand(text, target);
		}
	}
}
