package SmartLockerSdk;

public class BleCommandItem
{
	private BleItemId id = BleItemId.values()[0];
	public final BleItemId get_id()
	{
		return id;
	}
	public final void set_id(BleItemId value)
	{
		id = value;
	}
	private byte[] data;
	public final byte[] get_data()
	{
		return data;
	}
	public final void set_data(byte[] value)
	{
		data = value;
	}
}