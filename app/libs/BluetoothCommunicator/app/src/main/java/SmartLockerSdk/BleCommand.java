package SmartLockerSdk;

public enum BleCommand
{
	UpdateToken(0x00),
	GetConfiguration(0x01),
	SetConfiguration(0x02),
	GetDateTime(0x03),
	SetDateTime(0x04),
	GetBatteryStatus(0x05),
	ChangeKey(0x07),
	GetApiVendor(0x08),
	GetStatus(0x0A),
	IsDoorOpened(0x0C),
	IsLocked(0x11),
	Unlock(0x12),
	Lock(0x13),
	GetEvents(0x17),
	DeleteEvents(0x18),
	ResetDevice(0x1A),
	SetPassword(0x1B),
	GetPassword(0x1C),
	UpdateFirmware(0xFE),
	StatusNotification(0xFF);

	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, BleCommand> mappings;
	private static java.util.HashMap<Integer, BleCommand> getMappings()
	{
		if (mappings == null)
		{
			synchronized (BleCommand.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, BleCommand>();
				}
			}
		}
		return mappings;
	}

	private BleCommand(int value)
	{
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static BleCommand forValue(int value)
	{
		return getMappings().get(value);
	}
}