/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.components;

import javax.swing.JTextArea;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseMotionListener;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple Component to display descriptions on the UI. Will render with
 * a lower font weight and size.
 *
 * The reason to use a JTextArea is that it supports wrapping.
 *
 * @author Marius Messerschmidt <marius.messerschmidt@dlr.de>
 */
public class DescLabel extends JTextArea {

    public DescLabel(String text)
    {
        super(text);

        this.setBackground(new Color(0,0,0,0));
        this.setEditable(false);

        Map<TextAttribute, Object> attributes = new HashMap<>();

        attributes.put(TextAttribute.FAMILY, Font.SANS_SERIF);
        attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_SEMIBOLD);
        attributes.put(TextAttribute.SIZE, 12);

        this.setFont(Font.getFont(attributes));

        this.setLineWrap(true);
        this.setWrapStyleWord(true);
    }

    @Override
    public synchronized void addMouseMotionListener(final MouseMotionListener mouseMotionListener) {
        /*
        We do not want selection / drag+drop on a label, so override all motions by simply not
        adding them
        */
    }

}
