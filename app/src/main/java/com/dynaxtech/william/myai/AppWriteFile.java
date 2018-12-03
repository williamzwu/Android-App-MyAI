package com.dynaxtech.william.myai;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by william on 11/25/2017.
 *           v----------------------------------------|
 * !creation -> !hasNext  -> !next -> ... -> done -> again
 *          -------------- ^
 *
 */

public class AppWriteFile {
    private boolean isOpened;
    private String nextLine;
    private Context context;
    private String name;
    private String type;
    private BufferedWriter bw;
    private String lastError;
    private boolean ready;
    private void reset( String leftMsg )
    {
        nextLine = null;
        bw = null;
        isOpened = false;
        lastError = leftMsg;
    }
    public AppWriteFile(Context _context, String _name, String _type )
    {
        context = _context;
        name = _name;
        type = _type;
        reset( null );
        ready = true;
    }
    public boolean done()
    {
        if( isOpened && bw != null ) {
            try {
                bw.close();
                reset( null ); // normal
                String appFile = name + "." + type;
                File pathOut = context.getFilesDir();
                File fOut = new File( pathOut, appFile );
                File fTmp = new File( pathOut, appFile+".tmp");
                File fBak = new File( pathOut, appFile+".bak");
                if (fOut.exists())
                    fOut.renameTo(fBak);
                fTmp.renameTo(fOut);
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

    public boolean canWrite()
    {
        if( ready && ! isOpened ) {
            nextLine = null;
            bw = null;
            String appFileTmp = name + "." + type + ".tmp";
            File pathOut = context.getFilesDir();
            String msg;
            try {
                if( pathOut.canWrite() ) {
                    bw = new BufferedWriter(new OutputStreamWriter(context.openFileOutput(appFileTmp, context.MODE_APPEND)));
                    if( bw==null) {
                        lastError = "Cannot write";
                        ready = false;
                    } else {
                        isOpened = true;
                        ready = true;
                    }
                }
            } catch (IOException e)
            {
                bw = null;
                lastError = pathOut.getAbsolutePath() +" cannot be writren";
            }
        }
        return ready && isOpened;
    }

    public boolean add(String t)
    {
        if( ! (isOpened && bw != null) ) {
            reset( "is not opened" );
            return false;
        }
        try {
            bw.write(t+'\n');
        } catch (IOException e) {
            reset( "could not add data" );
            return false;
        }
        // everything normal
        return true;
    }

    public boolean again()
    {
        if( isOpened ) return false;
        ready = false;
        return ready;
    }
}
