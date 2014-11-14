package com.everythingissauce.pifuxelck.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.everythingissauce.pifuxelck.Drawing;
import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.ui.DrawingOnTouchListener;
import com.everythingissauce.pifuxelck.ui.DrawingView;


public class Testing extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

      Drawing.Builder drawingBuilder = new Drawing.Builder();
      DrawingView drawingView = (DrawingView) findViewById(R.id.drawing);
      drawingView.setDrawing(drawingBuilder);
      DrawingOnTouchListener.install(drawingView, drawingBuilder);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_testing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}