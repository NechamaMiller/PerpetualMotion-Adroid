package com.example.perpetualmotion.idiotsDelight;

public class EmptyStackException extends RuntimeException{

	public EmptyStackException()
	{
		super("empty stack");
	}
	
	public EmptyStackException(String message)
	{
		super(message);
	}
	private static final long serialVersionUID = 1L;

}
