package com.example.demo.libs.Model;

/*Define Command Protocol*/
public class BleCommandProtocol
{
	/* private member */
	private byte[] sendBuffer;
	private int sendPacketLen;
	private BleCommand currCommand = BleCommand.values()[0];
	private boolean created;
	private int sendOffset;
	private int sendItemNum;
	private short sendPayloadLen;
	private byte[] recvBuffer;
	private boolean received;
	private int recvOffset;
	private int recvItemNum;
	private short recvPayloadLen;
	private int recvPackeLen;
	private int recvNextItemNo;

	/* private method */
	public final BleCommand get_currCommand()
	{
		return currCommand;
	}
	private final void set_currCommand(BleCommand value)
	{
		currCommand = value;
	}

	/* public method */
	public final byte[] get_SendBuffer()
	{
		return sendBuffer;
	}
	public final void set_SendBuffer(byte[] value)
	{
		sendBuffer = value;
	}

	public final int get_SendPacketLen()
	{
		return sendPacketLen;
	}
	public final void set_SendPacketLen(int value)
	{
		sendPacketLen = value;
	}

	public final BleCommand get_CurrCommand()
	{
		return get_currCommand();
	}
	public final void set_CurrCommand(BleCommand value)
	{
		set_currCommand(value);
	}

	/*Constructor*/
	public BleCommandProtocol(BleCommand command)
	{
		int sendBuffLen = 512;
		int recvBuffLen = 4096;
		sendBuffer = new byte[sendBuffLen];
		recvBuffer = new byte[recvBuffLen];
		created = false;
		sendBuffer[0] = (byte)command.getValue(); //Set Command type(first byte of the sendBuffer)
		set_currCommand(command);
		sendOffset = 4;
		sendItemNum = 0;
		sendPayloadLen = 0;
		sendPacketLen = 4;

		received = false;
		recvOffset = 0;
		recvItemNum = 0;
		recvPayloadLen = 0;
		recvPackeLen = 0;
		recvNextItemNo = 0;
	}

	/*Add item to the sendBuffer(Command packet) -- 1*/
	public final int AddItem(BleItemId itemId, byte data)
	{
		short itemLen = (short)(1 + 2);
		int ret = 0;

		if (created == false)
		{
			if ((sendItemNum < 255) && ((sendPacketLen + itemLen) < sendBuffer.length))
			{
				sendBuffer[sendOffset++] = (byte)itemId.getValue();
				sendBuffer[sendOffset++] = 1;
				sendBuffer[sendOffset++] = data;
				sendPayloadLen += itemLen;
				sendPacketLen += itemLen;
				sendItemNum++;
			}
			else
			{
				ret = -1;
			}
		}
		else
		{
			ret = -1;
		}

		return (ret);
	}

	/*Add item to the sendBuffer(Command packet) -- 2*/
	public final int AddItem(BleItemId itemId, byte[] data)
	{
		short itemLen = (short)(data.length + 2);
		int ret = 0;

		if (created == false)
		{
			if ((sendItemNum < 255) && (data.length <= 256) && ((sendPacketLen + itemLen) < sendBuffer.length))
			{
				sendBuffer[sendOffset++] = (byte)itemId.getValue();
				sendBuffer[sendOffset++] = (byte)data.length;
				Utility.BlockCopy(data, 0, sendBuffer, sendOffset, data.length);
				sendOffset += data.length;
				sendPayloadLen += itemLen;
				sendPacketLen += itemLen;
				sendItemNum++;
			}
			else
			{
				ret = -1;
			}
		}
		else
		{
			ret = -1;
		}

		return (ret);
	}

	/*Create Packet(set payload length)*/
	public final int CreatePacket()
	{
		byte[] sVal = new byte[2];
		int ret = 0;

		if (created == false)
		{
			sendBuffer[1] = (byte)sendItemNum;
			sVal = Utility.GetBytes(sendPayloadLen);
			Utility.Reverse(sVal);
			Utility.BlockCopy(sVal, 0, sendBuffer, 2, 2);
			created = true;
			ret = sendPacketLen;
		}
		else
		{
			ret = -1;
		}

		return (ret);
	}

	/*Set response data to recvBuffer*/
	public final int SetResponse(byte[] data, int len)
	{
		int ret = 0;
		byte[] sVal = new byte[2];

		if ((created == true) && (len > 0) && (len <= recvBuffer.length))
		{
			int tmp = (int)data[0] + 0x80;

			if ((tmp < 0x80) && (((byte)tmp == sendBuffer[0]) || (data[0] == 0xFE)))
			{
				Utility.BlockCopy(data, 0, recvBuffer, 0, len);
				recvPackeLen = len;
				received = true;

				recvItemNum = recvBuffer[1];
				Utility.BlockCopy(recvBuffer, 2, sVal, 0, 2);
				recvPayloadLen = Utility.ToInt16(sVal, 0);
				recvOffset = 4;
			}
			else
			{
				/*Response data(ItemId) error*/
				ret = -1;
			}
		}
		else
		{
			ret = -1;
		}

		return (ret);
	}

	public final boolean HasResponse()
	{
		return (received);
	}

	public final int GetItemNum()
	{
		int ret = 0;

		if (received == true)
		{
			ret = recvItemNum;
		}
		else
		{
			ret = -1;
		}

		return (ret);
	}

	/*Get item from response*/
	public final BleCommandItem GetNextItem()
	{
		int len = 0;
		BleCommandItem item = null;

		if (received == true)
		{
			if (recvNextItemNo < recvItemNum)
			{
				item = new BleCommandItem();
				item.set_id(BleItemId.forValue(recvBuffer[recvOffset++]));
				len = Utility.toUnsigned(recvBuffer[recvOffset++]);

				/*Data length 256Byte*/
				if (len == 0)
				{
					len = 256;
				}
				else
				{
					/*Do Nothing*/
				}

				item.set_data(new byte[len]);
				Utility.BlockCopy(recvBuffer, recvOffset, item.get_data(), 0, len);
				recvOffset += len;
				recvNextItemNo++;
			}
			else
			{
				/*Do Nothing*/
				;
			}
		}
		else
		{
			/*Do Nothing*/
		}

		return (item);
	}

	/*Get Selected Status Bit from Status Byte*/
	public static boolean GetStatusBit(byte[] status, BleBoxStatus statusBit)
	{
		boolean bret = false;
		int bit = statusBit.getValue();

		if ((bit >= 0) && (bit < 8))
		{
			if ((status[1] & (0x01 << bit)) != 0)
			{
				bret = true;
			}
			else
			{
				/*false --  Do Nothing*/
			}
		}
		else if ((bit >= 8) && (bit < 16))
		{
			if ((status[0] & (0x01 << (bit - 8))) != 0)
			{
				bret = true;
			}
			else
			{
				/*false --  Do Nothing*/
			}
		}

		return (bret);
	}

	/*Set Selected Status Bit to Status Byte*/
	public final void SetStatusBit(byte[] status, BleBoxStatus statusBit)
	{
		int bit = statusBit.getValue();

		if ((bit >= 0) && (bit < 8))
		{
			status[1] |= (byte)(0x01 << bit);
			status[0] = 0x00;
		}
		else if ((bit >= 8) && (bit < 16))
		{
			status[1] = 0x00;
			status[0] = (byte)(0x01 << (bit - 8));
		}
		else
		{
			/*Do Nothing*/
		}
	}

}