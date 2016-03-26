/***************************************************************************************************
 Color Chief - ChromaScaleValueLUT Class - an LUT object reference h* values to C* values and C*
 scaling factors
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

import java.util.ArrayList;
import java.util.List;


public class ChromaScaleValueLUT {



    private List<Double> hueTable = new ArrayList<Double>();
    private List<Double> cScaleTable = new ArrayList<Double>();
    private List<Double> chromaTable = new ArrayList<Double>();


    public ChromaScaleValueLUT ()
    {

    }

    public void addElement(double hue, double cScale, double chroma)
    {
        if (hueTable.size() < 1)
        {
            hueTable.add(hue);
            cScaleTable.add(cScale);
            chromaTable.add(chroma);
        }
        else if (hueTable.size() == 1)
        {
            if (hue < hueTable.get(0))
            {
                hueTable.add(0, hue);
                cScaleTable.add(0, cScale);
                chromaTable.add(0, chroma);
            }
            else
            {
                hueTable.add(hue);
                cScaleTable.add(cScale);
                chromaTable.add(chroma);
            }

        }
        else
        {
            if (hue >= hueTable.get(hueTable.size()-1) )
            {
                hueTable.add(hue);
                cScaleTable.add(cScale);
                chromaTable.add(chroma);
            }
            else {
                for (int i = 0; i < (hueTable.size() - 1); i++) {
                    if ((hue >= hueTable.get(i)) && (hue < hueTable.get(i + 1))) {
                        hueTable.add(i + 1, hue);
                        cScaleTable.add(i + 1, cScale);
                        chromaTable.add(i + 1, chroma);
                        break;
                    }
                }
            }
        }


    }

    public double lookupScale (double hue)
    {



        if (hueTable.size() < 1)
        {
            return 0.0;
        }
        else if (hueTable.size() == 1)
        {
            return cScaleTable.get(0);

        }
        // need to interpolate between the last element and first element in the table
        // i.e last element becomes the "lower" value, first element becomes the "upper" value
        else if (hue >= hueTable.get(hueTable.size()-1))
        {
            double weightUpper = Math.abs(hueTable.get(0) + 2*Math.PI - hue) /
                    Math.abs(hueTable.get(0) + 2*Math.PI - hueTable.get(hueTable.size()-1));
            double weightLower = Math.abs(hue - hueTable.get(hueTable.size()-1)) /
                    Math.abs(hueTable.get(0) + 2*Math.PI - hueTable.get(hueTable.size()-1));
            return weightLower * cScaleTable.get(hueTable.size()-1) +
                    weightUpper * cScaleTable.get(0);
        }
        else {

            for (int i = 0; i < (hueTable.size() - 2); i++) {
                if ((hue >= hueTable.get(i)) && (hue < hueTable.get(i + 1))) {
                    double weightUpper = Math.abs(hueTable.get(i+1)-hue) /
                            Math.abs(hueTable.get(i+1)-hueTable.get(i));
                    double weightLower = Math.abs(hue-hueTable.get(i)) /
                            Math.abs(hueTable.get(i+1)-hueTable.get(i));

                    return weightLower * cScaleTable.get(i) + weightUpper * cScaleTable.get(i+1);
                }
            }
        }

        return 0.0;
    }

    public double lookupChroma (double hue)
    {


        if (hueTable.size() < 1)
        {
            return 0.0;
        }
        else if (hueTable.size() == 1)
        {
            return chromaTable.get(0);

        }
        // need to interpolate between the last element and first element in the table
        // i.e last element becomes the "lower" value, first element becomes the "upper" value
        else if (hue >= hueTable.get(hueTable.size()-1))
        {
            double weightUpper = Math.abs(hueTable.get(0) + 2*Math.PI - hue) /
                    Math.abs(hueTable.get(0) + 2*Math.PI - hueTable.get(hueTable.size()-1));
            double weightLower = Math.abs(hue - hueTable.get(hueTable.size()-1)) /
                    Math.abs(hueTable.get(0) + 2*Math.PI - hueTable.get(hueTable.size()-1));
            return weightLower * chromaTable.get(hueTable.size()-1) +
                    weightUpper * chromaTable.get(0);
        }
        else {

            for (int i = 0; i < (hueTable.size() - 2); i++) {
                if ((hue >= hueTable.get(i)) && (hue < hueTable.get(i + 1))) {
                    double weightUpper = Math.abs(hueTable.get(i+1)-hue) /
                            Math.abs(hueTable.get(i+1)-hueTable.get(i));
                    double weightLower = Math.abs(hue-hueTable.get(i)) /
                            Math.abs(hueTable.get(i+1)-hueTable.get(i));

                    return weightLower * chromaTable.get(i) + weightUpper * chromaTable.get(i+1);
                }
            }
        }

        return 0.0;
    }



}
