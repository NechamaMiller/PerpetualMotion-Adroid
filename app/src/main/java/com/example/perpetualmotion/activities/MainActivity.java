package com.example.perpetualmotion.activities;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.perpetualmotion.R;
import com.example.perpetualmotion.idiotsDelight.Card;
import com.example.perpetualmotion.idiotsDelight.PepetualMotion;
import com.example.perpetualmotion.lib.CardPilesAdapter;
import com.google.gson.Gson;

import java.util.EmptyStackException;

public class MainActivity extends AppCompatActivity
{
    private PepetualMotion mCurrentGame;
    private CardPilesAdapter mAdapter;
    private TextView mTv_cardsRemaining;
    private TextView mTv_cardsInDeck;
    private View mSbContainer;//container of snackbar
    private boolean[] mCheckedPiles;
    private final String mKeyCheckedPiles = "CHECKED_PILES";
    private final String mKeyGame = "GAME";

    //for when rotate
    //we're gonna serialize the Java game class, and then everything gets saved automatically
    //except the checks cuz that's in the xml, so save that also
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        //save the array of checked piles together with it's name (checked_piles), and same for game with its name
        outState.putBooleanArray(mKeyCheckedPiles, mCheckedPiles);
        outState.putString(mKeyGame, getJSONof(mCurrentGame));//save a string which contains serialized version of game object
    }

    protected String getJSONof(PepetualMotion obj)
    {
        Gson gson = new Gson();
        return gson.toJson(obj);
    }

    private PepetualMotion restoreGameFromJSON(String json)
    {
        Gson gson = new Gson();//GSON is Google String Object Notation - for serializ
        return gson.fromJson(json, PepetualMotion.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupGUI();
        setupBoard();
        doInitialStartGame(savedInstanceState);
        setupFAB();
    }

    private void setupGUI()
    {
        mSbContainer = findViewById(R.id.cl_main);
        mTv_cardsRemaining = (TextView)findViewById(R.id.tv_cards_remaining_to_discard);
        mTv_cardsInDeck = (TextView)findViewById(R.id.tv_cards_in_deck);
    }

    private final CardPilesAdapter.OIClickListener
            listener = new CardPilesAdapter.OIClickListener()
    {
        public void onItemClick(int position, View view)
        {
            try
            {
                if(mCurrentGame.getNumberOfCardsInStackAtPosition(position) > 0)
                {
                    mAdapter.toggleCheck(position);
                }
            }
            catch (Exception e)
            {
                Log.d("STACK", "Toggle Crashed: " + e.getMessage());
            }
        }
    };

    private void setupBoard()
    {
        mCheckedPiles = new boolean[] {false, false, false, false};
        mAdapter = new CardPilesAdapter(mCheckedPiles,getString(R.string.cards_in_stack));
        mAdapter.setOnItemClickListener(listener);
        RecyclerView piles = (RecyclerView)findViewById(R.id.rv_piles);
        //tells layout manager how many columns to make the grid, which depends on layout - portrait or landscape
        RecyclerView.LayoutManager layoutManager =
                new GridLayoutManager(this, getResources().getInteger(R.integer.rv_columns));
        layoutManager.setAutoMeasureEnabled(true);

        piles.setHasFixedSize(true);
        piles.setLayoutManager(layoutManager);
        piles.setAdapter(mAdapter);
    }

    private void doInitialStartGame(Bundle savedInstanceState)
    {
        if (savedInstanceState != null)//if this is not the first run, it's a rotation, then restore
        {
            //restore game from the JSON
            mCurrentGame = restoreGameFromJSON(savedInstanceState.getString(mKeyGame));
            doPostTurnUpdates();//refresh the GUI to reflect state game is up to
            mAdapter.overwriteChecksFrom(savedInstanceState.getBooleanArray(mKeyCheckedPiles));
        }
        else
        {
            startGame();
        }
    }

    private void doPostTurnUpdates()
    {
        updateStatusBar();
        updateRecyclerViewAdapter();
        checkForGameOver();
    }

    private void updateStatusBar ()
    {
        // Update the Status Bar with the number of cards left (from Java) via our current game obj
        mTv_cardsRemaining.setText (getString (R.string.cards_to_discard).concat
                (String.valueOf (mCurrentGame.getRemainingCards ())));

        mTv_cardsInDeck.setText (getString (R.string.in_deck).concat (
                String.valueOf (mCurrentGame.getNumberOfCardsLeftInDeck ())));
    }

    private void updateRecyclerViewAdapter ()
    {
        // get the data for the new board from our game object (Java) which tracks the four stacks
        Card[] currentTops = mCurrentGame.getCurrentStacksTopIncludingNulls ();

        // temporary card used when updating the board below
        Card currentCard;

        // Update the board one pile/card at a time
        for (int i = 0; i < currentTops.length; i++) {
            currentCard = currentTops[i];

            // Have Adapter set each card to the matching top card of each stack
            mAdapter.updatePile (i, currentCard,
                    mCurrentGame.getNumberOfCardsInStackAtPosition (i));

            // Clear any checks that the user might have just set
            mAdapter.clearCheck (i);
        }
    }

    /**
     * If the game is over, this method outputs a dialog box with the correct message (win/not)
     */
    private void checkForGameOver ()
    {
        // If the game is over, let the user know what happened and then start a new game
        if (mCurrentGame.gameOver ())
        {

            showInfoDialog (R.string.game_over, mCurrentGame.isWinner () ?
                    R.string.you_have_cleared_the_board :
                    R.string.no_more_turns_remain);
            startGame ();
        }
    }

    private void startGame()
    {
        mCurrentGame = new PepetualMotion();
        doPostTurnUpdates();
        Snackbar.make(mSbContainer,R.string.welcome_new_game,Snackbar.LENGTH_SHORT).show();
    }

    private void setupFAB()
    {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showInfoDialog("Information",mCurrentGame.getRules());
        }
        });
    }

    /**
     * Shows an Android (nicer) equivalent to JOptionPane
     * @param strTitle Title of the Dialog box
     * @param strMsg Message (body) of the Dialog box
     */
    private void showInfoDialog(String strTitle, String strMsg)
    {
        // create the listener for the dialog
        final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener ()
        {
            @Override
            public void onClick (DialogInterface dialog, int which)
            {
                //nothing needed to do here
            }
        };

        // Create the AlertDialog.Builder object
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder (MainActivity.this);

        // Use the AlertDialog's Builder Class methods to set the title, icon, message, et al.
        // These could all be chained as one long statement, if desired
        alertDialogBuilder.setTitle (strTitle);
        alertDialogBuilder.setIcon (R.drawable.ic_action_info);
        alertDialogBuilder.setMessage (strMsg);
        alertDialogBuilder.setCancelable (true);
        alertDialogBuilder.setNeutralButton (getString (R.string.OK), listener);

        // Create and Show the Dialog
        alertDialogBuilder.show ();
    }

    /**
     * Overloaded XML version of showInfoDialog(String, String) method
     * @param titleID Title stored in XML resource (e.g. strings.xml)
     * @param msgID Message (body) stored in XML resource (e.g. strings.xml)
     */
    private void showInfoDialog (int titleID, int msgID)
    {
        showInfoDialog (getString (titleID), getString (msgID));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about)
//        {
                showAbout();
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    private void showAbout()
    {
        showInfoDialog (R.string.app_name, R.string.about_message);
    }

    /**
     * This method lets us know how many cards have been checked off
     * @param checkedPiles the array of checked Cards
     * @return the number of cards checked
     */
    private int getCountOfChecks (boolean[] checkedPiles)
    {
        int totalChecked = 0;
        for (boolean checkedPile : checkedPiles) {
            totalChecked += checkedPile ? 1 : 0;
        }
        return totalChecked;
    }

    /**
     * This method lets us know what the n checked pile contains (out of only checked piles, not 4)
     * @param checkedPiles 4-element boolean array of four checked/unchecked piles
     * @param position The checked pile number (0 through x) of the checked pile
     * @return The pile number (out of the checked piles, not out of all 4 piles)
     */
    private int getCheckedItem (boolean[] checkedPiles, int position)
    {
        // create a new int array containing the number of elements == the number of checked cards
        int[] checkedItems = new int[getCountOfChecks (checkedPiles)];

        // i is the index for the 4-element array of all stacks passed in
        // j is the index for the new array of position numbers just created
        for (int i = 0, j = 0; i < checkedPiles.length; i++) {
            // increment j only if current element is true
            if (checkedPiles[i]) {
                checkedItems[j++] = i;
            }

        }
        return checkedItems[position];
    }

    private void showSB (String msg)
    {
        Snackbar.make (mSbContainer, msg, Snackbar.LENGTH_LONG).show ();
    }

    private void showSBErrorDiscard (int piles)
    {
        Snackbar.make (mSbContainer,
                getString (piles == 2 ?
                        R.string.turn_error_discard_two :
                        R.string.turn_error_discard_one),
                Snackbar.LENGTH_LONG).show ();
    }

    /**
     * This method handles the user's choice to discard the lower of two cards of the same suit
     * @param view this is the calling Object (e.g. button): irrelevant to us so we ignore it
     */
    public void discardOneLowestOfSameSuit (@SuppressWarnings ("UnusedParameters") View view)
    {
        final int CARDS_NEEDED = 1;
        boolean[] checkedPiles = mAdapter.getCheckedPiles ();
        if (getCountOfChecks (checkedPiles) != CARDS_NEEDED) {
            showSBErrorDiscard (CARDS_NEEDED);
        }
        else {
            // if the user selected exactly the number of needed cards
            try {
                mCurrentGame.discard (getCheckedItem (checkedPiles, 0));
                //doPostTurnUpdates ();      // not needed here because it is done at end of method
            }
            catch (EmptyStackException ese) {
                showSBErrorDiscard (CARDS_NEEDED);
            }
            catch (UnsupportedOperationException uoe)
            {
                showSB(uoe.getMessage ());
                Log.d ("STACK", "Unsupported Operation Exception (one card)");
            }
        }
        // Note: By design, this statement will execute regardless of what happened above
        // Cards will remain the same unless the discard was successful; clear checkboxes regardless
        doPostTurnUpdates ();
    }

    /**
     * This method handles the user's choice to discard two cards of the same rank
     * @param view this is the calling Object (e.g. button): irrelevant to us so we ignore it
     */
    public void discardBothOfSameRank (@SuppressWarnings ("UnusedParameters") View view)
    {
        final int CARDS_NEEDED = 2;
        boolean[] checkedPiles = mAdapter.getCheckedPiles ();
        if (getCountOfChecks (checkedPiles) != CARDS_NEEDED) {
            showSBErrorDiscard (CARDS_NEEDED);
        }
        else {
            // if the user selected exactly the number of needed cards
            try {
                mCurrentGame.discard (
                        getCheckedItem (checkedPiles, 0),
                        getCheckedItem (checkedPiles, 1));

                doPostTurnUpdates ();
            }
            catch (EmptyStackException ese) {
                showSBErrorDiscard (CARDS_NEEDED);
            }
            catch (UnsupportedOperationException uoe)
            {
                showSB(uoe.getMessage ());
                Log.d ("STACK", "Unsupported Operation Exception (two cards)");
            }

        }
        // If the discard was successful then we will clear the checks from the stacks; otherwise:
        // In this case of two cards, let's leave the checks as they are rather than clearing them
    }

    /**
     * This method handles the user's choice to deal one card to each stack (i.e. new top of each)
     * @param view this is the calling Object (e.g. button): irrelevant to us so we ignore it
     */
    public void dealOneCardToEachStack (@SuppressWarnings ("UnusedParameters") View view)
    {
        try {
            mCurrentGame.deal ();
        }
        catch (IllegalArgumentException e) {
            showInfoDialog (R.string.title_no_cards_remain,
                    R.string.body_all_cards_dealt_to_stacks);
        }

        // cards will remain as above but clear checkboxes either way, even if deck is empty
        doPostTurnUpdates ();
    }

    /**
     * Starts a new game when ActionBar button is pressed
     * @param item MenuItem that triggered this call - not relevant to us so it is ignored
     */
    public void startNewGame (@SuppressWarnings ("UnusedParameters") MenuItem item)
    {
        startGame ();
    }

    public void undoLastMove (@SuppressWarnings ("UnusedParameters")  MenuItem item)
    {
        undoLastMove();
    }

    private void undoLastMove ()
    {
        try {
            mCurrentGame.undoLatestTurn ();
            doPostTurnUpdates ();

        }
        catch (UnsupportedOperationException uoe)
        {
            showInfoDialog ("Can't Undo", uoe.getMessage ());
        }
    }

    // LG work-around for select older devices
    @Override
    public boolean onKeyDown (int keyCode, KeyEvent event)
    {
        boolean isOldLG = ((keyCode == KeyEvent.KEYCODE_MENU) &&
                (Build.VERSION.SDK_INT <= 16) &&
                (Build.MANUFACTURER.compareTo ("LGE") == 0));

        //noinspection SimplifiableConditionalExpression
        return isOldLG ? true : super.onKeyDown (keyCode, event);
    }

    @Override
    public boolean onKeyUp (int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_MENU) &&
                (Build.VERSION.SDK_INT <= 16) &&
                (Build.MANUFACTURER.compareTo ("LGE") == 0)) {
            openOptionsMenu ();
            return true;
        }
        return super.onKeyUp (keyCode, event);
    }
}
