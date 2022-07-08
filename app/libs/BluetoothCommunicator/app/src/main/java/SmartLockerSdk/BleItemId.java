package SmartLockerSdk;

public enum BleItemId
{
	ErrorCode(0x00),
	Token(0x01),
	Hash(0x02),
	FirmVersion(0x11),
	BoxTime(0x22),
	Status(0x23),
	BatteryStatus(0x25),
	AttemptMax(0x28),
	InputImpossibleTime(0x29),
	AutoClose(0x2A),
	LastAttempt(0x2C),
	WarnBatteryThreshold(0x2D),
	AdvertiseInterval(0x2E),
	StrengthPower(0x2F),
	ChannelUsed(0x31),
	CipheredConfig(0x32),
	WarnEventNum(0x33),
	ConnTimeOut(0x34),
	AESKey(0x41),
	CipheredPassword(0x43),
	IsDeletePassword(0x44),
	PasswordDeleteTime(0x45),
	EventDeleteNum(0x65),
	CipheredEvent(0x68),
	LastEventDate(0x69),
	IsDeleteEvents(0x6A),
	UpdateFirmware(0xFE),
	StatusNotification(0xFF);

	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, BleItemId> mappings;
	private static java.util.HashMap<Integer, BleItemId> getMappings()
	{
		if (mappings == null)
		{
			synchronized (BleItemId.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, BleItemId>();
				}
			}
		}
		return mappings;
	}

	private BleItemId(int value)
	{
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static BleItemId forValue(int value)
	{
		return getMappings().get(value);
	}
}