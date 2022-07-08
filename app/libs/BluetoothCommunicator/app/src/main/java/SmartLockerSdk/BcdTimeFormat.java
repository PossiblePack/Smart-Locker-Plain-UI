package SmartLockerSdk;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;

/*Transform DateTime <--> BCD format*/

/** 
 Bcd time format.
*/
public class BcdTimeFormat
{
	/** 
	 Covert Datetime to bcd.
	 
	 @return The time to bcd.
	 @param dateTime Date time.
	*/
	public final byte[] DateTimeToBcd(Date dateTime)
	{
		int[] dtByte = new int[7];
		byte[] bcdByte = new byte[7];
		int i = 0;

		int year = Integer.parseInt(new SimpleDateFormat("yyyy").format(dateTime));
		int month = Integer.parseInt(new SimpleDateFormat("MM").format(dateTime));
		int day = Integer.parseInt(new SimpleDateFormat("dd").format(dateTime));
		int hour = Integer.parseInt(new SimpleDateFormat("HH").format(dateTime));
		int min = Integer.parseInt(new SimpleDateFormat("mm").format(dateTime));
		int sec = Integer.parseInt(new SimpleDateFormat("ss").format(dateTime));

		/*Convert to bcd type*/
		dtByte[0] = year / 100;
		dtByte[1] = year % 100;
		dtByte[2] = month;
		dtByte[3] = day;
		dtByte[4] = hour;
		dtByte[5] = min;
		dtByte[6] = sec;

		for (i = 0; i < dtByte.length; i++)
		{
			bcdByte[i] = IntToBcd(dtByte[i]);
		}

		return (bcdByte);
	}

	/** 
	 Convert bcds to datetime.
	 
	 @return datetime.
	 @param bcd datetime(bcd type).
	*/
	public final Date BcdToDateTime(byte[] bcd)
	{
		int[] dt = new int[7];
		int i = 0;

		/*Devide bcd*/
		for (i = 0; i < dt.length; i++)
		{
			dt[i] = BcdToInt(bcd[i]);
		}

		/*Convert to datetime type*/
		DateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String dtStr = (dt[0] * 100 + dt[1]) + "/" + dt[2] + "/" + dt[3] + " " + dt[4] + ":" + dt[5] + ":" +dt[6];
		Date dateTime  = new Date();
		try {
			dateTime = dateTimeFormat.parse(dtStr);
		}
		catch (Exception e)
		{
			/* Do Nothing */
		}

		return (dateTime);
	}

	private byte IntToBcd(int data)
	{
		int decVal_10 = 0;
		int decVal_1 = 0;
		byte hexVal = 0;

		decVal_10 = data / 10;
		decVal_1 = data % 10;

		hexVal = (byte)(decVal_10 * 16 + decVal_1);

		return (hexVal);
	}

	private int BcdToInt(byte data)
	{
		byte hexVal_16 = 0;
		byte hexVal_1 = 0;
		int decVal = 0;

		hexVal_16 = (byte)((data >>> 4) & 0x0F);
		hexVal_1 = (byte)(data & 0x0F);

		decVal = (hexVal_16 * 10 + hexVal_1);

		return (decVal);
	}
}