package edu.tigers.sumatra.components;

import javax.swing.table.DefaultTableCellRenderer;
import java.text.DecimalFormat;


public class RoundedNumberRenderer extends DefaultTableCellRenderer
{
	private final DecimalFormat format;


	public RoundedNumberRenderer(String pattern)
	{
		format = new DecimalFormat(pattern);
	}


	@Override
	protected void setValue(final Object o)
	{
		if (o != null)
		{
			super.setValue(format.format(o));
		} else
		{
			super.setValue("N/A");
		}
	}
}
