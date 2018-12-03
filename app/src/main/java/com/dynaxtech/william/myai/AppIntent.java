package com.dynaxtech.william.myai;

import android.app.Activity;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * Created by william on 11/25/2017.
 *           v----------------------------------------|
 * !creation -> !hasNext  -> !next -> ... -> done -> again
 *          -------------- ^
 *
 */

public class AppIntent {
    private boolean isOpened;
    private String nextLine;
    private Activity activity;
    private String requiredAction;
    private String requiredType;
    private BufferedReader br;
    private String lastError;
    private boolean ready;
    private void reset( String leftMsg )
    {
        nextLine = null;
        br = null;
        isOpened = false;
        lastError = leftMsg;
    }
    public AppIntent( Activity _activity, String _action, String _type )
    {
        activity = _activity;
        requiredAction = _action;
        requiredType = _type;
        reset( null );
        ready = true;
    }
    public boolean done()
    {
        if( isOpened && br != null ) {
            try {
                br.close();
                reset( null ); // normal
                return true;
            } catch (IOException e) {
                reset( "Could not close" );
                return false;
            }
        } else {
            reset("Was not opened");
            return true;
        }
    }

    public boolean hasNext()
    {
        if( ready && ! isOpened && nextLine==null ) {
            nextLine = null;
            br = null;
            Intent intent = activity.getIntent();
            if( intent==null ) {
                reset( "No intent");
                return false;
            }
            String action = intent.getAction();
            String type = intent.getType();

            if ( requiredAction.equals(action) && type != null && requiredType.equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                try {
                    if( sharedText != null ) {
                        br = new BufferedReader(new StringReader(sharedText));
                        nextLine = br.readLine();
                        if( nextLine==null) {
                            lastError = "File is empty";
                            br.close();
                            br = null;
                            ready = false;
                        } else
                            isOpened = true;
                    }
                } catch (IOException e)
                {
                    br = null;
                    lastError = "Intent does not exist";
                }
            }
        }
        return isOpened && nextLine != null;
    }

    public String next()
    {
        if( ! isOpened ) {
            reset( "is not opened" );
            return null;
        }
        if( nextLine==null ) {
            if( br==null ) {
                reset( "no more data" );
                return null;
            }
            try {
                nextLine = br.readLine();
                if (nextLine == null) {
                    br.close();
                    reset( "no more data" );
                    return null;
                }
            } catch (IOException e) {
                reset( "could not get more data" );
                return null;
            }
        }
        // everything normal
        String r = nextLine;
        nextLine = null;
        lastError = null;
        return r;
    }

    public boolean again()
    {
        if( isOpened ) return false;
        ready = false;
        return ready;
    }
}
