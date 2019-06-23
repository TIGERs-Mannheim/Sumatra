/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 27, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.messaging.topics;

import com.google.protobuf.Message;

import edu.dhbw.mannheim.tigers.sumatra.util.messaging.MessageType;


/**
 * Interface that all topic classes or enums should implement. It will be accessed in Messaging System.
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public interface ITopics
{
	/**
	 * Used to get the topic name
	 * @return the mqttTopic
	 */
	String getName();
	
	
	/**
	 * This gives information about the message type. (@see
	 * @link{edu.dhbw.mannheim.tigers.sumatra.util.messaging.MessageType )
	 * @return the type
	 */
	MessageType getType();
	
	
	/**
	 * Returns a Builder instance for the prototype of this topic.<br />
	 * It will be used to create a message object from received binary data.
	 * 
	 * @return the protoType
	 */
	Message.Builder getProtoType();
}
