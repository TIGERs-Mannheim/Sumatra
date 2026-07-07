package com.github.g3force.configurable;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum EConfigUnit
{

	// DISTANCE
	DISTANCE_MM("mm"),
	DISTANCE_M("m"),
	BOT_RADIUS("bot radius"),

	// ANGLE
	ANGLE_RAD("rad"),
	ANGLE_DEG("deg"),

	// VELOCITY
	VELOCITY_M("m/s"),
	VELOCITY_MM("mm/s"),
	VELOCITY_ANGULAR_RAD("rad/s"),
	RPM("rpm"),

	// ACCELERATION
	ACCELERATION_MM("mm/s^2"),
	ACCELERATION_M("m/s^2"),
	ACCELERATION_ANGULAR_RAD("rad/s^2"),

	// TIME
	TIME_S("s"),
	TIME_MS("ms"),
	HZ("Hz"),
	FPS("fps"),

	// SIMPLE TYPES
	// DOUBLE
	PERCENTAGE("% (0-1.0)"),
	FACTOR("factor"),
	// INT
	COUNT("count"),
	// BOOLEAN
	BOOLEAN(""),

	// STRING
	URL("URL"),
	BOT_ID("BotID"),
	VEC2("2D vector"),

	// STOCHASTIC
	STD_DEVIATION_MM("mm"),
	VARIANCE_MM("mm^2"),
	VARIANCE_RAD("rad^2"),

	// DEFAULT
	NO_UNIT("-");


	private final String stringRepresentation;
}
