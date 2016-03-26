/***************************************************************************************************
Color Chief - MainActivity Class
****************************************************************************************************

 Copyright 2016 Bergen Fletcher

        Licensed under the Apache License, Version 2.0 (the "License");
        You may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
***************************************************************************************************/

package com.github.colorchief.colorchief;

import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Color;
import android.graphics.Matrix;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.provider.OpenableColumns;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TabHost;

import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ColorSuite:MainActivity";
    private static final int SELECT_IMAGE = 100;
    private static final int SELECT_ICC_IN = 42;
    private static final int SELECT_ICC_OUT = 43;
    private static final float H_ADJUST = 5f;
    private static final float S_ADJUST = 0.05f;
    private static final float V_ADJUST = 0.05f;
    private static final boolean SHOW_PROGRESS_BAR = false;
    private static final int LUT_SIZE = 9;
    private ColorLUT colorLUT = new ColorLUT(this);

    private int verticeCoords[] = new int[3];
    private ICCProfile iccProfileIn = new ICCProfile(this);
    private ICCProfile iccProfileOut = new ICCProfile(this);

    private static boolean bitmapLoaded = false;
    private static Bitmap bitmapScaledOriginal = null;
    private static Bitmap bitmapScaledTransform = null;
    private static Uri uriBitmapOriginal = null;
    private static Uri uriIccProfileIn = null;
    private static Uri uriIccProfileOut = null;
    private static boolean showTransformedBitmap = true;
    private static boolean transformedBitmapIsValid = false;

    private static double powerFactor = LutCalculate.DEFAULT_POWER_FACTOR;
    private static final int POWER_FACTOR_SEEK_BAR_MAX = 100;
    private static final int POWER_FACTOR_SEEK_BAR_MIN = 0;
    private static int Lselect = LutCalculate.DEFAULT_L_SELECT;
    private static int Cselect = LutCalculate.DEFAULT_C_SELECT;

    private ArrayList<Integer> colorControlVerticesX = new ArrayList<Integer>();
    private ArrayList<Integer> colorControlVerticesY = new ArrayList<Integer>();
    private ArrayList<Integer> colorControlVerticesZ = new ArrayList<Integer>();
    private ArrayList<Integer> colorControlOutputColors = new ArrayList<Integer>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        final TabHost tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setup();

        final TabHost.TabSpec tabImageView = tabHost.newTabSpec("Image View");
        final TabHost.TabSpec tabSettings = tabHost.newTabSpec("Settings");
        final TabHost.TabSpec tabAbout = tabHost.newTabSpec("About");

        tabImageView.setIndicator("", ContextCompat.getDrawable(this,
                R.drawable.ic_action_picture));
        tabImageView.setContent(R.id.tabImageView);


        tabSettings.setIndicator("", ContextCompat.getDrawable(this,
                R.drawable.ic_action_gear));
        tabSettings.setContent(R.id.tabSettings);

        tabAbout.setIndicator("", ContextCompat.getDrawable(this,
                R.drawable.ic_action_help));
        tabAbout.setContent(R.id.tabAbout);

        /** Add the tabs  to the TabHost to display. */
        tabHost.addTab(tabImageView);
        tabHost.addTab(tabSettings);
        tabHost.addTab(tabAbout);





        colorLUT.initLUT(LUT_SIZE, LUT_SIZE, LUT_SIZE);

        //check savedInstance
        // for Tab state and set active tab accordingly
        // for LUT values and update (restore) accordingly
        if (savedInstanceState != null) {
            tabHost.setCurrentTab(savedInstanceState.getInt("Active Tab"));
            colorLUT.setColorLutArray(savedInstanceState.getIntArray("Color LUT"));
        }

        ((SeekBar) findViewById(R.id.seekBarChromaPowerFactor)).setMax(POWER_FACTOR_SEEK_BAR_MAX);
        ((SeekBar) findViewById(R.id.seekBarChromaPowerFactor)).setProgress(getSeekPosition(powerFactor));

        if (Lselect == LutCalculate.L_SELECT_IN) {
            ((RadioButton) findViewById(R.id.radioButtonLin)).setChecked(true);
            ((RadioButton) findViewById(R.id.radioButtonLout)).setChecked(false);
        }
        else if (Lselect == LutCalculate.L_SELECT_OUT) {
            ((RadioButton) findViewById(R.id.radioButtonLin)).setChecked(false);
            ((RadioButton) findViewById(R.id.radioButtonLout)).setChecked(true);
        }
        if (Cselect == LutCalculate.C_SELECT_ABSOLUTE) {
            ((RadioButton) findViewById(R.id.radioButtonCin)).setChecked(true);
            ((RadioButton) findViewById(R.id.radioButtonCrelative)).setChecked(false);
            ((SeekBar) findViewById(R.id.seekBarChromaPowerFactor)).setEnabled(false);

        }
        else if (Cselect == LutCalculate.C_SELECT_RELATIVE) {
            ((RadioButton) findViewById(R.id.radioButtonCin)).setChecked(false);
            ((RadioButton) findViewById(R.id.radioButtonCrelative)).setChecked(true);
            ((SeekBar) findViewById(R.id.seekBarChromaPowerFactor)).setEnabled(true);
        }




        if (uriIccProfileIn != null) iccProfileIn.loadFromFile(uriIccProfileIn);
        if (uriIccProfileOut != null) iccProfileOut.loadFromFile(uriIccProfileOut);

        if (bitmapLoaded) {

            transformImage();
            updateImageViewer();

        }
        else {
            ((ImageView) findViewById(R.id.imageView)).setImageBitmap(BitmapFactory.decodeResource
                    (getResources(),R.drawable.ic_action_folder_open_blue));

        }



        ((ImageView)findViewById(R.id.imageView)).addOnLayoutChangeListener(new View.OnLayoutChangeListener() {

            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight,
                                       int oldBottom) {
                // if layout is not complete we will get all zero values for the positions, so ignore the event
                if (left == 0 && top == 0 && right == 0 && bottom == 0) {
                    return;
                } else {
                    if (bitmapLoaded) {
                        try {
                            decodeImageUri(uriBitmapOriginal, (ImageView) findViewById(R.id.imageView));
                        } catch (FileNotFoundException e) {
                            Log.e(TAG, "Failed to grab Bitmap: " + e);
                        }
                        //if (iccProfileOut.isValidProfile() && iccProfileIn.isValidProfile())
                        transformImage();
                        updateImageViewer();


                    }
                }


            }
        });

        ((SeekBar) findViewById(R.id.seekBarChromaPowerFactor)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                powerFactor = getPowerFactor(seekBar.getProgress());
                //convertImage(seekBar);
                //updateImageViewer();
            }
        });

        //when switching tabs, make sure:
        //a: update the image when switching to the imageview tab in case any settings changes were made
        //b: hide the color controls overlay if we are not on the imageview tab
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (tabId.equals(tabImageView.getTag()) ) {
                    //Log.d(TAG,"Tab changed to image view");

                    if (iccProfileIn.isValidProfile() && iccProfileOut.isValidProfile() && bitmapLoaded) {
                        recalculateTransform();
                        transformImage();
                        updateImageViewer();
                    }
                }
                else if (tabId.equals(tabAbout.getTag()) ) {
                    ((LinearLayout) findViewById(R.id.overlayColorControl)).setVisibility(View.INVISIBLE);
                    InputStream inputStream = getResources().openRawResource(R.raw.about);
                    ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
                    try {
                        int bytesRead = inputStream.read();
                        while (bytesRead != -1)
                        {
                            byteArrayStream.write(bytesRead);
                            bytesRead = inputStream.read();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //TextView textViewAbout = (TextView) findViewById(R.id.textViewAbout);
                    //textViewAbout.setText(Html.fromHtml(byteArrayStream.toString()));
                    WebView webViewAbout = (WebView) findViewById(R.id.webViewAbout);
                    webViewAbout.loadDataWithBaseURL(null, byteArrayStream.toString(), "text/html", "utf-8", null);
                }
                else
                {
                    ((LinearLayout) findViewById(R.id.overlayColorControl)).setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle toSave) {
        super.onSaveInstanceState(toSave);


        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        toSave.putInt("Active Tab", tabHost.getCurrentTab());

        toSave.putIntArray("Color LUT", colorLUT.getColorLutArray());


    }


//onActvityResult handles file selection requests for images and ICC profiles

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);


        if (requestCode == SELECT_IMAGE) {
            if (resultCode == RESULT_OK) {

                uriBitmapOriginal = resultData.getData();
                try {

                    bitmapScaledOriginal = decodeImageUri(uriBitmapOriginal,
                            ((ImageView) findViewById(R.id.imageView)));
                } catch (FileNotFoundException e) {
                    new AlertDialog.Builder(this)
                            .setTitle("Invalid Image or Image Error")
                            .setMessage(e.toString())
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing, clicking okay will close the dialog
                                }
                            })

                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }


                bitmapLoaded = true;
                transformImage();
                updateImageViewer();

            }
        }

        if (requestCode == SELECT_ICC_IN && resultCode == RESULT_OK){
            uriIccProfileIn = resultData.getData();


            //Log.d(TAG,"Selected ICC Profile file name and path: " + fNamePath);
            iccProfileIn.loadFromFile(uriIccProfileIn);


            if (iccProfileIn.isValidProfile()) {
                TextView textViewFileName = (TextView) findViewById(R.id.textViewICCinFileName);
                textViewFileName.setText(getUriFileName(uriIccProfileIn));
            }
            else {
                uriIccProfileIn = null;
                new AlertDialog.Builder(this)
                        .setTitle("Source ICC Profile Loading Error")
                        .setMessage("Not a valid ICC Profile")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing, clicking okay will close the dialog
                            }
                        })

                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }

        }

        if (requestCode == SELECT_ICC_OUT && resultCode == RESULT_OK){
            uriIccProfileOut = resultData.getData();


            //Log.d(TAG,"Selected ICC Profile file name and path: " + fNamePath);
            iccProfileOut.loadFromFile(uriIccProfileOut);

            if (iccProfileOut.isValidProfile()) {
                TextView textViewFileName = (TextView) findViewById(R.id.textViewICCoutFileName);
                textViewFileName.setText(getUriFileName(uriIccProfileOut));
            }
            else {
                uriIccProfileOut = null;
                new AlertDialog.Builder(this)
                        .setTitle("Output ICC Profile Loading Error")
                        .setMessage("Not a valid ICC Profile")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing, clicking okay will close the dialog
                            }
                        })

                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }

        }
    }




    public void openImageFile(View view) {

        Intent imagePickerIntent = new Intent(Intent.ACTION_PICK);
        imagePickerIntent.setType("image/*");
        startActivityForResult(imagePickerIntent, SELECT_IMAGE);

    }

    public void openICCFile(View view) {

        Intent filePickerIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        filePickerIntent.addCategory(Intent.CATEGORY_OPENABLE);


        filePickerIntent.setType("*/*");
        //filePickerIntent.setType("application/vnd.iccprofile/*");

        int requestCode = 0;
        if (view.getId() == R.id.buttonLoadICCin)
        {
            requestCode = SELECT_ICC_IN;
        }
        else if (view.getId() == R.id.buttonLoadICCout)
        {
            requestCode = SELECT_ICC_OUT;
        }


        startActivityForResult(filePickerIntent, requestCode);
    }


    //recalculate the LUT transform and convert the Image
    //only if ICC profiles are valid and bitmap is loaded

    public void recalculateTransform () {
        colorLUT.initLUT(LUT_SIZE,LUT_SIZE,LUT_SIZE);
        if (iccProfileIn.isValidProfile() && iccProfileOut.isValidProfile()) {

            LutCalculate.calculateLUT(colorLUT, iccProfileIn, iccProfileOut, powerFactor, Cselect, Lselect);
            for (int i=0;i<colorControlVerticesX.size();i++)
            {
                colorLUT.setLUTElement(colorControlVerticesX.get(i), colorControlVerticesY.get(i),
                        colorControlVerticesZ.get(i), colorControlVerticesZ.get(i));
            }
        }
        else {
            String toastMessage = null;
            if (!iccProfileIn.isValidProfile() && !iccProfileOut.isValidProfile())
            {
                toastMessage = "Source and Output ICC Profiles are invalid or not loaded";
            }
            else if (!iccProfileIn.isValidProfile()) {
                toastMessage = "Source ICC Profile is invalid or not loaded";
            }
            else if (!iccProfileOut.isValidProfile()) {
                toastMessage = "Output ICC Profile is invalid or not loaded";
            }
            else {
                toastMessage = "Cannot transform image - unknown error";
            }
            Toast.makeText(getApplicationContext(),toastMessage, Toast.LENGTH_SHORT).show();
            for (int i=0;i<colorControlVerticesX.size();i++)
            {
                colorLUT.setLUTElement(colorControlVerticesX.get(i), colorControlVerticesY.get(i),
                        colorControlVerticesZ.get(i), colorControlVerticesZ.get(i));
            }

        }
    }



    public void transformImage() {
        if (bitmapLoaded) {
            new ConvertImageTask().execute();
        }
    }

    // update the ImageViewer
    // we only want to show a bitmap if there is a bitmap loaded
    // if a bitmap is loaded - show the transformed version if valid and desired
    // if a bitmap is loaded - show the X button to allow it to be closed.
    // if no bitmap loaded, show the open file icon, hide the X button
    private void updateImageViewer()
    {
        if (bitmapLoaded) {

            if (showTransformedBitmap && transformedBitmapIsValid) {
                ((ImageView) findViewById(R.id.imageView)).setImageBitmap(bitmapScaledTransform);
            } else {
                ((ImageView) findViewById(R.id.imageView)).setImageBitmap(bitmapScaledOriginal);
            }
            ((ImageButton) findViewById(R.id.buttonImageClose)).setVisibility(View.VISIBLE);
        }
        else
        {
            ((ImageButton) findViewById(R.id.buttonImageClose)).setVisibility(View.INVISIBLE);
            ((ImageView) findViewById(R.id.imageView)).setImageBitmap(BitmapFactory.decodeResource
                    (getResources(), R.drawable.ic_action_folder_open_blue));
        }
    }



    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        LinearLayout overlayColorControl = (LinearLayout) findViewById(R.id.overlayColorControl);
        ImageButton buttonImageClose = (ImageButton) findViewById(R.id.buttonImageClose);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);

        if (event.getAction() == MotionEvent.ACTION_UP) {

            if (buttonImageClose.getVisibility() == View.VISIBLE)
            {
                if (clickInButtonImageClose((int)event.getX(), (int)event.getY()))
                {
                    closeImage((View)findViewById(R.id.imageView));
                }
            }

            if ( (overlayColorControl.getVisibility() == View.INVISIBLE) &&
                    ( ((TabHost)findViewById(R.id.tabHost)).getCurrentTab() == 0) )
            {

                if (clickInImage((int)event.getX(), (int)event.getY(), imageView))
                {

                    if (bitmapLoaded) {
                        int imagePixelLocation[] = clickImagePixelLocation((int)event.getX(),
                                (int)event.getY(), imageView);
                        clickColourChange(imagePixelLocation[0], imagePixelLocation[1], imageView);
                    }
                    else {
                        openImageFile(imageView);
                    }

                }
            }
        }

        return super.dispatchTouchEvent(event);
    }

    public void closeImage(View view) {
        bitmapLoaded = false;
        updateImageViewer();
    }



// Start Touch Event Location helper functions
    // check if touch event coordinate is in the image button location
    private boolean clickInButtonImageClose(int x, int y) {
        ImageButton buttonImageClose = (ImageButton) findViewById(R.id.buttonImageClose);

        if (buttonImageClose.getVisibility() != View.VISIBLE)
        {
            return false;
        }

        int[] buttonCoords = new int[2];

        buttonImageClose.getLocationOnScreen(buttonCoords);

        int[] buttonSize = new int[2];
        buttonSize[0] = buttonImageClose.getWidth();
        buttonSize[1] = buttonImageClose.getHeight();

        if ( (x >= buttonCoords[0]) && (x <= buttonCoords[0] + buttonSize[0] ) &&
                (y >= buttonCoords[1]) && (y <= buttonCoords[1] + buttonSize[1] ) ) {
            return true;
        }
        else
        {
            return false;
        }
    }

    // check if touch event coordinate is in the image location
    private boolean clickInImage(int x, int y, ImageView imageView) {

        //ImageView imageViewer = (ImageView) findViewById(R.id.imageView);


        if (imageView.getVisibility() != View.VISIBLE)
        {
            return false;
        }

        int[] imageViewCoords = new int[2];
        imageView.getLocationOnScreen(imageViewCoords);

        float[] imageViewMatrix = new float[9];
        imageView.getImageMatrix().getValues(imageViewMatrix);
        float scaleX = imageViewMatrix[Matrix.MSCALE_X];
        float scaleY = imageViewMatrix[Matrix.MSCALE_Y];

        Bitmap bitmap = null;
        int bitmapWidth = 0;
        int bitmapHeight = 0;

        try {
            bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            bitmapWidth = bitmap.getWidth();
            bitmapHeight = bitmap.getHeight();
        } catch (NullPointerException npe) {
            Log.e(TAG, "Failed to extract Bitmap from ImageView: " + npe);
        }

        //assuming Bitmap is centred in imageViewer
        int scaledBitmapWidth = Math.round(bitmapWidth * scaleX);
        int scaledBitmapHeight = Math.round(bitmapHeight * scaleY);

        int xOffsetBitmap2imageViewer = (imageView.getWidth() -
                    scaledBitmapWidth) / 2;
        int yOffsetBitmap2imageViewer = (imageView.getHeight() -
                   scaledBitmapHeight) / 2;


        // get total bitmap offset vs. screen origin
        int xTotalOffset = imageViewCoords[0] + xOffsetBitmap2imageViewer;
        int yTotalOffset = imageViewCoords[1] + yOffsetBitmap2imageViewer;


        if ( (x >= xTotalOffset) && (x <= xTotalOffset + scaledBitmapWidth)
                && (y >= yTotalOffset) && (y <= yTotalOffset + scaledBitmapHeight) )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //returns the unscaled bitmap pixel location based on click position in image viewer
    private int[] clickImagePixelLocation(int x, int y, ImageView imageView) {
        int[] pixelLocation = new int[2];

        //ImageView imageViewer = (ImageView) findViewById(R.id.imageView);
        //Bitmap bitmap = null;
        //Drawable displayedDrawable = null;

        int bitmapWidth;
        int bitmapHeight;

        try {
            //bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            bitmapWidth = bitmapScaledOriginal.getWidth();
            bitmapHeight = bitmapScaledOriginal.getHeight();
        } catch (NullPointerException npe) {
            Log.e(TAG, "Failed to extract Bitmap from ImageView or " +
                    "access width and height parameters: " + npe);
            pixelLocation[0] = pixelLocation[1] = 0;
            return pixelLocation;
        }


        float[] imageMatrixValues = new float[9];
        imageView.getImageMatrix().getValues(imageMatrixValues);
        float scaleX = imageMatrixValues[Matrix.MSCALE_X];
        float scaleY = imageMatrixValues[Matrix.MSCALE_Y];


        int[] imageViewerLoc = new int[2];
        imageView.getLocationOnScreen(imageViewerLoc);


        //assuming Bitmap is centred in imageViewer
        int xOffsetBitmap2imageViewer = (imageView.getWidth() -
                Math.round(bitmapWidth * scaleX)) / 2;
        int yOffsetBitmap2imageViewer = (imageView.getHeight() -
                Math.round(bitmapHeight * scaleY)) / 2;


        // get total bitmap offset vs. screen origin
        int xTotalOffset = imageViewerLoc[0] + xOffsetBitmap2imageViewer;
        int yTotalOffset = imageViewerLoc[1] + yOffsetBitmap2imageViewer;

        int xLocationScaledBitmap = x - xTotalOffset;
        int yLocationScaledBitmap = y - yTotalOffset;

        pixelLocation[0] = Math.round(xLocationScaledBitmap/scaleX);
        pixelLocation[1] = Math.round(yLocationScaledBitmap/scaleY);

        Log.d(TAG, "Pixel location x,y = " + Integer.toString(pixelLocation[0]) + ", "
                + Integer.toString(pixelLocation[1]));



        if (pixelLocation[0] < 0) pixelLocation[0] = 0;
        if (pixelLocation[0] > (bitmapWidth - 1))
            pixelLocation[0] = (bitmapWidth - 1);

        if (pixelLocation[1] < 0) pixelLocation[1] = 0;
        if (pixelLocation[1] > (bitmapHeight - 1))
            pixelLocation[1] = (bitmapHeight - 1);

        return pixelLocation;
    }
//End Touch Event Location Helpers

// Start Color Controls Handler Functions

    public void resetColorControlAll (View view)
    {
        new AlertDialog.Builder(this)
                .setTitle("Warning: Reset All? ")
                .setMessage("Choosing OK will reset any changes made with the colour controls.  " +
                        "Do you want to proceed?")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "ok");
                        LinearLayout overlayColorControl = (LinearLayout) findViewById(R.id.overlayColorControl);
                        overlayColorControl.setVisibility(LinearLayout.INVISIBLE);
                        colorControlVerticesX.clear();
                        colorControlVerticesY.clear();
                        colorControlVerticesZ.clear();
                        colorControlOutputColors.clear();

                        recalculateTransform();
                        transformImage();
                        updateImageViewer();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG,"cancel");
                        // do nothing - leave the LUT as is
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

    public void resetColorControl (View view)
    {
        ImageView colourBlock21 = (ImageView) findViewById(R.id.colour21);
        ColorDrawable colourDrawable = (ColorDrawable) colourBlock21.getBackground();
        int[] verticeCoords = colorLUT.getNearestVerticeCoords(colourDrawable.getColor());


        int resetColor = colorLUT.getLUTElement(verticeCoords[0], verticeCoords[1], verticeCoords[2]);

        ImageView colourBlock22 = (ImageView) findViewById(R.id.colour22);
        colourBlock22.setBackgroundColor(resetColor);
    }

    // handle the Color Control overlay radio button clicks
    public void setImageViewBitmap(View view) {
        if (bitmapLoaded) {
            if (view == (View)findViewById(R.id.radioButtonImageOriginal))
            {
                showTransformedBitmap = false;
            }
            else if (view == (View)findViewById(R.id.radioButtonImageTransformed))
            {
                showTransformedBitmap = true;
            }
            LinearLayout overlayColorControl = (LinearLayout) findViewById(R.id.overlayColorControl);
            overlayColorControl.setVisibility(LinearLayout.INVISIBLE);
            updateImageViewer();
        }
    }

    // get the colour at the pixel touched and set up the Color Controls view to match that colour
    private void clickColourChange(int x, int y, ImageView imageView){
        //ImageView imageViewer = (ImageView) findViewById(R.id.imageView);

        //Bitmap bitmap = null;

        int colourAtPixel = 0;

        try {
            //bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            //colourAtPixel = bitmap.getPixel(x, y);
            //grab the value of the original, not converted image
            colourAtPixel = bitmapScaledOriginal.getPixel(x, y);
        } catch (NullPointerException npe) {
            Log.e(TAG, "Failed to grab Bitmap from ImageView or invalid pixel request" +
                    " (i.e. out of bounds): " + npe);
            return;
        }


        verticeCoords = colorLUT.getNearestVerticeCoords(colourAtPixel);
        int inColor = colorLUT.getInputColor(verticeCoords[0], verticeCoords[1], verticeCoords[2]);

        int outColor = colorLUT.getLUTElement(verticeCoords[0], verticeCoords[1], verticeCoords[2]);

        /*
        Log.d(TAG, "Pixel x,y = " + Integer.toString(x) + ", " + Integer.toString(y)
                + " Colour = " + Integer.toHexString(colourAtPixel) + ", nearest input colour = " +
                Integer.toHexString(inColor) + ", output colour = " + Integer.toHexString(outColor));
        */

        LinearLayout overlayColorControl = (LinearLayout) findViewById(R.id.overlayColorControl);
        overlayColorControl.setVisibility(LinearLayout.VISIBLE);

        if (showTransformedBitmap && transformedBitmapIsValid) {
            ((RadioButton) findViewById(R.id.radioButtonImageOriginal)).setChecked(false);
            ((RadioButton) findViewById(R.id.radioButtonImageTransformed)).setChecked(true);
        }
        else
        {
            ((RadioButton) findViewById(R.id.radioButtonImageOriginal)).setChecked(true);
            ((RadioButton) findViewById(R.id.radioButtonImageTransformed)).setChecked(false);
        }

        ImageView colourBlock11 = (ImageView) findViewById(R.id.colour11);
        ImageView colourBlock12 = (ImageView) findViewById(R.id.colour12);
        ImageView colourBlock13 = (ImageView) findViewById(R.id.colour13);
        ImageView colourBlock21 = (ImageView) findViewById(R.id.colour21);
        ImageView colourBlock22 = (ImageView) findViewById(R.id.colour22);
        ImageView colourBlock23 = (ImageView) findViewById(R.id.colour23);
        ImageView colourBlock31 = (ImageView) findViewById(R.id.colour31);
        ImageView colourBlock32 = (ImageView) findViewById(R.id.colour32);
        ImageView colourBlock33 = (ImageView) findViewById(R.id.colour33);

        colourBlock11.setBackgroundColor(inColor);
        colourBlock12.setBackgroundColor(inColor);
        colourBlock13.setBackgroundColor(inColor);
        colourBlock21.setBackgroundColor(inColor);
        colourBlock23.setBackgroundColor(inColor);
        colourBlock31.setBackgroundColor(inColor);
        colourBlock32.setBackgroundColor(inColor);
        colourBlock33.setBackgroundColor(inColor);


        colourBlock22.setBackgroundColor(outColor);

        /*
        Log.d(TAG,"nearest vertice = " + Integer.toString(verticeCoords[0]) + ", "
                + Integer.toString(verticeCoords[1]) + ", " + Integer.toString(verticeCoords[2])
                + " and output Colour = " + Integer.toHexString(outColor));
        */
    }

    public void cancelButtonClick (View view) {
        LinearLayout overlayColorControl = (LinearLayout) findViewById(R.id.overlayColorControl);
        overlayColorControl.setVisibility(LinearLayout.INVISIBLE);
    }

    public void acceptButtonClick (View view) {
        LinearLayout overlayColorControl = (LinearLayout) findViewById(R.id.overlayColorControl);
        overlayColorControl.setVisibility(LinearLayout.INVISIBLE);
        ImageView colourBlock22 = (ImageView) findViewById(R.id.colour22);
        ColorDrawable colourDrawable = (ColorDrawable) colourBlock22.getBackground();
        int setColour = colourDrawable.getColor();
        colorLUT.setLUTElement(verticeCoords[0], verticeCoords[1], verticeCoords[2], setColour);
        colorControlVerticesX.add(verticeCoords[0]);
        colorControlVerticesY.add(verticeCoords[1]);
        colorControlVerticesZ.add(verticeCoords[2]);
        colorControlOutputColors.add(setColour);

        transformImage();
        updateImageViewer();
    }

    public void hsvButtonClick (View view) {
        ImageView colourBlock22 = (ImageView) findViewById(R.id.colour22);
        ColorDrawable colourDrawable = (ColorDrawable) colourBlock22.getBackground();
        int setColour = colourDrawable.getColor();
        int newColour = 0;
        float hsvColour[] = new float[3];
        Color.colorToHSV(setColour, hsvColour);

        if (view.getId() == R.id.hueDownButton) {

            //Log.d(TAG, "Hue Down Button Clicked");

            hsvColour[0] = hsvColour[0] - H_ADJUST;
            if (hsvColour[0] < 0f ) hsvColour[0] = 360f + hsvColour[0];


        }
        if (view.getId() == R.id.hueUpButton) {
            //Log.d(TAG, "Hue Up Button Clicked");

            hsvColour[0] = hsvColour[0] + H_ADJUST;
            if (hsvColour[0] > 360f ) hsvColour[0] = hsvColour[0] - 360f;

        }
        if (view.getId() == R.id.saturationDownButton) {
            //Log.d(TAG, "Saturation Down Button Clicked");

            hsvColour[1] = hsvColour[1] - S_ADJUST;
            //if (hsvColour[1] < 0f ) hsvColour[1] = 1f + hsvColour[1];
            if (hsvColour[1] < 0f ) hsvColour[1] = 0f;

        }
        if (view.getId() == R.id.saturationUpButton) {
            //Log.d(TAG, "Saturation Up Button Clicked");

            hsvColour[1] = hsvColour[1] + S_ADJUST;
            //if (hsvColour[1] > 1f ) hsvColour[1] = hsvColour[1] - 1f;
            if (hsvColour[1] > 1f ) hsvColour[1] = 1f;

        }
        if (view.getId() == R.id.lightnessDownButton) {
            //Log.d(TAG, "Lightness Down Button Clicked");

            hsvColour[2] = hsvColour[2] - V_ADJUST;
            //if (hsvColour[2] < 0f ) hsvColour[2] = 1f + hsvColour[2];
            if (hsvColour[2] < 0f ) hsvColour[2] = 0f;

        }
        if (view.getId() == R.id.lightnessUpButton) {
            //Log.d(TAG, "Lightness Up Button Clicked");

            hsvColour[2] = hsvColour[2] + V_ADJUST;
            //if (hsvColour[2] > 1f ) hsvColour[2] = hsvColour[2] - 1f;
            if (hsvColour[2] > 1f ) hsvColour[2] = 1f;
        }

        newColour = Color.HSVToColor(hsvColour);
        colourBlock22.setBackgroundColor(newColour);

    }
 // End of Color Controls Overlay handler functions


   //Handle changes in the settings UI
    public void updateSettings (View view) {
        if (view == (View)findViewById(R.id.radioButtonLin)) {
            Lselect = LutCalculate.L_SELECT_IN;
        }
        if (view == (View)findViewById(R.id.radioButtonLout)) {
            Lselect = LutCalculate.L_SELECT_OUT;
        }
        if (view == (View)findViewById(R.id.radioButtonCin)) {
            ((SeekBar) findViewById(R.id.seekBarChromaPowerFactor)).setEnabled(false);
            Cselect = LutCalculate.C_SELECT_ABSOLUTE;
        }
        if (view == (View)findViewById(R.id.radioButtonCrelative)) {
            ((SeekBar) findViewById(R.id.seekBarChromaPowerFactor)).setEnabled(true);
            Cselect = LutCalculate.C_SELECT_RELATIVE;
        }

        //No need to convert and update image here, handled instead by tab change listener -
        //waits for change back to image view tab before applying settings changes
        //convertImage(view);
        //updateImageViewer();

    }
    //End handle settings UI changes

 // Helper Functions (could be moved to another file)
    private String getUriFileName (Uri uri)
    {
        String uriString = uri.toString();
        File file = new File(uriString);
        String path = file.getAbsolutePath();
        String displayName = null;

        if (uriString.startsWith("content://")) {
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        } else if (uriString.startsWith("file://")) {
            displayName = file.getName();
        }
        return displayName;
    }

    private double getPowerFactor(int seekPosition)
    {
        double yIntercept;
        double slope;

        slope = (LutCalculate.MAX_POWER_FACTOR - LutCalculate.MIN_POWER_FACTOR) /
                ((double) (POWER_FACTOR_SEEK_BAR_MAX -POWER_FACTOR_SEEK_BAR_MIN));

        yIntercept = LutCalculate.MAX_POWER_FACTOR - (slope * (double)(POWER_FACTOR_SEEK_BAR_MAX));

        return slope * ((double)seekPosition) + yIntercept;

    }

    private int getSeekPosition (double powerFactorIn) {
        double yIntercept;
        double slope;

        slope = ((double) (POWER_FACTOR_SEEK_BAR_MAX -POWER_FACTOR_SEEK_BAR_MIN)) /
                (LutCalculate.MAX_POWER_FACTOR - LutCalculate.MIN_POWER_FACTOR);

        yIntercept = (double)POWER_FACTOR_SEEK_BAR_MAX - (slope * LutCalculate.MAX_POWER_FACTOR);

        return (int) Math.round(slope * powerFactorIn + yIntercept);
    }

    //decodeImageUri handles the scaling of the image to fit the imageView object that will be displaying it
    //To preserve memory and enhance performance, the imageviewer holds a scaled image (where applicable)
    //instead of the full size image
    private Bitmap decodeImageUri(Uri selectedImage, ImageView imageView) throws FileNotFoundException {

        // Decode image size (i.e. only read enough of the bitmap to determine its size)
        BitmapFactory.Options imageOptions = new BitmapFactory.Options();
        imageOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, imageOptions);


        int yScale = 1;
        int xScale = 1;
        int scale;

        //  To determine the magnitude of the scale, Get the size of ImageView component we are drawing to
        // (note, this assumes the InageView is already sized to fit the screen area available


        if ((imageOptions.outHeight > imageView.getHeight()) && (imageView.getHeight() > 0)) {
            yScale =  imageOptions.outHeight / imageView.getHeight();
        }

        if ((imageOptions.outWidth > imageView.getWidth()) && (imageView.getWidth() > 0)) {
            xScale =  imageOptions.outWidth / imageView.getWidth();
        }

        scale = Math.max(xScale, yScale);


        Log.d(TAG, "ImageView Height:" + Integer.toString(imageView.getHeight()));
        Log.d(TAG, "ImageView Width :" + Integer.toString(imageView.getWidth()));
        Log.d(TAG, "Bitmap Height:" + Integer.toString(imageOptions.outHeight));
        Log.d(TAG, "Bitmap Width :" + Integer.toString(imageOptions.outWidth));
        Log.d(TAG, "Scale :" + Integer.toString(scale));


        // Decode bitmap (Scale it)
        BitmapFactory.Options imageOptionsOut = new BitmapFactory.Options();
        imageOptionsOut.inSampleSize = scale;

        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, imageOptionsOut);
    }



    //end of helper functions


    //multi-threading handler (convert image on separate thread from main UI thread)
    private class ConvertImageTask extends AsyncTask<Void, Integer, Bitmap> {

        private ImageView imageViewer = null;
        private Bitmap mutableConvertBmp = null;
        private View progressView = findViewById(R.id.overlayProgress);

        protected void onPreExecute() {
            super.onPreExecute();
            imageViewer = (ImageView) findViewById(R.id.imageView);


            if (SHOW_PROGRESS_BAR) progressView.setVisibility(View.VISIBLE);


            try {

                mutableConvertBmp = bitmapScaledOriginal.copy(bitmapScaledOriginal.getConfig(), true);
            } catch (NullPointerException npe) {
                System.out.println("Failed to copy Bitmap " + npe);
            }

        }
        @Override
        protected Bitmap doInBackground(Void... voids) {


            return colorLUT.runLookup(mutableConvertBmp);

        }


        protected void onPostExecute(Bitmap result) {

            bitmapScaledTransform = result;
            transformedBitmapIsValid = true;
            updateImageViewer();
            progressView.setVisibility(View.INVISIBLE);

        }

        protected void onProgressUpdate(Integer... progress) {
            ProgressBar progressBar;
            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setProgress(progress[0]);

        }

    }
    // end of multi-thread handling




}

