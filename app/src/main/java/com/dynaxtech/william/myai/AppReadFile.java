package com.dynaxtech.william.myai;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by william on 11/25/2017.
 *           v----------------------------------------|
 * !creation -> !hasNext  -> !next -> ... -> done -> again
 *          -------------- ^
 *
 */

public class AppReadFile {
    private boolean isOpened;
    private String nextLine;
    private Context context;
    private String name;
    private String type;
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
    public AppReadFile(Context _context, String _name, String _type )
    {
        context = _context;
        name = _name;
        type = _type;
        reset( null );
        ready = true;
    }
    public AppReadFile(Context _context, String _name )
    {
        context = _context;
        name = _name;
        type = null;
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
        if( ready && isOpened && nextLine==null ) {
            try {
                nextLine = br.readLine();
            } catch (IOException e)
            {
                reset(name +" could not be opened");
            }
        }
        if( ready && ! isOpened ) {
            nextLine = null;
            br = null;
            String appFile = type==null ? name : name + "." + type;
            File pathIn = context.getFilesDir();
            File fIn = new File(pathIn, appFile);
            String msg;
            try {
                if( fIn.exists() ) {
                    br = new BufferedReader(new InputStreamReader(context.openFileInput(appFile)));
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
                reset(name +" could not be opened");
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

    public String all()
    {
        StringBuffer x = new StringBuffer();
        while(hasNext())
        {
            x.append(next());
            x.append('\n');
        }
        done();
        return x.toString();
    }

    public boolean again()
    {
        if( isOpened ) return false;
        ready = true;
        return ready;
    }
}
