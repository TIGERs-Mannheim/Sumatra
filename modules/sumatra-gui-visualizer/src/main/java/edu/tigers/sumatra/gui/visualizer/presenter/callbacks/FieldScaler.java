package edu.tigers.sumatra.gui.visualizer.presenter.callbacks;

import java.awt.Point;


@FunctionalInterface
public interface FieldScaler
{
	void scale(Point origin, double factor);
}
