/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.view;

import java.awt.EventQueue;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.StyledDocument;

import org.apache.log4j.Logger;


/**
 * This pane displays text! Colored text!
 * 
 * @author AndreR
 */
public class TextPane extends JScrollPane
{
	@SuppressWarnings("unused")
	private static final Logger	log					= Logger.getLogger(TextPane.class.getName());
	private static final long		serialVersionUID	= 1L;
	
	private final JTextPane			textPane;
	private final StyledDocument	doc;
	private final int					maxCapacity;
	private boolean					freeze				= false;
	
	private final Deque<Integer>	entries				= new LinkedList<Integer>();
	private final List<DocLine>	buffer				= new ArrayList<>();
	
	private static class DocLine
	{
		String			text;
		AttributeSet	aSet;
		boolean			append;
	}
	
	
	/**
	 * @param maxCapacity
	 */
	public TextPane(final int maxCapacity)
	{
		this.maxCapacity = maxCapacity;
		
		textPane = new JTextPane();
		doc = textPane.getStyledDocument();
		
		DefaultCaret caret = (DefaultCaret) textPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		// JTextArea style
		textPane.setFont(new Font("Verdana", Font.PLAIN, 12));
		textPane.setEditable(false);
		textPane.setOpaque(false);
		textPane.setFocusable(true);
		textPane.setDoubleBuffered(true);
		setBorder(null);
		
		super.getViewport().add(textPane);
	}
	
	
	/**
	 * @param text
	 * @param aset
	 */
	public void setText(final String text, final AttributeSet aset)
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
					log.error("Could not clear text pane.", err);
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
				if (freeze)
				{
					DocLine dLine = new DocLine();
					dLine.text = text;
					dLine.aSet = aset;
					dLine.append = true;
					buffer.add(dLine);
				} else
				{
					appendInternal(text, aset);
				}
			}
		});
	}
	
	
	private void appendInternal(final String text, final AttributeSet aset)
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
			
			textPane.setCaretPosition(doc.getLength());
		} catch (final BadLocationException err)
		{
			log.error("Could not append log event.", err);
		}
	}
	
	
	/**
	 * @param text
	 * @param aset
	 */
	public void prepend(final String text, final AttributeSet aset)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				if (freeze)
				{
					DocLine dLine = new DocLine();
					dLine.text = text;
					dLine.aSet = aset;
					dLine.append = false;
					buffer.add(dLine);
				} else
				{
					prependInternal(text, aset);
				}
			}
		});
	}
	
	
	private void prependInternal(final String text, final AttributeSet aset)
	{
		try
		{
			doc.insertString(0, text, aset);
			
			// Memorize the message-length
			entries.addFirst(text.length());
			
			// If there are too much entries: Remove the first!
			if (entries.size() > maxCapacity)
			{
				final Integer end = entries.pollLast();
				doc.remove(doc.getLength(), end);
			}
			
			textPane.setCaretPosition(0);
		} catch (final BadLocationException err)
		{
			log.error("Could not prepend log event.", err);
		}
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param freeze
	 */
	public void setFreeze(final boolean freeze)
	{
		if (!freeze)
		{
			EventQueue.invokeLater(() -> {
				buffer.forEach(dl -> {
					if (dl.append)
					{
						appendInternal(dl.text, dl.aSet);
					} else
					{
						prependInternal(dl.text, dl.aSet);
					}
				});
				buffer.clear();
				this.freeze = freeze;
			});
		} else
		{
			this.freeze = freeze;
		}
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
