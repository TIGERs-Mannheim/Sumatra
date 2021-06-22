/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view;

import edu.tigers.sumatra.drawable.EFontSize;
import edu.tigers.sumatra.util.ScalingUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.StyledDocument;
import java.awt.Font;
import java.util.Deque;
import java.util.LinkedList;


/**
 * This pane displays text! Colored text!
 */
public class TextPane extends JScrollPane
{
	private static final Logger log = LogManager.getLogger(TextPane.class.getName());

	private final JTextPane pane;
	private final StyledDocument doc;
	private final int maxCapacity;

	private final Deque<Integer> entryLengths = new LinkedList<>();


	public TextPane(final int maxCapacity)
	{
		this.maxCapacity = maxCapacity;

		pane = new JTextPane();
		doc = pane.getStyledDocument();

		DefaultCaret caret = (DefaultCaret) pane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		setFontSize(ScalingUtil.getFontSize(EFontSize.SMALL));
		pane.setEditable(false);
		pane.setOpaque(false);
		pane.setFocusable(true);
		pane.setDoubleBuffered(true);
		setBorder(null);

		super.getViewport().add(pane);
	}


	private void setFontSize(int fontSize)
	{
		pane.setFont(new Font("Verdana", Font.PLAIN, fontSize));
	}


	public void clear()
	{
		try
		{
			doc.remove(0, doc.getLength());
			entryLengths.clear();
		} catch (final BadLocationException err)
		{
			log.error("Could not clear text pane.", err);
		}
	}


	public void append(final String text, final AttributeSet aset)
	{
		try
		{
			doc.insertString(doc.getLength(), text, aset);

			// If there are too much entries: Remove the first!
			if (entryLengths.size() > maxCapacity)
			{
				final Integer end = entryLengths.pollFirst();
				if (end != null)
				{
					doc.remove(0, end);
				}
			}

			// Memorize the message-length
			entryLengths.add(text.length());

			pane.setCaretPosition(doc.getLength());
		} catch (final BadLocationException err)
		{
			log.error("Could not append text.", err);
		}
	}


	public void prepend(final String text, final AttributeSet aset)
	{
		try
		{
			doc.insertString(0, text, aset);

			// If there are too much entries: Remove the last!
			if (entryLengths.size() > maxCapacity)
			{
				final Integer end = entryLengths.pollLast();
				if (end != null)
				{
					doc.remove(doc.getLength(), end);
				}
			}

			// Memorize the message-length
			entryLengths.addFirst(text.length());

			pane.setCaretPosition(0);
		} catch (final BadLocationException err)
		{
			log.error("Could not prepend text.", err);
		}
	}


	/**
	 * Number of characters in doc
	 *
	 * @return
	 */
	public int getLength()
	{
		return doc.getLength();
	}
}
