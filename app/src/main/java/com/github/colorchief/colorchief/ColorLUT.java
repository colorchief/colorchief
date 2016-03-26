/***************************************************************************************************
 Color Chief - ColorLUT Class - an object that holds 3D LUT values and handles renderscript
 execution of the lookup function via RenderScript
 ***************************************************************************************************

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
 **************************************************************************************************/

package com.github.colorchief.colorchief;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Color;

import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsic3DLUT;
import android.support.v8.renderscript.Type;
import android.util.Log;

public class ColorLUT extends ContextWrapper {



    private static final String TAG = "ColorLUT";
    private ScriptIntrinsic3DLUT sI3dLut;
    private RenderScript rS;
    private Allocation allocLut;
    private int lutSizeX;
    private int lutSizeY;
    private int lutSizeZ;
    private int colorLutArray[];


    public ColorLUT(Context base) {
        super(base);
    }


    // initializes LUT parameters (to unity) and sets up renderscript
    public void initLUT (int setSizeX, int setSizeY, int setSizeZ) {

        lutSizeX = setSizeX;
        lutSizeY = setSizeY;
        lutSizeZ = setSizeZ;
        colorLutArray = new int[lutSizeX * lutSizeY * lutSizeZ];

        rS = RenderScript.create(this);

        Type.Builder tb = new Type.Builder(rS, Element.U8_4(rS));
        tb.setX(lutSizeX);
        tb.setY(lutSizeY);
        tb.setZ(lutSizeZ);
        Type t = tb.create();


        allocLut = Allocation.createTyped(rS, t);


        for (int x = 0; x < lutSizeX; x++) {
            for (int y = 0; y < lutSizeY; y++) {
                for (int z = 0; z < lutSizeZ; z++) {


                    colorLutArray[x * lutSizeY * lutSizeZ + y * lutSizeZ + z] = Color.argb(0xff,
                            (0xff * x / (lutSizeX - 1)),
                            (0xff * y / (lutSizeY - 1)),
                            (0xff * z / (lutSizeZ - 1)));
                    /*
                    Log.d(TAG, "x,y,z = " + Integer.toString(x) + ", " + Integer.toString(y) + ", " +
                            Integer.toString(z) + " = " + Integer.toHexString(colorLutArray[x * lutSizeY * lutSizeZ + y * lutSizeZ + z]));
                    */

                }
            }
        }
        allocLut.copyFromUnchecked(colorLutArray);
        sI3dLut = ScriptIntrinsic3DLUT.create(rS, Element.U8_4(rS));
        sI3dLut.setLUT(allocLut);
    }

    public void setLUTElement (int verticeX, int verticeY, int verticeZ, int verticeColor) {
        colorLutArray[verticeX * lutSizeY * lutSizeZ + verticeY * lutSizeZ + verticeZ] = verticeColor;
        allocLut.copyFromUnchecked(colorLutArray);
        sI3dLut.setLUT(allocLut);

    }

    public int getLUTElement (int verticeX, int verticeY, int verticeZ) {
        //Log.d(TAG, verticeX + ", " + verticeY + ", " + verticeZ);
        return colorLutArray[verticeX * lutSizeY * lutSizeZ + verticeY * lutSizeZ + verticeZ];
    }

    public int[] getColorLutArray ()
    {
        return colorLutArray;
    }

    public void setColorLutArray (int[] inArray)
    {
        colorLutArray = inArray;
        allocLut.copyFromUnchecked(colorLutArray);
        sI3dLut.setLUT(allocLut);
    }

    public int getInputColor (int verticeX, int verticeY, int verticeZ) {
            int r = (0xff * verticeX / (lutSizeX - 1));
            int g = (0xff * verticeY / (lutSizeY - 1));
            int b = (0xff * verticeZ / (lutSizeZ - 1));

        if (r < 0) r = 0;
        if (r > 255) r = 255;
        if (g < 0) g = 0;
        if (g > 255) g = 255;
        if (b < 0) b = 0;
        if (b > 255) b = 255;

        return Color.rgb(r,g,b);

    }

    public int[] getNearestVerticeCoords (int inputColor) {

        float xI, yI, zI;
        int coordOut[] = new int[3];

        xI = ( (float) Color.red(inputColor) * (lutSizeX - 1) ) / ( (float) 0xff);
        yI = ( (float) Color.green(inputColor) * (lutSizeY - 1) ) / ( (float) 0xff);
        zI = ( (float) Color.blue(inputColor) * (lutSizeZ - 1) ) / ( (float) 0xff);


        coordOut[0] = Math.round(xI);
        coordOut[1] = Math.round(yI);
        coordOut[2] = Math.round(zI);


        /*
        Log.d (TAG, "In X,Y,Z = " + Integer.toHexString(inputColor) + "; Out X,Y,Z = " +
                Integer.toHexString(coordOut[0]) + ", " + Integer.toHexString(coordOut[1]) + ", " +
                Integer.toHexString(coordOut[2]));
        Log.d (TAG, "float X,Y,Z = " + Float.toString(xI) + ", " + Float.toString(yI) + ", " +
                Float.toString(zI));
        */

        if (coordOut[0] >  lutSizeX - 1) Log.e(TAG, "X coord out of range" + Integer.toString(coordOut[0]));
        if (coordOut[1] >  lutSizeY - 1) Log.e(TAG, "Y coord out of range" + Integer.toString(coordOut[1]));
        if (coordOut[2] >  lutSizeZ - 1) Log.e(TAG, "Z coord out of range" + Integer.toString(coordOut[2]));

        return coordOut;
    }

    public void debugVertices() {
        int runningCount=0;
        int tempInt;

        for (int x = 0; x < lutSizeX; x++) {
            for (int y = 0; y < lutSizeY; y++) {
                for (int z = 0; z < lutSizeZ; z++ ) {
                    runningCount++;

                    tempInt = getLUTElement(x, y, z);
                    Log.d(TAG, "(X,Y,Z): (" + Integer.toString(x) + ", " + Integer.toString(y) +
                            ", " + Integer.toString(z) + " = " +
                            Integer.toHexString(Color.red(tempInt)) + ", " +
                            Integer.toHexString(Color.green(tempInt)) + ", " +
                            Integer.toHexString(Color.blue(tempInt)) + ";      " +
                            Integer.toHexString(tempInt) + "; LUT Size (bytes) = " +
                            Integer.toString(allocLut.getBytesSize()) + "; allocation value = " +
                            Integer.toHexString(tempInt) +
                            "running count = " + runningCount);

                }
            }
        }


    }

    public Bitmap runLookup(Bitmap bitmapIn) {
        Bitmap bitmapOut;
        Allocation allocBitmapIn;
        Allocation allocBitmapOut;

        bitmapOut = Bitmap.createBitmap(bitmapIn.getWidth(), bitmapIn.getHeight(), bitmapIn.getConfig());

        allocBitmapIn = Allocation.createFromBitmap(rS, bitmapIn);
        allocBitmapOut = Allocation.createFromBitmap(rS, bitmapOut);


        sI3dLut.forEach(allocBitmapIn, allocBitmapOut);

        allocBitmapOut.copyTo(bitmapOut);
        return bitmapOut;
    }


    public int getSizeX()
    {
        return lutSizeX;
    }
    public int getSizeY()
    {
        return lutSizeY;
    }
    public int getSizeZ()
    {
        return lutSizeZ;
    }

}