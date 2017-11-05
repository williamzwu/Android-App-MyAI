package com.dynaxtech.william.myai;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by william on 10/8/2017.
 */

// https://stackoverflow.com/questions/11200381/how-to-draw-a-custom-view-inside-a-surfaceview

public class FlowView extends SurfaceView implements SurfaceHolder.Callback {

    class Line {
        Path path;
        float length;
        PathEffect dashPath;
        Paint paint;
    };

    protected Paint goodPaint;
    protected Paint badPaint;
    protected Map<String, Line> lines;
    protected int numLines;

    public FlowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);

        lines = new HashMap<>();
    }

    // https://developer.android.com/reference/android/graphics/Path.html
    // https://stackoverflow.com/questions/16528572/draw-dash-line-on-a-canvas
    // https://stackoverflow.com/questions/5367950/android-drawing-an-animated-line
    // https://stackoverflow.com/questions/3563908/animated-dashed-border-in-android

    // https://alvinalexander.com/java/jwarehouse/android-examples/platforms/android-5/samples/ApiDemos/src/com/example/android/apis/graphics/PathEffects.java.shtml
    // https://developer.android.com/samples/index.html


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for(Map.Entry<String, Line> e: lines.entrySet()){
//            canvas.drawLine(lines[i].start_x, lines[i].start_y, lines[i].end_x, lines[i].end_y, goodPaint);
/*            Path mPath = new Path();
            mPath.moveTo(lines[i].start_x, lines[i].start_y);
            mPath.lineTo(lines[i].end_x, lines[i].end_y);
            */
//            Log.d("wifiview","draw:" + String.valueOf(i) + lines[i].goodPaint.getPathEffect().toString());
            String name = e.getKey();
            Line line = e.getValue();
            canvas.drawPath(line.path, line.paint);
        }
//        invalidate();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
    }

    ObjectAnimator animator;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        goodPaint = new Paint();
        goodPaint.setColor(Color.GREEN);
        goodPaint.setStyle(Paint.Style.STROKE);
        goodPaint.setStrokeWidth(5);

        badPaint = new Paint();
        badPaint.setColor(Color.BLACK);
        badPaint.setStyle(Paint.Style.STROKE);
        badPaint.setStrokeWidth(3);
        DashPathEffect dashPath = new DashPathEffect(new float[] { 20, 10 }, (float) 0);
        badPaint.setPathEffect(dashPath);

//        float[] intervals = new float[]{length, length};
//        addLine( "wifi", 500, 73, 600, 200);
//        addLine( "wifi", 319, 73, 500, 200);

        /*
        animator = ObjectAnimator.ofFloat(WifiView.this, "phase", 20f, 0.0f);
        animator.setDuration(30000);
        animator.setCurrentPlayTime(100);
        */
    }

    //        animator.start();
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
    }

    public void start()
    {
        if( animator != null ) animator.start();
    }
    public void stop()
    {
        if( animator != null ) animator.cancel();
    }

    public void addLine( String name, int sx, int sy, int ex, int ey )
    {
        Line line = new Line();
        line.path = new Path();
        line.path.moveTo(sx, sy);
        line.path.lineTo(ex, ey);
        line.paint = goodPaint;
        PathMeasure measure = new PathMeasure(line.path, false);
        line.length = measure.getLength();
//        line.dashPath = createPathEffect(line.length,0,0);
//        line.goodPaint.setPathEffect(line.dashPath);
        if( lines.containsKey(name) )
            lines.remove(name);
        lines.put( name, line );
    }

    public void setLineStatus(String name, boolean good)
    {
        Line line = lines.get(name);
        if( line == null ) return;
        line.paint = (good?goodPaint:badPaint);
        invalidate();//will calll onDraw
    }

    //is called by animator object
    public void setPhase(float phase)
    {
        Log.d("pathview","setPhase called with:" + String.valueOf(phase));
        for(Map.Entry<String, Line> e: lines.entrySet()){
//            canvas.drawLine(lines[i].start_x, lines[i].start_y, lines[i].end_x, lines[i].end_y, goodPaint);
/*            Path mPath = new Path();
            mPath.moveTo(lines[i].start_x, lines[i].start_y);
            mPath.lineTo(lines[i].end_x, lines[i].end_y);
            */
            String name = e.getKey();
            Line line = e.getValue();
            Log.d("wifiview","draw:" + name + line.paint.getPathEffect().toString());
            line.paint.setPathEffect(createPathEffect(line.length, phase, 0.0f));
        }
        invalidate();//will calll onDraw
    }

    private static PathEffect createPathEffect(float pathLength, float phase, float offset)
    {
//        return new DashPathEffect(new float[] { pathLength, pathLength },
  //              Math.max(phase * pathLength, offset));
//        return new DashPathEffect(new float[] { 20, 10 },
//                phase);
          return new DashPathEffect(new float[] { 0, pathLength/3, pathLength/3, pathLength/3 },
                      Math.max(phase * pathLength, offset));
    }
}
