package com.example.demo.libs.Model;

public enum BleResponse
{
	UpdateToken(0x80),
	GetConfiguration(0x81),
	SetConfiguration(0x82),
	GetDateTime(0x83),
	SetDateTime(0x84),
	GetBatteryStatus(0x85),
	ChangeKey(0x87),
	GetApiVendor(0x88),
	GetStatus(0x8A),
	IsDoorOpened(0x8C),
	IsLocked(0x91),
	Unlock(0x92),
	Lock(0x93),
	GetEvents(0x97),
	DeleteEvents(0x98),
	ReseDevice(0x9A),
	SetPassword(0x9B),
	GetPassword(0x9C),
	UpdateFirmware(0xFE),
	StatusNotification(0xFF);

	public static final int SIZE = Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, BleResponse> mappings;
	private static java.util.HashMap<Integer, BleResponse> getMappings()
	{
		if (mappings == null)
		{
			synchronized (BleResponse.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, BleResponse>();
				}
			}
		}
		return mappings;
	}

	private BleResponse(int value)
	{
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static BleResponse forValue(int value)
	{
		return getMappings().get(value);
	}
}