package com.example.perpetualmotion.idiotsDelight;

import java.util.ArrayList;
import java.util.Random;

public class Deck
{
	private ArrayList<Card> cards;
	private int numCards;
	private final int STARTING_NUM_CARDS = 52;
	
	public Deck()
	{
		numCards = STARTING_NUM_CARDS;
		
		cards = new ArrayList<Card>(numCards);
		
		for (int i=0; i<numCards; i++)
		{
			Rank rank;
			Suit suit;
			
			rank = Rank.values()[(i%13)];
			
			if(i<13)
			{
				suit = Suit.HEARTS;
			}
			else if(i<26)
			{
				suit = Suit.DIAMONDS;
			}
			else if (i<39)
			{
				suit = Suit.SPADES;
			}
			else
			{
				suit = Suit.CLUBS;
			}
			
			cards.add(i, new Card(rank,suit));
		}
	}
	
	public void shuffle()
	{
		Random rand = new Random();
		
		for (int i=0; i<numCards; i++)
		{
			int swapIndex = rand.nextInt(numCards);
			Card cardToSwap = cards.get(swapIndex);
			
			cards.set(swapIndex, cards.get(i));
			cards.set(i, cardToSwap);
		}
	}
	
	/**
	 * removes top card from deck and returns it
	 * @return top card of deck or null if deck is empty
	 */
	public Card deal()
	{
		if (!isEmpty())
		{
			return cards.remove(--numCards);
		}
		else
		{
			return null;
		}
	}
	
	public boolean isEmpty()
	{
		return cards.isEmpty();
	}
	
	public int getRemainingCards()
	{
		return numCards;
	}

	public void returnCardToDeck(Card card)
	{
		cards.add(card);
	}
	
	public String toString()
	{
		if (numCards == 0)
		{
			return "Deck is empty.";
		}
		
		StringBuilder sb = new StringBuilder();		
		int cardNumber = 1;
		
		for (Card card: cards)
		{
			sb.append("Card number " + cardNumber++ + ": " + card + "\n");
		}
		
		return sb.toString();
	}	
}
