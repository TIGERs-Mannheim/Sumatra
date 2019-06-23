/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.log.internals;

import java.awt.Font;
import java.util.Deque;
import java.util.LinkedList;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;


/**
 * This pane displays text! Colored text!
 * 
 * @author AndreR
 * 
 */
public class TextPane extends JScrollPane
{
	private static final long		serialVersionUID	= 1L;
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final JTextPane			textPane;
	private final StyledDocument	doc;
	private final int					maxCapacity;
	private boolean					autoscroll			= true;
	
	private final Deque<Integer>	entries				= new LinkedList<Integer>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param maxCapacity
	 */
	public TextPane(int maxCapacity)
	{
		this.maxCapacity = maxCapacity;
		
		textPane = new JTextPane();
		doc = textPane.getStyledDocument();
		
		// JTextArea style
		textPane.setAutoscrolls(true);
		textPane.setFont(new Font("Verdana", Font.PLAIN, 12));
		textPane.setEditable(false);
		textPane.setOpaque(false);
		textPane.setFocusable(true);
		textPane.setDoubleBuffered(true);
		setBorder(null);
		
		super.getViewport().add(textPane);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param text
	 * @param aset
	 */
	public void setText(String text, AttributeSet aset)
	{
		clear();
		
		append(text, aset);
	}
	
	
	/**
	 */
	public void clear()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					doc.remove(0, doc.getLength());
					
					entries.clear();
				} catch (final BadLocationException err)
				{
				}
			}
		});
	}
	
	
	/**
	 * @param text
	 * @param aset
	 */
	public void append(final String text, final AttributeSet aset)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					doc.insertString(doc.getLength(), text, aset);
					
					// Memorize the message-length
					entries.add(text.length());
					
					// If there are too much entries: Remove the first!
					if (entries.size() > maxCapacity)
					{
						final Integer end = entries.pollFirst();
						doc.remove(0, end);
					}
					
					
					if (autoscroll)
					{
						textPane.setCaretPosition(doc.getLength());
					}
				} catch (final BadLocationException err)
				{
				}
			}
		});
	}
	
	
	/**
	 * @param en
	 */
	public void setAutoscroll(boolean en)
	{
		autoscroll = en;
		textPane.setCaretPosition(doc.getLength() - 1);
	}
	
	
	/**
	 * Number of entries in doc
	 * 
	 * @return
	 */
	public int getLength()
	{
		return doc.getLength();
	}
}
