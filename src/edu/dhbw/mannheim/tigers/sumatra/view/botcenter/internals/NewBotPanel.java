/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 25.08.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;

/**
 * Select a new bot.
 * 
 * @author AndreR
 * 
 */
public class NewBotPanel extends JDialog
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -7060966523316572971L;
	
	private JComboBox type = null;
	private JTextField id = null;
	private JTextField name = null;

	private ImageIcon botIcon = null;
	
	private Map<String, EBotType> types;
	
	private boolean cancelled = true;
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public NewBotPanel(Map<String, EBotType> types)
	{
		botIcon = new ImageIcon(ClassLoader.getSystemResource("bot.png"));
		
		setTitle("Create new bot");
		setLayout(new MigLayout("fill", "[]10[100,fill]10[]10[30,fill]10[]10[100,fill]", "[]20[]"));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		setIconImage(botIcon.getImage());
		setModal(true);
		
		this.types = types;
		
		String[] names = types.keySet().toArray(new String[types.size()]);

		type = new JComboBox(names);
		id = new JTextField();
		name = new JTextField();
		
		JButton ok = new JButton("OK");
		JButton cancel = new JButton("Cancel");
		
		ok.addActionListener(new CloseOperation(false));
		cancel.addActionListener(new CloseOperation(true));
	
		add(new JLabel("Type:"));
		add(type);
		add(new JLabel("ID:"));
		add(id);
		add(new JLabel("Name:"));
		add(name, "wrap");
		add(ok, "span 3, w 100, align center");
		add(cancel, "span 3, w 100, align center, grow 0");
		
		pack();
		
		setMinimumSize(getSize());
		
		setLocation(Toolkit.getDefaultToolkit().getScreenSize().width/2-getSize().width/2, Toolkit.getDefaultToolkit().getScreenSize().height/2-getSize().height/2);
	}	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public boolean isDataValid()
	{
		if(cancelled)
			return false;
		
		try
		{
			Integer.parseInt(id.getText());
		}
		catch(NumberFormatException e)
		{
			return false;
		}
		
		return true;
	}
	
	public EBotType getType()
	{
		return types.get(type.getSelectedItem());
	}
	
	public int getId()
	{
		return Integer.parseInt(id.getText());
	}
	
	public String getName()
	{
		return name.getText();
	}
	
	protected class CloseOperation implements ActionListener
	{
		private boolean cancel;
		
		public CloseOperation(boolean cancel)
		{
			this.cancel = cancel;
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			cancelled = cancel;
			
			dispose();
		}
	}
}
