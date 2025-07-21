/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra;

import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.ESumatraViewType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.infonode.docking.View;
import net.infonode.docking.ViewSerializer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;


@Log4j2
@RequiredArgsConstructor
class WindowViewSerializer implements ViewSerializer
{
	private final Collection<ASumatraView> views;


	@Override
	public void writeView(final View view, final ObjectOutputStream out) throws IOException
	{
		String title = view.getTitle();
		for (ESumatraViewType viewType : ESumatraViewType.values())
		{
			if (viewType.getTitle().equals(title))
			{
				out.writeInt(viewType.getId());
				return;
			}
		}
	}


	@Override
	public View readView(final ObjectInputStream in) throws IOException
	{
		int id = in.readInt();

		for (ASumatraView sumatraView : views)
		{
			if (sumatraView.getType().getId() == id)
			{
				return sumatraView.getView();
			}
		}
		ESumatraViewType type = ESumatraViewType.fromId(id);
		log.warn("View {} with id {} has been removed.", type, id);
		return null;
	}
}
