/***************************************************************************************************
 Color Chief - AbsoluteColor Class - a set of static methods to convert between RGB, XYZ, and L*C*h
 using ICC profiles
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
import java.lang.Math;
import android.util.Log;



public class AbsoluteColor {

    private static String TAG = "Absolute Color";


    public AbsoluteColor ()
    {

    }



    public static double[] RGB2XYZ (int inColor, ICCProfile iccProfile, boolean inverseTRC)
    {
        double Rin = (double)Color.red(inColor)/(double)255;
        double Bin = (double)Color.blue(inColor)/(double)255;
        double Gin = (double)Color.green(inColor)/(double)255;

        double Rp; double Gp; double Bp;

        if (inverseTRC) {
            Rp = iccProfile.getrTRC().getInverseCorrectedValue(Rin);
            Gp = iccProfile.getgTRC().getInverseCorrectedValue(Gin);
            Bp = iccProfile.getbTRC().getInverseCorrectedValue(Bin);
        }
        else {
            Rp = iccProfile.getrTRC().getCorrectedValue(Rin);
            Gp = iccProfile.getgTRC().getCorrectedValue(Gin);
            Bp = iccProfile.getbTRC().getCorrectedValue(Bin);
        }

        double XYZ[] = new double[3];

        XYZ[0] = iccProfile.getrCIEXYZ()[0] * Rp + iccProfile.getgCIEXYZ()[0] * Gp +
                iccProfile.getbCIEXYZ()[0] * Bp;
        XYZ[1] = iccProfile.getrCIEXYZ()[1] * Rp + iccProfile.getgCIEXYZ()[1] * Gp +
                iccProfile.getbCIEXYZ()[1] * Bp;
        XYZ[2] = iccProfile.getrCIEXYZ()[2] * Rp + iccProfile.getgCIEXYZ()[2] * Gp +
                iccProfile.getbCIEXYZ()[2] * Bp;

        /*
        Log.d(TAG, "in R,G,B = " + Double.toString(Rin) + ", " + Double.toString(Gin) + ", "
                + Double.toString(Bin) + ", ");
        Log.d(TAG, "prime R,G,B = " + Double.toString(Rp) + ", " + Double.toString(Gp) + ", "
                + Double.toString(Bp) + ", ");

        Log.d(TAG, "X,Y,Z = " + Double.toString(XYZ[0]) + ", " + Double.toString(XYZ[1]) + ", "
                + Double.toString(XYZ[2]) + ", ");
        */

        return XYZ;

    }


    public static int XYZ2RGB (double XYZ[], ICCProfile iccProfile, boolean inverseTRC)
    {


        double rXYZ[] = iccProfile.getrCIEXYZ();
        double gXYZ[] = iccProfile.getgCIEXYZ();
        double bXYZ[] = iccProfile.getbCIEXYZ();


        double determinant = (rXYZ[0] * gXYZ[1] * bXYZ[2]) + (gXYZ[0] * bXYZ[1] * rXYZ[2]) +
                (bXYZ[0] * rXYZ[1] * gXYZ[2]) - (rXYZ[2] * gXYZ[1] * bXYZ[0]) -
                (gXYZ[2] * bXYZ[1] * rXYZ[0]) - (bXYZ[2] * rXYZ[1] * gXYZ[0]);



/*
        double m00 = (bXYZ[1] * gXYZ[2] - bXYZ[2] * gXYZ[1]) / determinant;
        double m01 = (gXYZ[0] * bXYZ[2] - gXYZ[2] * bXYZ[0]) / determinant;
        double m02 = (bXYZ[0] * gXYZ[1] - bXYZ[1] * gXYZ[0]) / determinant;
        double m10 = (gXYZ[1] * rXYZ[2] - gXYZ[2] * rXYZ[1]) / determinant;
        double m11 = (rXYZ[0] * gXYZ[2] - rXYZ[2] * gXYZ[0]) / determinant;
        double m12 = (gXYZ[0] * rXYZ[1] - gXYZ[1] * rXYZ[0]) / determinant;
        double m20 = (rXYZ[1] * bXYZ[2] - rXYZ[2] * bXYZ[1]) / determinant;
        double m21 = (bXYZ[0] * rXYZ[2] - bXYZ[2] * rXYZ[0]) / determinant;
        double m22 = (rXYZ[0] * bXYZ[1] - rXYZ[1] * bXYZ[0]) / determinant;
*/
        double m00 = (gXYZ[1] * bXYZ[2] - gXYZ[2] * bXYZ[1]) / determinant;
        double m01 = (gXYZ[2] * bXYZ[0] - gXYZ[0] * bXYZ[2]) / determinant;
        double m02 = (gXYZ[0] * bXYZ[1] - gXYZ[1] * bXYZ[0]) / determinant;
        double m10 = (rXYZ[2] * bXYZ[1] - rXYZ[1] * bXYZ[2]) / determinant;
        double m11 = (rXYZ[0] * bXYZ[2] - rXYZ[2] * bXYZ[0]) / determinant;
        double m12 = (rXYZ[1] * bXYZ[0] - rXYZ[0] * bXYZ[1]) / determinant;
        double m20 = (rXYZ[1] * gXYZ[2] - rXYZ[2] * gXYZ[1]) / determinant;
        double m21 = (rXYZ[2] * gXYZ[0] - rXYZ[0] * gXYZ[2]) / determinant;
        double m22 = (rXYZ[0] * gXYZ[1] - rXYZ[1] * gXYZ[0]) / determinant;


        double Rp; double Gp; double Bp;

        Rp = m00 * XYZ[0] + m01 * XYZ[1] + m02 * XYZ[2];
        Gp = m10 * XYZ[0] + m11 * XYZ[1] + m12 * XYZ[2];
        Bp = m20 * XYZ[0] + m21 * XYZ[1] + m22 * XYZ[2];

        double Rpp; double Gpp; double Bpp;

        if (inverseTRC) {
            Rpp = iccProfile.getrTRC().getInverseCorrectedValue(Rp);
            Gpp = iccProfile.getgTRC().getInverseCorrectedValue(Gp);
            Bpp = iccProfile.getbTRC().getInverseCorrectedValue(Bp);
        }
        else {
            Rpp = iccProfile.getrTRC().getCorrectedValue(Rp);
            Gpp = iccProfile.getgTRC().getCorrectedValue(Gp);
            Bpp = iccProfile.getbTRC().getCorrectedValue(Bp);
        }
        /*
        Log.d(TAG, "prime R,G,B = " + Double.toString(Rp) + ", " + Double.toString(Gp) + ", "
                + Double.toString(Bp) + ", ");
        Log.d(TAG, "double prime R,G,B = " + Double.toString(Rpp) + ", " + Double.toString(Gpp) + ", "
                + Double.toString(Bpp) + ", ");
        */
        int red = (int)Math.round(Rpp * 255.0);
        int green = (int)Math.round(Gpp * 255.0);
        int blue = (int)Math.round(Bpp * 255.0);

        if (red > 255) red = 255;
        if (red < 0) red = 0;

        if (green > 255) green = 255;
        if (green < 0) green = 0;

        if (blue > 255) blue = 255;
        if (blue < 0) blue = 0;


        return Color.argb(0xFF, red, green, blue);


    }

    public static int Lab2RGB (double Lab[], ICCProfile iccProfile, boolean inverseTRC)
    {
        return XYZ2RGB(Lab2XYZ(Lab, iccProfile), iccProfile, inverseTRC);

    }

    public static int LCh2RGB (double LCh[], ICCProfile iccProfile, boolean inverseTRC)
    {
        double Lab[] = new double[3];

        Lab[0] = LCh[0];
        Lab[1] = LCh[1] * Math.cos(LCh[2]);
        Lab[2] = LCh[1] * Math.sin(LCh[2]);
        /*
        Log.d(TAG, "L*,C*,h* = " + Double.toString(LCh[0]) + ", " + Double.toString(LCh[1]) + ", "
                + Double.toString(LCh[2]) + ", ");
        Log.d(TAG, "L*,a*,b* = " + Double.toString(Lab[0]) + ", " + Double.toString(Lab[1]) + ", "
                + Double.toString(Lab[2]) + ", ");
        */
        return XYZ2RGB(Lab2XYZ(Lab, iccProfile), iccProfile, inverseTRC);

    }

    public static double[] XYZ2Lab (double[] XYZ, ICCProfile iccProfile) {
        double Lab[] = new double[3];

        Lab[0] = 116.0 * LabFunction(XYZ[1]/iccProfile.getnCIEXYZ()[1]) - 16;
        Lab[1] = 500.0 * ( LabFunction(XYZ[0]/iccProfile.getnCIEXYZ()[0]) -
                LabFunction(XYZ[1]/iccProfile.getnCIEXYZ()[1]) );
        Lab[2] = 200.0 * ( LabFunction(XYZ[1]/iccProfile.getnCIEXYZ()[1]) -
                LabFunction(XYZ[2]/iccProfile.getnCIEXYZ()[2]) );
        /*
        Log.d(TAG, "L*,a*,b* = " + Double.toString(Lab[0]) + ", " + Double.toString(Lab[1]) + ", "
                + Double.toString(Lab[2]) + ", ");
        */
        return Lab;
    }

    public static double[] Lab2XYZ (double[] Lab, ICCProfile iccProfile) {
        double XYZ[] = new double[3];

        XYZ[0] = iccProfile.getnCIEXYZ()[0] * invLabFunction( (Lab[0] + 16.0)/116.0
                + Lab[1]/500.0 );
        XYZ[1] = iccProfile.getnCIEXYZ()[1] * invLabFunction( (Lab[0] + 16.0)/116.0 );
        XYZ[2] = iccProfile.getnCIEXYZ()[2] * invLabFunction( (Lab[0] + 16.0)/116.0
                - Lab[2]/200.0 );

        /*
        Log.d(TAG, "X,Y,Z = " + Double.toString(XYZ[0]) + ", " + Double.toString(XYZ[1]) + ", "
                + Double.toString(XYZ[2]) + ", ");
        */
        return XYZ;
    }


    public static double[] RGB2Lab (int inColor, ICCProfile iccProfile, boolean inverseTRC) {
        return XYZ2Lab(RGB2XYZ(inColor, iccProfile, inverseTRC), iccProfile);

    }

    //encode L*C*h*, where h* is in radians from 0 to 2pi
    public static double[] RGB2LCh (int inColor, ICCProfile iccProfile, boolean inverseTRC) {

        double Lab[] = XYZ2Lab(RGB2XYZ(inColor, iccProfile, inverseTRC), iccProfile);

        double LCh[] = new double[3];


        LCh[0] = Lab[0];
        //LCh[1] = Math.sqrt(Math.pow(Lab[1], 2.0) + Math.pow(Lab[2], 2.0));
        LCh[1] = Math.hypot(Lab[1], Lab[2]);
        LCh[2] = Math.atan2(Lab[2], Lab[1]);

        if (LCh[2] < 0)
        {
            LCh[2] = 2.0*Math.PI + LCh[2];
        }

        return LCh;

    }



    private static double LabFunction (double t) {
        if (t > (Math.pow(6.0/29.0, 3.0)) ) {
            return Math.pow(t, 1.0/3.0);
        }
        else {
            return ( (1.0/3.0) * Math.pow(29.0/6.0, 2.0) * t ) + 4.0/29.0;
        }
    }

    private static double invLabFunction (double t) {
        if (t > (6.0/29.0)) {
            return Math.pow(t, 3.0);
        }
        else {
            return 3.0 * Math.pow(6.0/29.0, 2.0) * (t - 4.0/29.0);
        }
    }



}

