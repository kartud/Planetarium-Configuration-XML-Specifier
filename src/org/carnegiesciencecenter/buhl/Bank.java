package org.carnegiesciencecenter.buhl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.jdom.*;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class Bank implements Comparable
{	
	private String bankName;
	private String deviceType;	// The kind of device in the Bank (e.g., motor, slew, video projector, etc)
	private CommandAction defaultAction;	// Whether or not the bank should keep commands by default or discard
	private HashMap<String, SpiceCommand> spiceCommands;	// Spice Commands, indexed by name
	private HashMap<String, Unit> units;	// Units with projection location information, indexed by name
	// Names and values used to describe XML attributes or tags
	public static final String ACTION_ATTR = "action";
	public static final String COMMAND_ATTR = "command";
	public static final String DEVICE_TYPE_ATTR = "devicetype";
	public static final String NAME_ATTR = "name";
	public static final String KEEPING_MOST	= "keepmost";
	public static final String DISCARD_MOST = "discardmost";
	public static final String CONVERT_MOST = "convertmost";
	public static final String KEEP 	= "keep";
	public static final String DISCARD 	= "discard";
	public static final String CONVERT 	= "convert";
	
	// The different alternate actions that may be taken when seeing a given command
	public enum CommandAction
	{
		KEEP,			// Keep the command (i.e., do an exec)
		DISCARD,		// Discard the command
		CONVERT_TO_DS 	// Do an explicit conversion/update from SPICE to DS
	}
	
	/**
	 * Public ctor that sets the device name and default command action (keep or discard) for the Bank.
	 * @param name	Bank/device name (e.g., SRC2)
	 * @param devType	The kind of device in the Bank (e.g., motor, slew, videoprojector, etc)
	 * @param shouldKeepMost	true  if the default behavior for this set of devices should be to keep commands,
	 * 							false if the default behavior should be to discard them
	 */
	public Bank(String name, String devType, CommandAction action)
	{
		bankName = name;
		deviceType = devType.trim();
		spiceCommands = new HashMap<String, SpiceCommand>();
		units = new HashMap<String, Unit>();
		defaultAction = action;
	}
	
	/**
	 * Gets the JDOM XML Element that represents this Bank.
	 * @return	The JDOM XML Element that describes this Bank
	 */
	public Element getXML()
	{
		Element bankElement = new Element("bank");	// The root of the bank definition
		bankElement.setAttribute(NAME_ATTR, bankName);
		bankElement.setAttribute(DEVICE_TYPE_ATTR, deviceType);
		
		switch (defaultAction)
		{
		case KEEP:
			bankElement.setAttribute(ACTION_ATTR, KEEPING_MOST);
			break;
		case DISCARD:
			bankElement.setAttribute(ACTION_ATTR, DISCARD_MOST);
			break;
		case CONVERT_TO_DS:
			bankElement.setAttribute(ACTION_ATTR, CONVERT_MOST);
			break;
		}
		
		// Spice command information
		SpiceCommand[] commands = Arrays.copyOf(spiceCommands.values().toArray(), spiceCommands.size(), SpiceCommand[].class);
		for (SpiceCommand cmd : commands)	
			bankElement.addContent(cmd.getXML());	// Add the command
		
		// Unit information
		Unit[] unts = Arrays.copyOf(units.values().toArray(), units.size(), Unit[].class);
		Arrays.sort(unts);	// Alphabetize (for neatness)
		
		for (Unit u : unts)
			bankElement.addContent(u.getXML());
		
		return bankElement;
	}

	/**
	 * Returns the name of this Bank.
	 * @return The name of the Bank
	 */
	public String toString()
	{
		return bankName;
	}
	
	/**
	 * Returns the default action taken for the Bank's commands.
	 * @return	The default action to take.
	 */
	public CommandAction checkIfKeepingMost()
	{
		return defaultAction;
	}
	
	/**
	 * Sets a special, non-default action for a given command for the Bank
	 * @param name		The name of the command (e.g., "SEarch")
	 * @param action	Whether to keep, discard, or do an explicit conversion 
	 * 					of the command to SPICE
	 */
	public void setCommand(String name, CommandAction action)
	{
		spiceCommands.put(name, new SpiceCommand(name, action));
	}
	
	/**
	 * Sets/adds a unit for the given bank.
	 * @param unit	The unit to set.
	 */
	public void setUnit(Unit unit)
	{
		units.put(unit.getName(), unit);
	}
	
	/**
	 * Resets the Bank's list of specially treated commands to 
	 * the given set of commands.
	 * @param commands	The array of commands used to replace
	 * those currently recorded
	 */
	public void setSpecialCommands(SpiceCommand[] commands)
	{
		// (Re)Populate the commands map
		spiceCommands = new HashMap<String, SpiceCommand>();
		for (SpiceCommand cmd : commands)
			spiceCommands.put(cmd.getName(), cmd);
	}
	
	/**
	 * Resets the Bank's list of units the the one given.
	 * @param unts	The array of units used to replace those
	 * 				currently recorded
	 */
	public void setUnits(Unit[] unts)
	{
		// (Re)Populate the units map
		units = new HashMap<String, Unit>();
		for (Unit u : unts)
			units.put(u.getName(), u);
	}
	
	/**
	 * Returns an array containing the list of all commands
	 * associated with this Bank that get special treatment.
	 * @return 	An array containing the list of all commands
	 * 			associated with this Bank that get special treatment.
	 */
	public SpiceCommand[] getSpecialCommands()
	{		
		return Arrays.copyOf(spiceCommands.values().toArray(), spiceCommands.size(), SpiceCommand[].class);
	}
	
	/**
	 * Returns an array containing the list of all units
	 * associated with this Bank.
	 * @return	An array containing the list of all units
	 * 			associated with this Bank.
	 */
	public Unit[] getUnits()
	{
		return Arrays.copyOf(units.values().toArray(), units.size(), Unit[].class);
	}

	/**
	 * Returns the kind of device in the Bank (e.g., motor, slew, video projector, etc)
	 * @return The kind of device in the Bank (e.g., motor, slew, video projector, etc)
	 */
	public String getDeviceType()
	{
		return deviceType;
	}
	
	/**
	 * Sets the kind of device in the bank (e.g., Motor, Slew, Slide Projector, etc)
	 * @param devType the kind of device in the bank
	 */
	public void setDeviceType(String devType)
	{
		deviceType = devType.trim();
	}

	@Override
	public int compareTo(Object obj) 
	{
		if (obj == this)
			return 0;	// Equal
		if (obj.getClass() != Bank.class)
			return -1;	// Arbitrary for non-Banks
		
		return bankName.compareTo(((Bank)obj).bankName);
	}
}
