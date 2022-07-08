package SmartLockerSdk;

import java.time.*;

public class StatusEventArgs extends EventArgs
{
	public boolean status;

	public StatusEventArgs(boolean newStatus)
	{
		status = newStatus;
	}
}