package com.example.demo.libs.Model;

public class StatusEventArgs extends EventArgs
{
	public boolean status;

	public StatusEventArgs(boolean newStatus)
	{
		status = newStatus;
	}
}