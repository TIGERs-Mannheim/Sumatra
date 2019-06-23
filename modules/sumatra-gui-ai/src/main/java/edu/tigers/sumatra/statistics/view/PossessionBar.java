/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.statistics.view;

import edu.tigers.sumatra.ids.ETeamColor;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

public class PossessionBar extends JPanel {

    private double shareY = 0.0;
    private double shareB = 0.0;

    public void setTeamShare(ETeamColor team, double value)
    {
        if(team == ETeamColor.YELLOW)
            shareY = value;
        else
            shareB = value;

        this.repaint();
        this.revalidate();
    }

    public void setTeamShareBoth(double shareY, double shareB)
    {
        this.shareB = shareB;
        this.shareY = shareY;

        this.repaint();
        this.revalidate();
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Color old = g.getColor();


        Dimension d = this.getSize();

        double wB = d.width * shareB;
        double wY = d.width * shareY;
        double wR = d.width * (1.0 - (shareB + shareY));

        g.setColor(ETeamColor.YELLOW.getColor());
        g.fillRect(0, 0, (int) wY, d.height);
        g.setColor(old);
        g.drawRect(0, 0, (int) wY, d.height);

        g.setColor(Color.WHITE);
        g.fillRect((int) wY, 0, (int) wR, d.height);
        g.setColor(old);
        g.drawRect((int) wY, 0, (int) wR, d.height);

        g.setColor(ETeamColor.BLUE.getColor());
        g.fillRect((int) (wY + wR), 0, (int) wB, d.height);
        g.setColor(old);
        g.drawRect((int) (wY + wR), 0, (int) wB, d.height);
    }

    @Override
    public Dimension getMinimumSize()
    {
        return new Dimension(200, 10);
    }


}
