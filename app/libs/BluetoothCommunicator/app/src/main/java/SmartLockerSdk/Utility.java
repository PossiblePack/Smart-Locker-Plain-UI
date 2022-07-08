package SmartLockerSdk;

import android.util.Log;

import java.nio.ByteBuffer;

public class Utility
{
	private static final String LOG_TAG = "SLSDK";
	private static final String LOG_CLASS = "[Utility]";

    public static final void Reverse(byte[] data)
    {
        if (null == data)
        {
			BleLog.e(LOG_TAG, LOG_CLASS + "[Reverse][E] data is null.");
            return;
        }

        for (int i = 0, j = (data.length - 1); i < j; i++, j--)
        {
            byte tmp = data[i];
            data[i] = data[j];
            data[j] = tmp;
        }
    }

    public static final void BlockCopy(byte[] src, int srcOffset, byte[] dst, int dstOffset, int count)
    {
        if (null == src)
        {
			BleLog.e(LOG_TAG, LOG_CLASS + "[BlockCopy][E] src is null.");
            return;
        }
        if (null == dst)
        {
			BleLog.e(LOG_TAG, LOG_CLASS + "[BlockCopy][E] dst is null.");
            return;
        }

        for (int i = srcOffset, j = dstOffset, k = 0; (k < count) && (i < src.length); i++, j++, k++ )
        {
            dst[j] = src[i];
        }
    }

	public static final byte[] GetBytes(byte value)
	{
		byte[] buf = new byte[1];
		buf[0] = value;
		return buf;
	}

	public static final byte[] GetBytes(short value)
	{
		byte[] buf = new byte[2];
		buf[0] = (byte)value;
		buf[1] = (byte)(value >> 8);
		return buf;
	}

	public static final byte[] GetBytes(int value)
	{
		byte[] buf = new byte[4];
		buf[0] = (byte)value;
		buf[1] = (byte)(value >> 8);
		buf[2] = (byte)(value >> 16);
		buf[3] = (byte)(value >> 24);
		return buf;
	}

	public static final byte[] GetBytes(boolean value)
	{
		if (value)
		{
			return GetBytes((byte)1);
		}
		else {
			return GetBytes((byte) 0);
		}
	}

	public static final byte[] GetBytes(Integer value)
	{
		return GetBytes((int)value);
	}

	public static final short ToInt16(byte[] value, int startIndex)
	{
        if (null == value)
        {
			BleLog.e(LOG_TAG, LOG_CLASS + "[ToInt16][E] value is null.");
            return 0;
        }

		return ByteBuffer.wrap(value, startIndex, 2).getShort();
	}

	public static final int ToInt32(byte[] value, int startIndex)
	{
        if (null == value)
        {
			BleLog.e(LOG_TAG, LOG_CLASS + "[ToInt32][E] value is null.");
            return 0;
        }

		return ByteBuffer.wrap(value, startIndex, 4).getInt();
	}

	public static final String toString(byte[] value, int len)
	{
        if (null == value)
        {
			BleLog.e(LOG_TAG, LOG_CLASS + "[toString][E] value is null.");
            return null;
        }

		String str = new String();
		int i = len;
		for (byte c : value)
		{
			if (0 >= i)
			{
				break;
			}
			str += String.format("%02X", (Byte)c);
			i--;
		}

		return str;
	}

	public static final int toUnsigned(byte value)
	{
		int tmp = value; /* byte -128<>127 --> 0<>255 */
		if (0 > tmp)
		{
			tmp += 256;
		}

		return tmp;
	}
}