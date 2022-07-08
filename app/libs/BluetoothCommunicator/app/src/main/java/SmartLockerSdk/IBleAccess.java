package SmartLockerSdk;

import android.bluetooth.BluetoothDevice;

import java.util.*;

/*Interface*/
public interface IBleAccess
{
	void Scan(int timeout) throws BoxException;
	void StopScan() throws BoxException;
	void Connect(BluetoothDevice device) throws BoxException;
	void Disconnect(BluetoothDevice device) throws BoxException;
	Integer SendCommand(BleCommandProtocol cmd) throws BoxException;
	Integer UpdateFirmware(byte[] firmware) throws BoxException;
}