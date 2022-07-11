package com.example.demo.libs.Model;

/*BoxDiscoverd Event*/
public class DiscoverEventArgs extends EventArgs
{
	public BoxController box;
	public byte[] advdata;


	public DiscoverEventArgs(BleAccess bleAccess, BleDevice bleDevice)
	{
		box = new BoxController(bleAccess, bleDevice);
		/*Get HardWareDeviceCode (format **-**-**-**-**-**) */
		try
		{
			box.set_HardwareDeviceCode(bleDevice.get_DeviceCodeData());
			advdata = bleDevice.get_AdvertisedData();
		}
		catch (RuntimeException e)
		{
			/*Discovered Device is not box*/
			box.set_HardwareDeviceCode(null);
			advdata = null;
		}

	}

}