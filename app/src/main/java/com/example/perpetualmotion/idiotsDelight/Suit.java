package com.example.perpetualmotion.idiotsDelight;

import android.graphics.Color;

public enum Suit
{
	HEARTS(Color.RED, '♥'),
	DIAMONDS(Color.RED, '♦'),
	CLUBS(Color.BLACK, '♣'),
	SPADES(Color.BLACK, '♠');
	private int color;
	private char character;

	private Suit(int color, char character)
	{
		this.color = color;
		this.character = character;
	}

	public int getColor()
	{
		return color;
	}

	public char getCharacter()
	{
		return character;
	}
}
