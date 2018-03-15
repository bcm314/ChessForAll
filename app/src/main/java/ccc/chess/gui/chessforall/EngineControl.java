package ccc.chess.gui.chessforall;

import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;

import ccc.chess.book.BookOptions;
import ccc.chess.book.C4aBook;

public class EngineControl 
{
	EngineControl(MainActivity mA)
    {
		mainA = mA;						// main thread MainActivity for UI-actions
		createEngines();
    }
	public void createEngines()
	{
		en_1 = new ChessEngine(mainA, 1, mainA.userPrefs.getBoolean("user_options_enginePlay_logOn", false));	// engine number 1
		book = new C4aBook(mainA);
		book.getInstance();
	}
	public final void setBookOptions() 
	{
		bookOptions.filename = mainA.userPrefs.getString("user_options_enginePlay_OpeningBookName", "");
		if (bookOptions.filename.length() > 0) 
        {
            File extDir = Environment.getExternalStorageDirectory();
            CharSequence sep = File.separator;
            bookOptions.filename = extDir.getAbsolutePath() + sep + bookOptions.filename;
        }
        book.setOptions(bookOptions);
    }

	public void setPlaySettings(SharedPreferences userP)
    {	// get play settings data from userPrefs + setting engine number
		// play settings data from userPrefs
		chessEnginePlayMod = userP.getInt("user_play_playMod", 1);
		if (chessEnginePlayMod == 4)
			initClockAfterAnalysis = true;
		twoEngines = false;
		if (chessEnginePlayMod == 3 | chessEnginePlayMod == 4)	// engine vs engine | analysis
			chessEngineSearching = true;
    }
	public void setPlayData(SharedPreferences userP)												
    {	// setting the PGN-Data 
		chessEngineEvent = "Android " + android.os.Build.VERSION.RELEASE;
		chessEngineSite = android.os.Build.MODEL;
		chessEngineRound = "-";
		if (chessEngineAutoRun)
		{
			chessEngineRound = 	userP.getInt("user_play_eve_round", 1) 
								+ "." + userP.getInt("user_play_eve_gameCounter", 1);
		}
		if (twoEngines)
		{

		}
		else
		{
			switch (chessEnginePlayMod)
	        {
	        	case 1:
	        		chessEnginePlayerWhite = userP.getString("user_options_gui_playerName", "Me");
	        		chessEnginePlayerBlack = en_1.engineName;
	        		break;
	        	case 2:
	        		chessEnginePlayerWhite = en_1.engineName;
	        		chessEnginePlayerBlack = userP.getString("user_options_gui_playerName", "Me");
	        		break;
	        	case 3:	
	        		chessEnginePlayerWhite = en_1.engineName;
	        		chessEnginePlayerBlack = en_1.engineName;
	        		break;
	        	case 4:	
	        		chessEnginePlayerWhite = mainA.gc.cl.history.getGameTagValue("White");
	        		chessEnginePlayerBlack = mainA.gc.cl.history.getGameTagValue("Black");
	        		break;
	        }
		}
    }

	public void setEngineNumber(int eNumber) 
	{	//>361 engine Number: for better controlling multiple ChessEngines in GUI
		engineNumber = eNumber; 
	}
	public ChessEngine getEngine()
    {
    	switch (engineNumber)
        {
        	case 1:		return en_1;
//        	case 2:		return en_2;
        	default:	return en_1;
        }
    }



	public void stopAllEngines() 
    {	//>381 shutdownEngine() and releaseEngineService()
    	setEngineNumber(1);
    	if (getEngine() != null)	
    		stopComputerThinking(true);
    	if (!mainA.isAppEnd)
    		chessEnginePaused = true;
    }

	public void setStartPlay(CharSequence color)
    {
		if (twoEngines)
		{

		}
		else
		{
			if 	(		chessEnginePlayMod == 3
					|	chessEnginePlayMod == 4
					|	chessEnginePlayMod == 1 & color.equals("b")
					| 	chessEnginePlayMod == 2 & color.equals("w")
				)
			{
				en_1.startPlay = true;
				makeMove = true;
			}
			else
			{
				en_1.startPlay = false;
				makeMove = false;
			}
		}
//		Log.i(TAG, "setStartPlay(), playMod: " + chessEnginePlayMod + ", color: " + color + ", makeMove: " + makeMove);
    }
	public final synchronized void stopComputerThinking(boolean shutDown) 
    {
//Log.i(TAG, "stopComputerThinking, processAlive: " + getEngine().processAlive + ", shutDown: " + shutDown);
		chessEngineStopSearch = true;
		try
		{
			if (getEngine().processAlive)
	    	{
	    		if (!getEngine().syncStopSearch())
				{

				}
	    		if (mainA.chessEngineSearchTask != null)
		    		mainA.chessEngineSearchTask.cancel(true);
	    	}
			if (shutDown)
	    	{
				getEngine().shutDown();
				if (mainA.chessEngineSearchTask != null)
	    			mainA.chessEngineSearchTask.cancel(true);
				try {Thread.sleep(sleepTime);}
				catch (InterruptedException e) {}
	    	}
		}
		catch (NullPointerException e) {e.printStackTrace();}
    }

	final String TAG = "EngineControl";
	MainActivity mainA;
	ChessEngine en_1;	// default: Stockfish
	C4aBook book;
	BookOptions bookOptions = new BookOptions();
	int engineNumber = 1;					// for controlling ChessEngines: en_1 | en_2
	boolean twoEngines = false;				// true if two different engines(b/w)
	boolean makeMove = false;				// engine makes first move
    int chessEnginePlayMod = 1;				// 1 = player vs engine, 2 = engine vs player, 3 = engine vs engine, 4 = engine analysis
    				                        // 5 = player vs player, 6 = edit
	boolean initClockAfterAnalysis = false;
	long sleepTime = 200;
    
    public boolean chessEngineInit = false;
    public boolean chessEnginesOpeningBook = false;
    public boolean chessEngineSearching = false;

    public boolean chessEngineSearchingPonder = false;
    public CharSequence ponderUserFen = "";

    public boolean chessEnginePaused = false;
    public boolean chessEngineProblem = false;
    public boolean chessEngineAutoRun = false;
    public boolean chessEngineAnalysis = false;
    public boolean chessEngineStopSearch = false;
    public int chessEngineAnalysisStat = 0; 	// 0 = no analysis, 1 = make move ans stop engine, 2 = make move and continue analysis

    CharSequence chessEnginePlayerWhite = "Me";
    CharSequence chessEnginePlayerBlack = "";
    CharSequence chessEngineEvent = ""; 
    CharSequence chessEngineSite = "";
    CharSequence chessEngineRound = "";

}
