package com.example.perpetualmotion.idiotsDelight;

import android.util.Log;

import java.util.Stack;

public class PepetualMotion
{
	private Deck theDeck;
	private Stack<Card>[] stacks;
	private Card[] tentativeTopCardsAtLastTurn, finalTopCardsAtLastTurn;
	private boolean lastTurnWasADiscard, lastTurnCanBeUndone;
	private final int NUM_STACKS = 4;

	public PepetualMotion()
	{
		theDeck = new Deck();
		theDeck.shuffle();
		stacks = (Stack<Card>[]) new Stack[NUM_STACKS];
		for (int i = 0; i < stacks.length; i++)
		{
			stacks[i] = new Stack<Card>();
			stacks[i].push(theDeck.deal());
		}
		tentativeTopCardsAtLastTurn = new Card[NUM_STACKS];
		finalTopCardsAtLastTurn = tentativeTopCardsAtLastTurn.clone();
		lastTurnWasADiscard = false;
		lastTurnCanBeUndone = false;
	}

	public void undoLatestTurn()
	{
		if(lastTurnCanBeUndone)
		{
			if (!(getRemainingCards() == 52 && getNumberOfCardsLeftInAllStacks() == stacks.length))
			{
				doUndoATurnOfEitherDiscardOrDeal();
			}
			else
			{
				throw new UnsupportedOperationException("This is the beginnning of the game.");
			}
		}
		else
		{
			throw new UnsupportedOperationException("Already undid last turn.");
		}
	}

	private void doUndoATurnOfEitherDiscardOrDeal()
	{
		if(lastTurnWasADiscard)
		{
			doUndoATurnOfTypeDiscard();
		}
		else
		{
			doUndoATurnOfTypeDeal();
		}

		lastTurnCanBeUndone = false;
	}

	private void doUndoATurnOfTypeDiscard()
	{
		for(int i=0; i<finalTopCardsAtLastTurn.length; i++)
		{
			if(finalTopCardsAtLastTurn[i] != null)
			{
				if(stacks[i].size() == 0 || !(finalTopCardsAtLastTurn[i].equals((stacks[i].peek()))))
				{
					stacks[i].push(finalTopCardsAtLastTurn[i]);
				}
			}
		}
	}

	private void doUndoATurnOfTypeDeal()
	{
		for(int i=stacks.length-1; i>= 0; i--)
		{
			theDeck.returnCardToDeck(stacks[i].pop());
		}
	}

	private void saveTentativePriorTurnInformation()
	{
		for(int i=0; i<tentativeTopCardsAtLastTurn.length; i++)
		{
			tentativeTopCardsAtLastTurn[i] = stacks[i].size() != 0 ? stacks[i].peek().copy() : null;
		}
	}

	private void commitPriorTurnInformationAndSetUndoTypeTo(boolean turnWasADiscard)
	{
		lastTurnCanBeUndone = true;
		lastTurnWasADiscard = turnWasADiscard;

		System.arraycopy(tentativeTopCardsAtLastTurn,0,finalTopCardsAtLastTurn,0,tentativeTopCardsAtLastTurn.length);
	}

	/**
	 * Acts like a toString()
	 */
	public String display()
	{
		StringBuilder sb = new StringBuilder();
		int remainingCards = theDeck.getRemainingCards();
		for (int i = 0; i < stacks.length; i++)
		{
			if (!(stacks[i].isEmpty()))
			{
				sb.append("The " + stacks[i].peek() + " is on top of stack number " + (i + 1) + ".\n");
				remainingCards += stacks[i].size();
			}
			else
			{
				sb.append("Stack number " + (i + 1) + " is empty.\n");
			}
		}
		sb.append("There are " + theDeck.getRemainingCards() + " cards left in the deck and a total of " + remainingCards + " cards still to discard in order to win.");
		return sb.toString();
	}

	/**
	 * discards top card from both stacks indicated if their ranks match
	 *
	 * @param stackOneIndex index of first stack to discard from
	 * @param stackTwoIndex index of second stack to discard from
	 * @return true if cards were discarded or false otherwise. Will also return false if stack at one of stackIndexes was empty
	 * @throws IllegalArgumentException if index parameters are equal or either index parameter is less than 0 or more than 3
	 */
	public boolean discard(int stackOneIndex, int stackTwoIndex)
	{
		if (stackOneIndex == stackTwoIndex)
		{
			throw new IllegalArgumentException("Indexes cannot be equal.");
		}
		if (stackOneIndex < 0 || stackOneIndex > (stacks.length - 1) || stackTwoIndex < 0 || stackTwoIndex > (stacks.length - 1))
		{
			throw new IllegalArgumentException("Indexes must be between 0 and " + (stacks.length - 1) + ".");
		}
		if (stacks[stackOneIndex].isEmpty() || stacks[stackTwoIndex].isEmpty())
		{
			return false;
		}
		if (stacks[stackOneIndex].peek().getRank().getValue() == (stacks[stackTwoIndex].peek().getRank().getValue()))
		{
			saveTentativePriorTurnInformation();
			stacks[stackOneIndex].pop();
			stacks[stackTwoIndex].pop();
			commitPriorTurnInformationAndSetUndoTypeTo(true);
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * discards top card from indicated stack if the top card of another stack has the same suit with a higher rank
	 *
	 * @param stackIndex index of stack to discard from
	 * @return true if card was discarded, false otherwise. Will also return false if stack at stackIndex was empty
	 * @throws IllegalArgumentException if index is less than 0 or more than 3
	 */
	public boolean discard(int stackIndex)
	{
		if (stackIndex < 0 || stackIndex > (stacks.length - 1))
		{
			throw new IllegalArgumentException("Index cannot be less than 0 or more than " + (stacks.length - 1) + ".");
		}
		if (stacks[stackIndex].isEmpty())
		{
			return false;
		}
		Card card1 = stacks[stackIndex].peek();
		for (int i = 0; i < stacks.length; i++)
		{
			//it is possible for one stack to be empty when others aren't because there can be a different amount of cards in different stacks depending how many times discard() was called on that stack			
			if (!(stacks[i].isEmpty()))
			{
				Card card2 = stacks[i].peek();
				if (i != stackIndex && card1.getSuit().equals(card2.getSuit()) && card1.getRank().getValue() < card2.getRank().getValue())
				{
					saveTentativePriorTurnInformation();
					stacks[stackIndex].pop();
					commitPriorTurnInformationAndSetUndoTypeTo(true);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Game is won if deck and all stacks are empty
	 *
	 * @return true if won, false otherwise
	 */
	public boolean gameOver()
	{
		return theDeck.isEmpty();
	}

	/**
	 * Deals a new card on top of each stack, unless the deck of cards is empty
	 *
	 * @return true if cards were dealt, false if deck was empty and no cards were dealt
	 */
	public void deal()
	{
		if (theDeck.isEmpty())
		{
			throw new IllegalArgumentException();
		}

		saveTentativePriorTurnInformation();

		//I am checking if the deck is empty before every card even though I already checked in case the number of cards in the deck wouldn't be a multiple of 4 so I don't introduce nulls
		for (int i = 0; i < stacks.length && (!(theDeck.isEmpty())); i++)
		{
			stacks[i].push(theDeck.deal());
		}

		commitPriorTurnInformationAndSetUndoTypeTo(false);
	}

	//even though there is a display method, I put in a toString() because users often expect it
	public String toString()
	{
		return display();
	}

	public Card[] getCurrentStacksTop()
	{
		Card[] currentStacksTop = new Card[4];
		for (int i = 0; i < 4; i++)
		{
			currentStacksTop[i] = stacks[i].isEmpty() ? null : stacks[i].peek().copy();
		}
		return currentStacksTop;
	}

	public int getRemainingCards()
	{
		return getNumberOfCardsLeftInDeck() + getNumberOfCardsLeftInAllStacks();
	}

	public int getNumberOfCardsLeftInDeck()
	{
		return theDeck.getRemainingCards();
	}
	public int getNumberOfCardsLeftInAllStacks()
	{
		int cardsInStacks = 0;
		for(Stack<Card> stack : stacks)
		{
			cardsInStacks += stack.size();
		}

		return cardsInStacks;
	}

	/**
	 * Gets an array containing the current top card of each of the piles (0-3).
	 * In MainActivity, we pass this to the adapter to replace the current "board" with these cards.
	 * @return array of 4 card tops (elements 0-3)
	 */
	public Card[] getCurrentStacksTopIncludingNulls ()
	{
		Card[] currentStacksTop = new Card[4];
		for (int i = 0; i < 4; i++) {
			currentStacksTop[i] = stacks[i].isEmpty () ? null : stacks[i].peek ().copy ();
		}
		return currentStacksTop;
	}

	/**
	 * Gets the number of cards left in a particular stack, including ones below the top.
	 * @param position stack number (0-3) of stack whose total will be returned
	 * @return total number of cards in a particular stack
	 */
	public int getNumberOfCardsInStackAtPosition (int position)
	{
		return stacks[position].size ();
	}


	/**
	 * Gets the availability of at least one move in the current Stack tops
	 *
	 * @return true if there is one move in the current Stack tops
	 */
	public boolean hasAtLeastOneValidMoveInCurrentStackTops()
	{
		boolean hasAvailableTurn = false;
		for (int i = 0; i < stacks.length && !hasAvailableTurn; i++)
		{
			for (int j = 0; j < stacks.length && !hasAvailableTurn; j++)
			{
				if (i != j && stacks[i].size() > 0 && stacks[j].size() > 0)
				{
					if (stacks[i].peek().getRank().equals(stacks[j].peek().getRank()) ||
							stacks[i].peek().getSuit().equals(stacks[j].peek().getSuit()))
					{
						hasAvailableTurn = true;
					}
				}
			}
		}

		return hasAvailableTurn;
	}

	/**
	 * Gets the status of the game having been won, as opposed to in progress or game lost.
	 * @return true if no more cards left to discard; otherwise, false.
	 */
	public boolean isWinner()
	{
		return getRemainingCards () == 0;
	}

	/**
	 * Gets the rules of the game.
	 * @return game's rules
	 */
	public String getRules ()
	{
		// This should really be a string in XML, but it came (almost) like this from Java-only...
		return "The goal of the game is to discard all cards " +
				"until both the deck and all four piles are empty."
				+ "\n\nAfter all 52 cards have been dealt from deck, game-play can continue until:"
				+ "\n1. All cards have been discarded from all four stacks (a winner!). - OR -"
				+ "\n2. None of the remaining top cards in any pile can be discarded (not a winner)."
				+ "\n\nEach pile initially contains one card at the top, " +
				"which leaves 48 cards remaining in the deck."
				+ "\n\nFor each turn taken, there are three potential options from which to choose:"
				+ "\n1. If there are two cards of same suit showing, discard the lower-ranked card."
				+ "\n2. If there are two cards with same rank showing, discard both of those cards."
				+ "\n3. Deal four new cards from the deck, one on top of each stack.";
	}

}
