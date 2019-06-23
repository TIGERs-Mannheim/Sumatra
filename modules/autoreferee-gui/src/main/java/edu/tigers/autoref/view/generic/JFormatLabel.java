/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 12, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.generic;

import javax.swing.*;


/**
 * @author "Lukas Magel"
 * @param <T>
 */
public class JFormatLabel<T> extends JLabel
{
	
	/**  */
	private static final long	serialVersionUID	= 4937004578155936454L;
	
	private LabelFormatter<T>	formatter;
	private T						value;
	
	
	/**
	 * @param formatter
	 */
	public JFormatLabel(final LabelFormatter<T> formatter)
	{
		this.formatter = formatter;
	}
	
	
	/**
	 * @param value
	 */
	public void setValue(final T value)
	{
		this.value = value;
		updateAppearance();
	}
	
	
	/**
	 * 
	 */
	private void updateAppearance()
	{
		formatter.formatLabel(value, this);
	}
	
	
	/**
	 * @return
	 */
	public T getValue()
	{
		return value;
	}
	
	
	/**
	 * @return
	 */
	public LabelFormatter<T> getFormatter()
	{
		return formatter;
	}
	
	
	/**
	 * @param formatter
	 */
	public void setFormatter(final LabelFormatter<T> formatter)
	{
		this.formatter = formatter;
	}
	
	/**
	 * @author "Lukas Magel"
	 * @param <T>
	 */
	@FunctionalInterface
	public interface LabelFormatter<T>
	{
		
		/**
		 * @param value
		 * @param label
		 */
		void formatLabel(T value, JLabel label);
	}
}
