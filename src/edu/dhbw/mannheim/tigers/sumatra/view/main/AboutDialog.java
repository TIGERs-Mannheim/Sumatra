/* 
 * *********************************************************
 * Copyright (c) 2009 DHBW Mannheim - Tigers Mannheim
 * Project: tigers-centralSoftware
 * Date: 29.08.2009
 * Authors: Bernhard Perun <bernhard.perun@googlemail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.main;

import java.awt.*;
import javax.swing.*;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;

/**
 * Information about central software.
 */
@SuppressWarnings("serial")
public class AboutDialog extends JDialog
{
	// --------------------------------------------------------------------------
	// --- instance-variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	private static ClassLoader classLoader = AboutDialog.class.getClassLoader();
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Initializes about - dialog.
	 */
	public AboutDialog()
	{
		// --- window configuration ---
		this.setSize(300, 350);
		this.setResizable(false);
		this.setTitle("About");
		this.setLayout(new BorderLayout());
		this.setModal(true);

		// --- alignment: center on screen ---
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenDimension = tk.getScreenSize();
		this.setLocation((int)(screenDimension.getWidth()-this.getWidth())/2,
				(int)(screenDimension.getHeight()-this.getHeight())/2);
		
		// --- heading ---
		JLabel heading = new JLabel("Tigers Mannheim - Sumatra", JLabel.CENTER);
		heading.setFont(new Font("Dialog", Font.BOLD, 15));
		this.add(heading, BorderLayout.NORTH);

		// --- logo ---
		JLabel logo = new JLabel();
		ImageIcon iconNormal = new ImageIcon(classLoader.getResource("tigerIcon.png"));
		ImageIcon iconSmall = new ImageIcon(iconNormal.getImage().getScaledInstance(200, 200, Image.SCALE_DEFAULT));
		logo.setPreferredSize(new Dimension(300, 250));
		logo.setIcon(iconSmall);
		logo.setHorizontalAlignment(SwingConstants.CENTER);
		logo.setVerticalAlignment(SwingConstants.CENTER);
		this.add(logo, BorderLayout.CENTER);
		
		// --- text ---
		JLabel text = new JLabel();
		text.setText(
				"<html>Version: " + SumatraModel.VERSION + "<br>" +
				"Team: Tigers Mannheim<br>" +
				"University: DHBW Mannheim<br>" +
				"Date: July 2009 - January 2011" +
				"</html>");
		this.add(text, BorderLayout.SOUTH);

	}
}
