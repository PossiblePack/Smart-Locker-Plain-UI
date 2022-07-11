package com.example.demo.libs.Model;

import java.util.HashMap;

public enum BleChannel
{
	NO_CHANNEL(0x00),
	CHANNEL_37(0x01),
	CHANNEL_38(0x02),
	CHANNEL_37_38(0x03),
	CHANNEL_39(0x04),
	CHANNEL_37_39(0x05),
	CHANNEL_38_39(0x06),
	CHANNEL_37_38_39(0x07);

	public static final int SIZE = Integer.SIZE;

	private int intValue;
	private static HashMap<Integer, BleChannel> mappings;
	private static HashMap<Integer, BleChannel> getMappings()
	{
		if (mappings == null)
		{
			synchronized (BleChannel.class)
			{
				if (mappings == null)
				{
					mappings = new HashMap<Integer, BleChannel>();
				}
			}
		}
		return mappings;
	}

	private BleChannel(int value)
	{
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static BleChannel forValue(int value)
	{
		return getMappings().get(value);
	}
}