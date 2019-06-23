/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.testplays.commands;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.tigers.sumatra.testplays.util.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class PathCommand extends ACommand {

	@JsonProperty("path")
	private List<Point> points;

	/**
	 * Creates a new path command with the given vectors
	 * 
	 * @param path
	 */
	public PathCommand(List<Point> path) {
		super(CommandType.PATH);

		this.points = path;
	}

	/**
	 * Creates a new empty path command.
	 */
	public PathCommand() {

		this(new ArrayList<>());
	}

	public List<Point> getPoints() {

		return points;
	}

	public void setPoints(List<Point> points) {

		this.points = points;
	}

	@JsonIgnore
	public int getNumberOfPoints() {

		if (points != null) {
			return points.size();
		}

		return 0;
	}

	@Override
	public String toString() {

		String name = getCommandType().name();
		if (points != null) {
			name += " (" + points.size() + ")";
		} else {
			name += " (not initialized)";
		}
		return name;
	}
}
