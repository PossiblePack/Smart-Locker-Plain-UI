package SmartLockerSdk;

import java.util.*;
import java.time.*;

public class VendorApi
{
	private String Name;
	public final String get_Name()
	{
		return Name;
	}
	public final void set_Name(String value)
	{
		Name = value;
	}
	private String firmwareVersion;
	public final String get_firmwareVersion()
	{
		return firmwareVersion;
	}
	public final void set_firmwareVersion(String value)
	{
		firmwareVersion = value;
	}
}