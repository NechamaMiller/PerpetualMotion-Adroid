package com.example.perpetualmotion.idiotsDelight;

public class Card
{
	private Rank rank;
	private Suit suit;
	
	/**
	 * constructor
	 * @param rank value for card's rank - must be valid from list of Rank enums
	 * @param suit value for card's suit - must be valid from list of Suit enums
	 * @throws NullPointerException if rank, suit or color is null
	 */ 
	public Card(Rank rank, Suit suit)
	{
		if (rank==null)
		{
			throw new NullPointerException("Rank cannot be null.");
		}
		if (suit==null)
		{
			throw new NullPointerException("Suit cannot be null.");
		}

		this.rank = rank;
			
		this.suit = suit;

	}
	
	public Rank getRank()
	{
		return rank;
	}
	
	public Suit getSuit()
	{
		return suit;
	}

	public int getColor() {return suit.getColor();}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append(rank.toString().toLowerCase());
		sb.append(" of ");
		sb.append(suit.toString().toLowerCase());
		
		return sb.toString();
	}

	public Card copy()
	{
		return new Card(rank, suit);
	}
}
