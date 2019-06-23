/*
 * *********************************************************
 * Copyright (c) 2009 DHBW Mannheim - Tigers Mannheim
 * Project: tigers-centralSoftware
 * Date: 29.08.2009
 * Authors: Bernhard Perun <bernhard.perun@googlemail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import edu.tigers.sumatra.model.SumatraModel;


/**
 * Information about central software.
 */
public class AboutDialog extends JDialog
{
	// --------------------------------------------------------------------------
	// --- instance-variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= 3461893941869192656L;
	private static ClassLoader	classLoader			= AboutDialog.class.getClassLoader();
	
	
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
		setResizable(false);
		setTitle("About");
		setLayout(new BorderLayout());
		setModal(true);
		
		// --- alignment: center on screen ---
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenDimension = tk.getScreenSize();
		this.setLocation((int) (screenDimension.getWidth() - getWidth()) / 2,
				(int) (screenDimension.getHeight() - getHeight()) / 2);
		
		// --- heading ---
		JLabel heading = new JLabel("Tigers Mannheim - Sumatra", SwingConstants.CENTER);
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
		text.setText("<html>Version: " + SumatraModel.getVersion() + "<br>" + "Team: Tigers Mannheim<br>"
				+ "University: DHBW Mannheim<br>" + "Date: July 2009 - today" + "</html>");
		this.add(text, BorderLayout.SOUTH);
		
	}
}
