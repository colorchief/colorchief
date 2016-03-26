/***************************************************************************************************
 Color Chief - TRC - a Object to hold a TRC (Tonal Reproduction Curve) and look-up/calculation to
 retrieve "corrected" or "inverse corrected" values from the TRC
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

import android.util.Log;

import java.util.ArrayList;


public class TRC {

    private final static String TAG = "TRC";

    public final static int TYPE_CURV_IDENTITY = 0;
    public final static int TYPE_CURV_GAMMA = 1;
    public final static int TYPE_CURV_LUT = 2;
    public final static int TYPE_PARA_G = 3;
    public final static int TYPE_PARA_GAB = 4;
    public final static int TYPE_PARA_GABC = 5;
    public final static int TYPE_PARA_GABCD = 6;
    public final static int TYPE_PARA_GABCDEF = 7;


    private int type;

    private ArrayList<Integer> lut = new ArrayList<Integer>();
    private double paramG;
    private double paramA;
    private double paramB;
    private double paramC;
    private double paramD;
    private double paramE;
    private double paramF;
    private float gamma;




    public TRC()
    {

    }

    public void setType (int setVal) {
        this.type = setVal;
    }

    public void addLutElement (int setVal) {
        this.lut.add(setVal);
    }

    public void setLutElement (int index, int setVal) {
        this.lut.add(index, setVal);
    }



    public void setParamG (double setVal) {
        this.paramG = setVal;
    }

    public void setParamA (double setVal) {
        this.paramA = setVal;
    }

    public void setParamB (double setVal) {
        this.paramB = setVal;
    }

    public void setParamC (double setVal) {
        this.paramC = setVal;
    }

    public void setParamD (double setVal) {
        this.paramD = setVal;
    }

    public void setParamE (double setVal) {
        this.paramE = setVal;
    }

    public void setParamF (double setVal) {
        this.paramF = setVal;
    }

    public void setGamma (float setVal) {
        this.gamma = setVal;
    }


    public int getType () {
        return this.type;
    }

    public int getLutElement (int index) {
        return this.lut.get(index);
    }

    public int getLutSize () {
        return this.lut.size();
    }

    public double getParamG () {
        return this.paramG;
    }

    public double getParamA () {
        return this.paramA;
    }

    public double getParamB () {
        return this.paramB;
    }

    public double getParamC () {
        return this.paramC;
    }

    public double getParamD () {
        return this.paramD;
    }

    public double getParamE () {
        return this.paramE;
    }

    public double getParamF () {
        return this.paramF;
    }

    public float getGamma () {
        return this.gamma;
    }

    public double interpolateLut(double inVal)
    {
        double outVal = -150;
        //ToDo: maxVal is defined as 65535 except when input is PCSXYZ, then it is 1+(32767/32768)
        //need to figure out how to handle this case.
        double maxVal = 65535;

        if (inVal >= 1.0) return 1.0;
        else if (inVal <= 0.0) return 0.0;

        else {


            //Log.d(TAG, "LUT size = " + this.getLutSize());

            for (int i = 0; i < (this.getLutSize() - 1); i++) {
                double lowLutInVal = ((double) i) / ((double) this.getLutSize());
                double hiLutInVal = ((double) (i + 1)) / ((double) this.getLutSize());


                if (inVal == lowLutInVal) {
                    outVal = ((double) this.getLutElement(i)) / maxVal;
                    break;
                } else if (inVal == hiLutInVal) {
                    outVal = ((double) this.getLutElement(i + 1)) / maxVal;
                    break;
                } else if ((inVal > lowLutInVal) && (inVal < hiLutInVal)) {
                    double lowLutOutVal = ((double) this.getLutElement(i)) / maxVal;
                    double hiLutOutVal = ((double) this.getLutElement(i + 1)) / maxVal;
                    outVal = hiLutOutVal - ((hiLutOutVal - lowLutOutVal) / (hiLutInVal - lowLutInVal) *
                            (hiLutInVal - inVal));
                    break;
                } else {
                    outVal = -160;
                }

            }
        }
        return outVal;
    }

    public double inverseInterpolateLut(double inVal)
    {
        double outVal = -250;
        //ToDo: maxVal is defined as 65535 except when input is PCSXYZ, then it is 1+(32767/32768)
        //need to figure out how to handle this case.
        double maxVal = 65535;

        if (inVal >= 1.0) return 1.0;
        else if (inVal <= 0.0) return 0.0;

        else {


            //Log.d(TAG, "LUT size = " + this.getLutSize());

            for (int i = 0; i < (this.getLutSize() - 1); i++) {
                double lowLutInVal = ( (double) this.getLutElement(i) ) / maxVal;
                double hiLutInVal = ( (double) this.getLutElement(i + 1)) / maxVal;


                if (inVal == lowLutInVal) {
                    outVal = ((double) i) / ((double)this.getLutSize());
                    break;
                } else if (inVal == hiLutInVal) {
                    outVal = ((double) (i+1)) / ((double)this.getLutSize());
                    break;
                } else if ((inVal > lowLutInVal) && (inVal < hiLutInVal)) {
                    double lowLutOutVal = ((double) i) / ((double)this.getLutSize());
                    double hiLutOutVal = ((double) (i+1)) / ((double)this.getLutSize());
                    outVal = hiLutOutVal - ((hiLutOutVal - lowLutOutVal) / (hiLutInVal - lowLutInVal) *
                            (hiLutInVal - inVal));
                    break;
                } else {
                    outVal = -260;
                }

            }
        }
        return outVal;
    }

    public double getCorrectedValue (double inVal)
    {
        double outVal = -100;
        switch (this.getType())
        {
            case TYPE_CURV_IDENTITY:
                outVal = inVal;
                break;
            case TYPE_CURV_GAMMA:
                outVal = Math.pow(inVal,(double)this.getGamma());
                break;
            case TYPE_CURV_LUT:
                outVal = interpolateLut(inVal);
                break;
            case TYPE_PARA_G:
                outVal = Math.pow(inVal,this.getParamG());
                break;
            case TYPE_PARA_GAB:
                if ( inVal >= (-1*this.getParamB()/this.getParamA()) )
                {
                    outVal = Math.pow( (inVal * this.getParamA() + this.getParamB() ),
                            this.getParamG());
                }
                else outVal = 0;
                break;
            case TYPE_PARA_GABC:
                if ( inVal >= (-1*this.getParamB()/this.getParamA()) )
                {
                    outVal = Math.pow( (inVal * this.getParamA() + this.getParamB() ),
                            this.getParamG()) + this.getParamC();
                }
                else outVal = getParamC();
                break;
            case TYPE_PARA_GABCD:
                if ( inVal >= this.getParamD() )
                {
                    outVal = Math.pow( (inVal * this.getParamA() + this.getParamB() ),
                            this.getParamG());
                }
                else outVal = getParamC() * inVal;
                break;
            case TYPE_PARA_GABCDEF:
                if ( inVal >= this.getParamD() )
                {
                    outVal = Math.pow( (inVal * this.getParamA() + this.getParamB() ),
                            this.getParamG()) + this.getParamC();
                }
                else outVal = getParamC() * inVal + getParamF();
                break;
            default:
                outVal = -100;
                break;

        }

        return outVal;


    }

    public double getInverseCorrectedValue (double inVal)
    {
        double outVal = -200;
        double checkVal;
        switch (this.getType())
        {
            case TYPE_CURV_IDENTITY:
                outVal = inVal;
                break;
            case TYPE_CURV_GAMMA:
                outVal = Math.pow(inVal,1/((double)this.getGamma()));
                break;
            case TYPE_CURV_LUT:
                outVal = inverseInterpolateLut(inVal);
                //Log.d(TAG, "Inverse Curv LUT Method not implemented");
                break;
            case TYPE_PARA_G:
                outVal = Math.pow(inVal,1/this.getParamG());
                break;
            case TYPE_PARA_GAB:
                if ( inVal > 0 )
                {
                    outVal = (Math.pow(inVal, 1/this.getParamG()) - this.getParamB()) /
                        this.getParamA();
                }
                else outVal = -this.getParamB()/this.getParamA();
                break;
            case TYPE_PARA_GABC:
                if ( inVal > this.getParamC() )
                {
                    outVal = (Math.pow( (inVal - this.getParamC()), 1/this.getParamG() )
                            - this.getParamB()) / this.getParamA();
                }
                else outVal = -this.getParamB()/this.getParamA();
                break;
            case TYPE_PARA_GABCD:
                checkVal = Math.pow(this.getParamD() * this.getParamA() + this.getParamB(),
                    this.getParamG());
                if ( inVal >= checkVal)
                {
                    outVal = ( Math.pow( inVal, 1/this.getParamG() ) - this.getParamB() ) /
                            this.getParamA();
                }
                else outVal = inVal / getParamC() ;
                break;
            case TYPE_PARA_GABCDEF:
                checkVal = Math.pow(this.getParamD() * this.getParamA() + this.getParamB(),
                        this.getParamG()) + this.getParamC();
                if ( inVal >= checkVal )
                {
                    outVal = ( Math.pow( inVal - this.getParamC(), 1/this.getParamG() ) - this.getParamB() ) /
                            this.getParamA();
                }
                else outVal = (inVal -getParamF()) /getParamC() ;
                break;
            default:
                outVal = -200;
                break;

        }

        return outVal;


    }


}
