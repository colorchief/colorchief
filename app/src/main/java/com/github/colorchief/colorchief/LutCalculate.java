/***************************************************************************************************
 Color Chief - LutCalculate Class - a helper class to MainActivity facilitating LUT calculation
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

import android.graphics.Color;
import android.util.Log;


public class LutCalculate {

    private final static String TAG = "LutCalculate";
    public final static double DEFAULT_POWER_FACTOR = 1.0;
    public final static double MAX_POWER_FACTOR = 5.0;
    public final static double MIN_POWER_FACTOR = 0.1;
    public final static int L_SELECT_IN = 1;
    public final static int L_SELECT_OUT = 2;
    public final static int DEFAULT_L_SELECT = L_SELECT_OUT;
    public final static int C_SELECT_ABSOLUTE = 1;
    public final static int C_SELECT_RELATIVE = 2;
    public final static int DEFAULT_C_SELECT = C_SELECT_RELATIVE;

    public LutCalculate()
    {

    }

    public static void calculateXYZ (ICCProfile iccIn, int colorIn)
    {
        /*AbsoluteColor absCol = new AbsoluteColor();

        absCol.setRGB(colorIn);

        TRC rTRC = iccIn.getrTRC();
        TRC gTRC = iccIn.getgTRC();
        TRC bTRC = iccIn.getbTRC();

        double[] rXYZ = iccIn.getrCIEXYZ();
        double[] gXYZ = iccIn.getgCIEXYZ();
        double[] bXYZ = iccIn.getbCIEXYZ();

        absCol.calculateXYZ(rTRC, gTRC, bTRC, rXYZ, gXYZ, bXYZ);

        double XYZ[] = absCol.getXYZ();



        Log.d(TAG,"R,G,B = " + Integer.toHexString(Color.red(colorIn)) + ", " +
                Integer.toHexString(Color.green(colorIn)) + ", " +
                Integer.toHexString(Color.blue(colorIn)) + "; => X,Y,Z = " +
                Double.toString(XYZ[0]) + ", " + Double.toString(XYZ[1]) + "," +
                Double.toString(XYZ[2]));
       */
    }

    public static void calculateLUT (ColorLUT cLUT, ICCProfile iccIn, ICCProfile iccOut)
    {
       calculateLUT(cLUT, iccIn, iccOut, DEFAULT_POWER_FACTOR, DEFAULT_C_SELECT, DEFAULT_L_SELECT);
    }

    public static void calculateLUT (ColorLUT cLUT, ICCProfile iccIn, ICCProfile iccOut, double powerFactor, int Cselect, int Lselect)
    {
        if (powerFactor < MIN_POWER_FACTOR) powerFactor = MIN_POWER_FACTOR;
        if (powerFactor > MAX_POWER_FACTOR) powerFactor = MAX_POWER_FACTOR;

        double rInLCh[] = AbsoluteColor.RGB2LCh(Color.RED, iccIn, false);
        double gInLCh[] = AbsoluteColor.RGB2LCh(Color.GREEN, iccIn, false);
        double bInLCh[] = AbsoluteColor.RGB2LCh(Color.BLUE, iccIn, false);
        double cInLCh[] = AbsoluteColor.RGB2LCh(Color.CYAN, iccIn, false);
        double mInLCh[] = AbsoluteColor.RGB2LCh(Color.MAGENTA, iccIn, false);
        double yInLCh[] = AbsoluteColor.RGB2LCh(Color.YELLOW, iccIn, false);

        /*
        Log.d(TAG, "R in LCh = " + Integer.toHexString(Color.RED) + ", " + Double.toString(rInLCh[0])
                + ", " + Double.toString(rInLCh[1]) + ", " + Double.toString(rInLCh[2]) + ", ");
        Log.d(TAG, "G in LCh = " + Integer.toHexString(Color.GREEN) + ", " + Double.toString(gInLCh[0])
                + ", " + Double.toString(gInLCh[1]) + ", " + Double.toString(gInLCh[2]) + ", ");
        Log.d(TAG, "B in LCh = " + Integer.toHexString(Color.BLUE) + ", " + Double.toString(bInLCh[0])
                + ", " + Double.toString(bInLCh[1]) + ", " + Double.toString(bInLCh[2]) + ", ");
        Log.d(TAG, "C in LCh = " + Integer.toHexString(Color.CYAN) + ", " + Double.toString(cInLCh[0])
                + ", " + Double.toString(cInLCh[1]) + ", " + Double.toString(cInLCh[2]) + ", ");
        Log.d(TAG, "M in LCh = " + Integer.toHexString(Color.MAGENTA) + ", " + Double.toString(mInLCh[0])
                + ", " + Double.toString(mInLCh[1]) + ", " + Double.toString(mInLCh[2]) + ", ");
        Log.d(TAG, "Y in LCh = " + Integer.toHexString(Color.YELLOW) + ", " + Double.toString(yInLCh[0])
                + ", " + Double.toString(yInLCh[1]) + ", " + Double.toString(yInLCh[2]) + ", ");
       */

        double rOutLCh[] = AbsoluteColor.RGB2LCh(Color.RED, iccOut, false);
        double gOutLCh[] = AbsoluteColor.RGB2LCh(Color.GREEN, iccOut, false);
        double bOutLCh[] = AbsoluteColor.RGB2LCh(Color.BLUE, iccOut, false);
        double cOutLCh[] = AbsoluteColor.RGB2LCh(Color.CYAN, iccOut, false);
        double mOutLCh[] = AbsoluteColor.RGB2LCh(Color.MAGENTA, iccOut, false);
        double yOutLCh[] = AbsoluteColor.RGB2LCh(Color.YELLOW, iccOut, false);
        double wOutLCh[] = AbsoluteColor.RGB2LCh(Color.WHITE, iccOut, false);
        double kOutLCh[] = AbsoluteColor.RGB2LCh(Color.BLACK, iccOut, false);

        /*
        Log.d(TAG, "R out LCh = " + Integer.toHexString(Color.RED) + ", " + Double.toString(rOutLCh[0])
                + ", " + Double.toString(rOutLCh[1]) + ", " + Double.toString(rOutLCh[2]) + ", ");
        Log.d(TAG, "G out LCh = " + Integer.toHexString(Color.GREEN) + ", " + Double.toString(gOutLCh[0])
                + ", " + Double.toString(gOutLCh[1]) + ", " + Double.toString(gOutLCh[2]) + ", ");
        Log.d(TAG, "B out LCh = " + Integer.toHexString(Color.BLUE) + ", " + Double.toString(bOutLCh[0])
                + ", " + Double.toString(bOutLCh[1]) + ", " + Double.toString(bOutLCh[2]) + ", ");
        Log.d(TAG, "C out LCh = " + Integer.toHexString(Color.CYAN) + ", " + Double.toString(cOutLCh[0])
                + ", " + Double.toString(cOutLCh[1]) + ", " + Double.toString(cOutLCh[2]) + ", ");
        Log.d(TAG, "M out LCh = " + Integer.toHexString(Color.MAGENTA) + ", " + Double.toString(mOutLCh[0])
                + ", " + Double.toString(mOutLCh[1]) + ", " + Double.toString(mOutLCh[2]) + ", ");
        Log.d(TAG, "Y out LCh = " + Integer.toHexString(Color.YELLOW) + ", " + Double.toString(yOutLCh[0])
                + ", " + Double.toString(yOutLCh[1]) + ", " + Double.toString(yOutLCh[2]) + ", ");
        Log.d(TAG, "W out LCh = " + Integer.toHexString(Color.WHITE) + ", " + Double.toString(wOutLCh[0])
                + ", " + Double.toString(wOutLCh[1]) + ", " + Double.toString(wOutLCh[2]) + ", ");
        Log.d(TAG, "K out LCh = " + Integer.toHexString(Color.BLACK) + ", " + Double.toString(kOutLCh[0])
                + ", " + Double.toString(kOutLCh[1]) + ", " + Double.toString(kOutLCh[2]) + ", ");
        */



        //set out hue (h) to equal in hue

        rOutLCh[2] = rInLCh[2];
        gOutLCh[2] = gInLCh[2];
        bOutLCh[2] = bInLCh[2];
        cOutLCh[2] = cInLCh[2];
        mOutLCh[2] = mInLCh[2];
        yOutLCh[2] = yInLCh[2];

        //determine max chroma scale factor

        double rCmaxScale = rOutLCh[1] / rInLCh[1];
        double gCmaxScale = gOutLCh[1] / gInLCh[1];
        double bCmaxScale = bOutLCh[1] / bInLCh[1];
        double cCmaxScale = cOutLCh[1] / cInLCh[1];
        double mCmaxScale = mOutLCh[1] / mInLCh[1];
        double yCmaxScale = yOutLCh[1] / yInLCh[1];

        /*
        Log.d(TAG, "R C max scale = " + rCmaxScale);
        Log.d(TAG, "G C max scale = " + gCmaxScale);
        Log.d(TAG, "B C max scale = " + bCmaxScale);
        Log.d(TAG, "C C max scale = " + cCmaxScale);
        Log.d(TAG, "M C max scale = " + mCmaxScale);
        Log.d(TAG, "Y C max scale = " + yCmaxScale);
*/


        ChromaScaleValueLUT cScaleLUT = new ChromaScaleValueLUT();

        cScaleLUT.addElement(rInLCh[2], rCmaxScale, rInLCh[1]);
        cScaleLUT.addElement(gInLCh[2], gCmaxScale, gInLCh[1]);
        cScaleLUT.addElement(bInLCh[2], bCmaxScale, bInLCh[1]);
        cScaleLUT.addElement(cInLCh[2], cCmaxScale, cInLCh[1]);
        cScaleLUT.addElement(mInLCh[2], mCmaxScale, mInLCh[1]);
        cScaleLUT.addElement(yInLCh[2], yCmaxScale, yInLCh[1]);

        for (int x=0; x < cLUT.getSizeX(); x++)
        {
            for (int y=0; y < cLUT.getSizeY(); y++)
            {
                for (int z=0; z < cLUT.getSizeZ(); z++)
                {
                    int verticeColor;
                    int r = (x * 255) / (cLUT.getSizeX()-1);
                    int g = (y * 255) / (cLUT.getSizeY()-1);
                    int b = (z * 255) / (cLUT.getSizeZ()-1);

                    double inputLCh[] = AbsoluteColor.RGB2LCh(Color.rgb(r,g,b),iccIn,false);
                    double outputLCh[] = AbsoluteColor.RGB2LCh(Color.rgb(r,g,b),iccOut,false);


                    if (Lselect == L_SELECT_IN)
                    {
                        //Note: setting output L to input L may cause clipping
                        outputLCh[0] = inputLCh[0];
                    }
                    //else {
                        //make no change to output L
                        //outputLCh[0] = outputLCh[0];
                    //}



                    //set output hue equal to input hue
                    outputLCh[2] = inputLCh[2];

                    if (Cselect == C_SELECT_ABSOLUTE) {
                        outputLCh[1] = inputLCh[1];
                    }
                    else {
                        //assume relative
                        //calculate best chroma scaling factor

                        double maxChromaScale = cScaleLUT.lookupScale(outputLCh[2]);
                        double maxChroma = cScaleLUT.lookupChroma(outputLCh[2]);
                        double chromaScale = 1.0;

                        //if Chroma is zero, there is no need to scale it
                        if (outputLCh[1] == 0.0) {
                            chromaScale = 1.0;
                        }
                        //when the output chroma is larger than the input chroma, then don't scale
                        else if (outputLCh[1] / inputLCh[1] >= 1.0) {
                            chromaScale = 1.0;
                        } else if (outputLCh[1] >= maxChroma) {
                            chromaScale = 1.0;
                        } else if (maxChroma == 0.0) {
                            chromaScale = 1.0;
                        }

                        //otherwise determine the scale factor, but ensure it never scales by more
                        // than the maximum amount.
                        else {
                            chromaScale = powerLaw(outputLCh[1] / maxChroma, maxChromaScale,
                                    powerFactor);
                        }

                        /*
                        Log.d(TAG, "chroma scale = " + Double.toString(chromaScale) +
                                ", max chroma scale = " + Double.toString(maxChromaScale) +
                                ", max chroma = " + Double.toString(maxChroma));
                        */

                        outputLCh[1] = chromaScale * outputLCh[1];
                    }


                    verticeColor = AbsoluteColor.LCh2RGB(outputLCh,iccOut,true);
                    /*
                    Log.d(TAG, "input colour = " + Integer.toHexString(Color.argb(0xFF,r,g,b))
                            + ", output colour = " + Integer.toHexString(verticeColor));
                    */
                    cLUT.setLUTElement(x,y,z,verticeColor);


                }
            }
        }







    }


    // chromaScale = 1 - maxScale
    private static double powerLaw (double inScale, double maxScale, double powerFactor)
    {
        return 1.0 - maxScale * Math.pow(inScale, powerFactor);
    }

}
