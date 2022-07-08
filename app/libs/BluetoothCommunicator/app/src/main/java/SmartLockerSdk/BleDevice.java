package SmartLockerSdk;

import android.bluetooth.BluetoothDevice;

import java.util.*;

public class BleDevice
{
	private UUID Id;
	public final UUID get_Id()
	{
		return Id;
	}
	public final void set_Id(UUID value)
	{
		Id = value;
	}
	private byte[] AdvertisedData;
	public final byte[] get_AdvertisedData()
	{
		return AdvertisedData;
	}
	public final void set_AdvertisedData(byte[] value)
	{
		AdvertisedData = value;
	}
	private String DeviceCodeData;
	public final String get_DeviceCodeData()
	{
		return DeviceCodeData;
	}
	public final void set_DeviceCodeData(String value)
	{
		DeviceCodeData = value;
	}

	private BluetoothDevice Device;
	public final BluetoothDevice get_Device()
	{
		return Device;
	}
	public final void set_Device(BluetoothDevice value)
	{
		Device = value;
	}
}