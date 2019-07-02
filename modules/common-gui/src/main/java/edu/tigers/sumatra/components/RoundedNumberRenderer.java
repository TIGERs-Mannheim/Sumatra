package edu.tigers.sumatra.components;

import java.text.DecimalFormat;

import javax.swing.table.DefaultTableCellRenderer;


public class RoundedNumberRenderer extends DefaultTableCellRenderer
{
	private DecimalFormat format = new DecimalFormat("#.###");


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
