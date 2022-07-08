package SmartLockerSdk;

import android.content.Context;
import android.util.Log;

import java.util.*;

public class BoxManager
{
	private static final String LOG_TAG = "SLSDK";
	private static final String LOG_CLASS = "[BoxManager]";

	public String apiVersion = CoreAssembly.version.toString();
	public ArrayList<BoxController> discoveredBoxControllers = new ArrayList<BoxController>();
	private BleAccess bleAccess;
	@FunctionalInterface
	public interface DiscoverEventHandler
	{
		void invoke(Object sender, DiscoverEventArgs e);
	}
	public Event<DiscoverEventHandler> OnBoxControllerDiscovered = new Event<DiscoverEventHandler>();
	public Event<EventHandler<EventArgs>> OnScanFinished = new Event<EventHandler<EventArgs>>();

	/*Constracter*/
	public BoxManager(Context context) throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[BoxManager]");

		/* Create BleAccess */
		bleAccess = new BleAccess(context);
		/*Set delegate functions*/
		bleAccess.deviceDiscoveredCallback = new DeviceDiscoverCallback();
		bleAccess.scanTimeoutCallback = new ScanTimeoutCallback();
	}

	public final String getApiVersion()
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[getApiVersion] apiVersion=" + apiVersion);

		return apiVersion;
	}
	public final void setApiVersion(String value)
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[setApiVersion] value=" + value);

		apiVersion = value;
	}

	public final ArrayList<BoxController> getDiscoveredBoxControllers()
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[getDiscoveredBoxControllers] discoveredBoxControllers.size()=" + discoveredBoxControllers.size());

		return discoveredBoxControllers;
	}
	public final void setDiscoveredBoxControllers(ArrayList<BoxController> value)
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[setDiscoveredBoxControllers] value.size()=" + value.size());

		discoveredBoxControllers = value;
	}


	public final void StartScanBoxControllers() throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[StartScanBoxControllers]");

		StartScanBoxControllers(0);
	}

	public final void StartScanBoxControllers(int timeout) throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[StartScanBoxControllers] timeout=" + timeout);

		/*Start scanning*/
		bleAccess.Scan(timeout);
	}

	public final void StopScanBoxControllers() throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[StopScanBoxControllers]");

		bleAccess.StopScan();
	}

	public final String GetSdkApiVersion()
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[GetSdkApiVersion] apiVersion=" + apiVersion);

		return (apiVersion);
	}

	private void DeviceDiscover(BleAccess bleAccess, BleDevice bleDevice)
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[DeviceDiscover] bleDevice=" + bleDevice.toString());

		DiscoverEventArgs e = new DiscoverEventArgs(bleAccess, bleDevice);

		if (ValidateBox(e.box, e.advdata))
		{
			BoxControllerDiscovered(e);
		}
	}

	public class DeviceDiscoverCallback implements BleAccess.DeviceDiscoveredCallback
	{
		public void invoke(BleAccess bleAccess, BleDevice bleDevice)
		{
			DeviceDiscover(bleAccess, bleDevice);
		}
	}

	/*Validate founeded BleDevice*/
	private boolean ValidateBox(BoxController box, byte[] rawData)
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[ValidateBox] box=" + box.get_HardwareDeviceCode() + " rawData.length=" + rawData.length);

		try
		{
			/*Decord ProtoBuffer*/
			box.setAdvertisedMessage(decodeAdvertisedData(rawData));
		}
		catch (RuntimeException e)
		{
			BleLog.e(LOG_TAG, LOG_CLASS + "[ValidateBox][E] box.setAdvertisedMessage() error.");
			return false;
		}

		return true;
	}

	/*Decord advertised data*/
	private Advertising decodeAdvertisedData(byte[] rawData)
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[decodeAdvertisedData] rawData.length=" + rawData.length);

		Advertising advertising = new Advertising();
		advertising.set_status(new BoxStatus());
		advertising.set_type(BoxType.forValue(rawData[0]));
		advertising.set_firmVersion(new byte[] {rawData[1], rawData[2], rawData[3]});
		advertising.set_isDoorOpen(((int)rawData[5] & 0x01) != 0);
		advertising.set_isLockLock(((int)rawData[5] & 0x02) != 0);
		advertising.set_isTooMuchEvent(((int)rawData[5] & 0x04) != 0);
		advertising.set_isPasswordSet(((int)rawData[5] & 0x08) != 0);
		advertising.set_passwordNum((byte)(((int)rawData[5] >> 4) & 0x0F));
		advertising.set_remainingBattery(Utility.toUnsigned(rawData[6])); /* byte -128<>127 --> 0<>255 */

		/* for Debug Log */
		String tmptype;
		if (null == advertising.get_type())
		{
			tmptype = "Unknown";
		}
		else
		{
			tmptype = advertising.get_type().toString();
		}
		BleLog.d(LOG_TAG, LOG_CLASS + "[decodeAdvertisedData] Advertising={type=" + tmptype +
				" firmVersion.length=" + advertising.get_firmVersion().length +
				" firmVersion=" + advertising.get_firmVersion().toString() +
				" remainingBattery=" + advertising.get_remainingBattery() +
				" isDoorOpen=" + advertising.get_isDoorOpen() +
				" isLockLock=" + advertising.get_isLockLock() +
				" isTooMuchEvent=" + advertising.get_isTooMuchEvent() +
				" isPasswordSet=" + advertising.get_isPasswordSet() +
				" passwordNumSet=" + advertising.get_passwordNum() +
				"");

		return advertising;
	}

	private void ScanTimeout()
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[ScanTimeout]");

		ScanFinished(new EventArgs());
	}

	public class ScanTimeoutCallback implements BleAccess.ScanTimeoutCallback
	{
		public void invoke()
		{
			ScanTimeout();
		}
	}

	/*Event : OnBoxDiscovered*/
	protected void BoxControllerDiscovered(DiscoverEventArgs e)
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[BoxControllerDiscovered] Start");

		boolean isFind = false;
		for (BoxController boxController : discoveredBoxControllers)
		{
			if (e.box.get_HardwareDeviceCode().equals(boxController.hardwareDeviceCode))
			{
				isFind = true;
				break;
			}
		}
		if (!isFind)
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[BoxControllerDiscovered] add:" + e.box.get_HardwareDeviceCode());
			discoveredBoxControllers.add(e.box);
		}
		if (OnBoxControllerDiscovered != null)
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[BoxControllerDiscovered] OnBoxControllerDiscovered.listeners().size()=" + OnBoxControllerDiscovered.listeners().size());
			for (DiscoverEventHandler listener : OnBoxControllerDiscovered.listeners())
			{
				listener.invoke(this, e);
			}
		}
		else
		{
			/*Do Nothing*/
			BleLog.e(LOG_TAG, LOG_CLASS + "[BoxControllerDiscovered] OnBoxControllerDiscoveredk is null.");
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[BoxControllerDiscovered] End");
	}

	/*Event : ScanFinished*/
	protected void ScanFinished(EventArgs e)
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[ScanFinished] Start");

		if (OnScanFinished != null)
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[ScanFinished] OnScanFinished.listener().size()=" + OnScanFinished.listeners().size());
			for (EventHandler<EventArgs> listener : OnScanFinished.listeners())
			{
				listener.invoke(this, e);
			}
		}
		else
		{
			/*Do Nothing*/
			BleLog.e(LOG_TAG, LOG_CLASS + "[ScanFinished] OnScanFinished is null.");
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[ScanFinished] End");
	}
}