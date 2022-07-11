package com.example.demo.libs.Model;

@FunctionalInterface
public interface EventHandler<T extends EventArgs>
{
	void invoke(Object sender, T e);
}