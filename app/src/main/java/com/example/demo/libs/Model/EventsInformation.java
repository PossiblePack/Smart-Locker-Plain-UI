package com.example.demo.libs.Model;

import java.util.Date;

public class EventsInformation
{
	private Date lastEventDate = new Date();
	public final Date get_lastEventDate()
	{
		return lastEventDate;
	}
	public final void set_lastEventDate(Date value)
	{
		lastEventDate = value;
	}
	private byte[] events;
	public final byte[] get_events()
	{
		return events;
	}
	public final void set_events(byte[] value)
	{
		events = value;
	}
}