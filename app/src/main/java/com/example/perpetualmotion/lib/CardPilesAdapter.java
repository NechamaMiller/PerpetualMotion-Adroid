package com.example.perpetualmotion.lib;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.perpetualmotion.R;
import com.example.perpetualmotion.idiotsDelight.Card;

/**
 * RecyclerView Adapter that displays checkable 4 cards and the number of cards beneath each one.
 */

public class CardPilesAdapter extends RecyclerView.Adapter<CardPilesAdapter.ViewHolder>
{
    private static OIClickListener sOIClickListener;
    private final Card[] mPILES;
    private final boolean[] mCHECKED_PILES;
    private final int[] mNUMBER_OF_CARDS_IN_PILE;
    private final String mMSG_CARDS_IN_STACK;

    public void setOnItemClickListener (OIClickListener oiClickListener)
    {
        CardPilesAdapter.sOIClickListener = oiClickListener;
    }

    public CardPilesAdapter (boolean[] checkedPiles, String msgCardsInStack)
    {
        int NUMBER_OF_PILES = 4;
        mPILES = new Card[NUMBER_OF_PILES];
        mCHECKED_PILES = checkedPiles;
        mNUMBER_OF_CARDS_IN_PILE = new int[NUMBER_OF_PILES];
        mMSG_CARDS_IN_STACK = msgCardsInStack;
    }

    @Override public ViewHolder onCreateViewHolder (ViewGroup parent, int viewType)
    {
        View itemLayoutView = LayoutInflater.from (parent.getContext ()).inflate (
                R.layout.rv_item_pile_top_card, parent, false);
        return new ViewHolder (itemLayoutView);
    }

    @Override public void onBindViewHolder (ViewHolder holder, int position)
    {
        TextView tv_pile_card_rank_top_right, tv_pile_card_rank_bottom_right, tv_pile_card_suit;
        TextView tv_pile_card_cards_below;
        CheckBox cb_pile_checkbox;
        CardView cv_pile_inner_card;
        TextView[] tv_all;

        cv_pile_inner_card = holder.cv_pile_inner_Card;

        cb_pile_checkbox = holder.cb_pile_card_checkbox;

        tv_pile_card_rank_top_right = holder.tv_pile_card_rank_top;
        tv_pile_card_rank_bottom_right = holder.tv_pile_card_name_bottom;
        tv_pile_card_suit = holder.tv_pile_card_suit_center;
        tv_pile_card_cards_below = holder.tv_pile_card_cards_in_stack;

        tv_all = new TextView[]
                {tv_pile_card_rank_top_right, tv_pile_card_rank_bottom_right, tv_pile_card_suit};

        tv_pile_card_rank_top_right.setText (mPILES[position] == null ? "" :
                Integer.toString (
                        mPILES[position].getRank ().getValue()));


        tv_pile_card_rank_bottom_right.setText (mPILES[position] == null ? "" :
                mPILES[position].getRank ().toString ());

        tv_pile_card_suit.setText (mPILES[position] == null ? "" :
                Character.toString (
                        mPILES[position].getSuit ().getCharacter ()));

        tv_pile_card_cards_below.setText (mMSG_CARDS_IN_STACK.
                concat (Integer.toString (mNUMBER_OF_CARDS_IN_PILE[position])) );

        for (TextView aTv_all : tv_all) {
            if (mPILES[position] == null)
                aTv_all.setTextColor (0);
            else
                aTv_all.setTextColor (mPILES[position].getSuit ().getColor ());
        }

        if (mPILES[position] == null) {
            cb_pile_checkbox.setChecked(false);
            cv_pile_inner_card.setVisibility (View.INVISIBLE);
        }
        else {
            cv_pile_inner_card.setVisibility (View.VISIBLE);
            cb_pile_checkbox.setChecked (mCHECKED_PILES[position]);
        }

    }

    @Override public int getItemCount ()
    {
        return mPILES.length;
    }

    public void updatePile (int pileNumber, Card card, int numberOfCardsInStack)
    {
        mPILES[pileNumber] = card;
        mNUMBER_OF_CARDS_IN_PILE[pileNumber] = numberOfCardsInStack;
        notifyDataSetChanged ();
    }

    public void toggleCheck (int position)
    {
        mCHECKED_PILES[position] = !mCHECKED_PILES[position];
        notifyDataSetChanged ();
    }

    public void clearCheck (int position)
    {
        mCHECKED_PILES[position] = false;
        notifyDataSetChanged ();
    }


    public void overwriteChecksFrom (boolean[] newChecksSet)
    {
        System.arraycopy (newChecksSet, 0, mCHECKED_PILES, 0, mCHECKED_PILES.length);
    }

    public boolean[] getCheckedPiles ()
    {
        return mCHECKED_PILES.clone ();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        public final CardView cv_pile_inner_Card;
        public final TextView tv_pile_card_rank_top, tv_pile_card_name_bottom,
                tv_pile_card_suit_center, tv_pile_card_cards_in_stack;
        public final CheckBox cb_pile_card_checkbox;

        public ViewHolder (View itemLayoutView)
        {
            super (itemLayoutView);

            cv_pile_inner_Card = (CardView) itemLayoutView.findViewById(R.id.pile_card_inner_card);

            tv_pile_card_rank_top =
                    (TextView) itemLayoutView.findViewById (R.id.pile_card_rank_top);
            tv_pile_card_name_bottom =
                    (TextView) itemLayoutView.findViewById (R.id.pile_card_name_bottom);
            tv_pile_card_suit_center =
                    (TextView) itemLayoutView.findViewById (R.id.pile_card_suit_center);
            tv_pile_card_cards_in_stack =
                    (TextView) itemLayoutView.findViewById (R.id.pile_card_in_stack);
            cb_pile_card_checkbox =
                    (CheckBox) itemLayoutView.findViewById (R.id.pile_card_checkbox);

            cb_pile_card_checkbox.setClickable (false);

            itemLayoutView.setOnClickListener (this);
        }

        @Override public void onClick (View view)
        {
            sOIClickListener.onItemClick (getAdapterPosition (), view);
        }
    }

    // used to send data out of Adapter - implemented in the calling Activity/Fragment
    @SuppressWarnings ("UnusedParameters")
    public interface OIClickListener
    {
        void onItemClick (int position, View v);
    }
}