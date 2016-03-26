/***************************************************************************************************
 Color Chief - ICCProfile Class - an Object which opens and parses ICC Profile files, and stores the
 relevant ICC tag information
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
import android.net.Uri;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.GregorianCalendar;




public class ICCProfile extends ContextWrapper {

    private final static String TAG = "ICC Profile";


    private final static int PARA_TYPE_G = 0;
    private final static int PARA_TYPE_GAB = 1;
    private final static int PARA_TYPE_GABC = 2;
    private final static int PARA_TYPE_GABCD = 3;
    private final static int PARA_TYPE_GABCDEF = 4;
    private final static String ACSP_CHECK = "acsp";


    private long profileSize;
    private long preferredCMM;
    private long profileVersion;
    private String profileDeviceClass;
    private String dataColourSpace;
    private String PCS;
    private GregorianCalendar dateTime;
    private String acsp;
    private String primaryPlatformSignature;
    private long profileFlags;
    private String deviceManufacturer;
    private String deviceModel;
    private byte[] deviceAttributes = new byte[8];
    private long renderingIntent;
    private double[] nCIEXYZ = new double[3];
    private String profileCreatorSignature;
    private byte[] profileID = new byte[16];
    private byte[] reservedBytes = new byte[28];
    private int tagCount;
    private String[] tagSignature;
    private long[] tagOffset;
    private long[] tagDataElementSize;
    private double[] rCIEXYZ = new double[3];
    private double[] gCIEXYZ = new double[3];
    private double[] bCIEXYZ = new double[3];


    private TRC rTRC = new TRC();
    private TRC gTRC = new TRC();
    private TRC bTRC = new TRC();

    private boolean validProfile = false;




    public ICCProfile(Context base) {
        super(base);
    }

    public boolean loadFromFile (Uri fileUri) {
        InputStream inputStream;
        inputStream = null;
        int inputStreamAvailableBytes = 0;

        try {
            inputStream = getContentResolver().openInputStream(fileUri);
            inputStreamAvailableBytes = inputStream.available();
            Log.d(TAG, "Estimated Available Bytes = " + inputStreamAvailableBytes);
        }
        catch (java.io.FileNotFoundException e) {

            e.printStackTrace();
            validProfile = false;
            return validProfile;
        }
        catch (java.io.IOException e) {
            e.printStackTrace();
            validProfile = false;
            return validProfile;
        }
        catch (java.lang.NullPointerException e)
        {
            e.printStackTrace();
            validProfile = false;
            return validProfile;
        }


        byte tempBuffer[] = new byte[28];
        byte smallTempBuffer[] = new byte[4];

        int offset = 0;
        int bytesRead = 0;

        try {
            BufferedInputStream buffIn = new BufferedInputStream(inputStream, inputStreamAvailableBytes);
            Log.d(TAG, "Estimated Available Bytes = " + buffIn.available());


            bytesRead = buffIn.read(smallTempBuffer, 0, 4);
            Log.d(TAG,"Bytes Read = " + bytesRead);
            offset = offset + 4;
            profileSize = unsigned4BytesToLong(smallTempBuffer);
            Log.d(TAG, "Profile Size = " + profileSize);



            bytesRead = buffIn.read(smallTempBuffer, 0, 4);
            Log.d(TAG, "Bytes Read = " + bytesRead);
            offset = offset + 4;
            preferredCMM = unsigned4BytesToLong(smallTempBuffer);
            Log.d(TAG,"Preferred CMM = " + preferredCMM);

            bytesRead = buffIn.read(smallTempBuffer, 0, 4);
            Log.d(TAG, "Bytes Read = " + bytesRead);
            offset = offset + 4;
            profileVersion = unsigned4BytesToLong(smallTempBuffer);
            Log.d(TAG,"Profile Version = " + profileVersion);

            bytesRead = buffIn.read(smallTempBuffer, 0, 4);
            Log.d(TAG, "Bytes Read = " + bytesRead);
            offset = offset + 4;
            profileDeviceClass = unsigned4BytesToString(smallTempBuffer);
            Log.d(TAG,"Profile Device Class = " + profileDeviceClass);

            bytesRead = buffIn.read(smallTempBuffer, 0, 4);
            Log.d(TAG, "Bytes Read = " + bytesRead);
            offset = offset + 4;
            dataColourSpace = unsigned4BytesToString(smallTempBuffer);
            Log.d(TAG,"Colour Space of Data = " + dataColourSpace);


            bytesRead = buffIn.read(smallTempBuffer, 0, 4);
            Log.d(TAG, "Bytes Read = " + bytesRead);
            offset = offset + 4;
            PCS = unsigned4BytesToString(smallTempBuffer);
            Log.d(TAG,"PCS = " + PCS);


            bytesRead = buffIn.read(tempBuffer, 0, 12);
            Log.d(TAG, "Bytes Read = " + bytesRead);
            offset = offset + 12;
            int year = ( ((int)tempBuffer[0] & 0xFF) << 8) | ((int)tempBuffer[1] & 0xFF);
            int month = ( ((int)tempBuffer[2] & 0xFF) << 8) | ((int)tempBuffer[3] & 0xFF);
            int day = ( ((int)tempBuffer[4] & 0xFF) << 8) | ((int)tempBuffer[5] & 0xFF);
            int hour = ( ((int)tempBuffer[6] & 0xFF) << 8) | ((int)tempBuffer[7] & 0xFF);
            int minute = ( ((int)tempBuffer[8] & 0xFF) << 8) | ((int)tempBuffer[9] & 0xFF);
            int second = ( ((int)tempBuffer[10] & 0xFF) << 8) | ((int)tempBuffer[11] & 0xFF);


            dateTime = new GregorianCalendar(year, month-1, day, hour, minute, second);
            Log.d(TAG, "Date,Time: " + DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(dateTime.getTime()));

            bytesRead = buffIn.read(smallTempBuffer, 0, 4);
            Log.d(TAG, "Bytes Read = " + bytesRead);
            offset = offset + 4;
            acsp = unsigned4BytesToString(smallTempBuffer);
            Log.d(TAG, "acsp = " + acsp);

            if (!acsp.equals(ACSP_CHECK))
            {
                validProfile = false;
                return validProfile;
            }

            bytesRead = buffIn.read(smallTempBuffer, 0, 4);
            Log.d(TAG, "Bytes Read = " + bytesRead);
            offset = offset + 4;
            primaryPlatformSignature = unsigned4BytesToString(smallTempBuffer);
            Log.d(TAG, "Primary Platform Signature = " + primaryPlatformSignature);

            bytesRead = buffIn.read(smallTempBuffer, 0, 4);
            Log.d(TAG, "Bytes Read = " + bytesRead);
            offset = offset + 4;
            profileFlags = unsigned4BytesToLong(smallTempBuffer);
            Log.d(TAG, "Profile Flags = " + Long.toHexString(profileFlags));

            bytesRead = buffIn.read(smallTempBuffer, 0, 4);
            Log.d(TAG, "Bytes Read = " + bytesRead);
            offset = offset + 4;
            deviceManufacturer = unsigned4BytesToString(smallTempBuffer);
            Log.d(TAG, "Device Manufacturer = " + deviceManufacturer);

            bytesRead = buffIn.read(smallTempBuffer, 0, 4);
            Log.d(TAG, "Bytes Read = " + bytesRead);
            offset = offset + 4;
            deviceModel = unsigned4BytesToString(smallTempBuffer);
            Log.d(TAG, "Device Model = " + deviceModel);

            bytesRead = buffIn.read(deviceAttributes, 0, 8);
            Log.d(TAG, "Bytes Read = " + bytesRead);
            offset = offset + 8;
            Log.d(TAG,"Device Attributes = " + deviceAttributes);

            bytesRead = buffIn.read(smallTempBuffer, 0, 4);
            Log.d(TAG, "Bytes Read = " + bytesRead);
            offset = offset + 4;
            renderingIntent = unsigned4BytesToLong(smallTempBuffer);
            Log.d(TAG, "Rendering Intent = " + renderingIntent);

            bytesRead = buffIn.read(smallTempBuffer, 0, 4);
            Log.d(TAG, "Bytes Read = " + bytesRead);
            offset = offset + 4;
            nCIEXYZ[0] =  signed4BytesToDouble(smallTempBuffer);
            Log.d(TAG, "nX Bytes = " + Integer.toHexString((int)smallTempBuffer[0]&0xFF) + "\n" +
                Integer.toHexString((int)smallTempBuffer[1]&0xFF) + "\n" +
                Integer.toHexString((int)smallTempBuffer[2]&0xFF) + "\n" +
                Integer.toHexString((int)smallTempBuffer[3]&0xFF));

            bytesRead = buffIn.read(smallTempBuffer, 0, 4);
            Log.d(TAG, "Bytes Read = " + bytesRead);
            offset = offset + 4;
            nCIEXYZ[1] =  signed4BytesToDouble(smallTempBuffer);
            Log.d(TAG, "nY Bytes = " + Integer.toHexString((int)smallTempBuffer[0]&0xFF) + "\n" +
                    Integer.toHexString((int)smallTempBuffer[1]&0xFF) + "\n" +
                    Integer.toHexString((int)smallTempBuffer[2]&0xFF) + "\n" +
                    Integer.toHexString((int)smallTempBuffer[3]&0xFF));

            bytesRead = buffIn.read(smallTempBuffer, 0, 4);
            Log.d(TAG, "Bytes Read = " + bytesRead);
            offset = offset + 4;
            nCIEXYZ[2] =  signed4BytesToDouble(smallTempBuffer);
            Log.d(TAG, "nZ Bytes = " + Integer.toHexString((int)smallTempBuffer[0]&0xFF) + "\n" +
                    Integer.toHexString((int)smallTempBuffer[1]&0xFF) + "\n" +
                    Integer.toHexString((int)smallTempBuffer[2]&0xFF) + "\n" +
                    Integer.toHexString((int)smallTempBuffer[3]&0xFF));

            Log.d(TAG,"n X,Y,Z = " + nCIEXYZ[0] + ", " + nCIEXYZ[1] + ", " + nCIEXYZ[2]);

            bytesRead = buffIn.read(smallTempBuffer, 0, 4);
            Log.d(TAG, "Bytes Read = " + bytesRead);
            offset = offset + 4;
            profileCreatorSignature = unsigned4BytesToString(smallTempBuffer);
            Log.d(TAG, "Profile Creator Signature = " + profileCreatorSignature);

            bytesRead = buffIn.read(profileID, 0, 16);
            Log.d(TAG, "Bytes Read = " + bytesRead);
            offset = offset + 16;
            Log.d(TAG,"Profile ID = " + profileID);

            bytesRead = buffIn.read(reservedBytes, 0, 28);
            Log.d(TAG, "Bytes Read = " + bytesRead);
            offset = offset + 28;
            Log.d(TAG,"Reserved Bytes = " + reservedBytes);

            bytesRead = buffIn.read(smallTempBuffer, 0, 4);
            Log.d(TAG,"Bytes Read = " + bytesRead);
            offset = offset + 4;
            tagCount = (int)unsigned4BytesToLong(smallTempBuffer);
            Log.d(TAG, "Tag Count = " + Integer.toString(tagCount));

            if (tagCount < 1) {
                validProfile = false;
                return validProfile;
            }

            tagSignature = new String[tagCount];
            tagOffset = new long[tagCount];
            tagDataElementSize = new long[tagCount];

            for (int i=0; i<tagCount; i++) {
                Log.d(TAG, "TAG #" + Integer.toString(i));

                bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                Log.d(TAG,"Bytes Read = " + bytesRead);
                offset = offset + 4;
                tagSignature[i] = unsigned4BytesToString(smallTempBuffer);
                Log.d(TAG, "Tag Signature = " + tagSignature[i]);

                bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                Log.d(TAG,"Bytes Read = " + bytesRead);
                offset = offset + 4;
                tagOffset[i] = unsigned4BytesToLong(smallTempBuffer);
                Log.d(TAG, "Tag Offset = " + Long.toString(tagOffset[i]));

                bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                Log.d(TAG,"Bytes Read = " + bytesRead);
                offset = offset + 4;
                tagDataElementSize[i] = unsigned4BytesToLong(smallTempBuffer);
                Log.d(TAG, "Tag Data Element Size = " + Long.toString(tagDataElementSize[i]));
            }

            //set a marker here where the tag data begins
            int tagDataStartPosition = offset;
            buffIn.mark(inputStreamAvailableBytes - tagDataStartPosition);
            //buffIn.reset();


            //search tag signatures for rXYZ, gXYZ, and bXYZ values
            for (int i=0;i<tagCount; i++)
            {


                if (tagSignature[i].contains("rXYZ"))
                {
                    Log.d(TAG, "Tag Signature = " + tagSignature[i]);

                    buffIn.skip(tagOffset[i]-tagDataStartPosition);

                    bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                    offset = offset + 4;

                    String rXYZSignature = unsigned4BytesToString(smallTempBuffer);
                    Log.d(TAG, "XYZ Signature = " + rXYZSignature);

                    //reserved Bytes
                    bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                    offset = offset + 4;

                    //XYZnumber
                    bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                    offset = offset + 4;
                    rCIEXYZ[0] = signed4BytesToDouble(smallTempBuffer);

                    bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                    offset = offset + 4;
                    rCIEXYZ[1] = signed4BytesToDouble(smallTempBuffer);

                    bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                    offset = offset + 4;
                    rCIEXYZ[2] = signed4BytesToDouble(smallTempBuffer);


                    Log.d(TAG, "red X,Y,Z = " + Double.toString(rCIEXYZ[0]) + ", " +
                        Double.toString(rCIEXYZ[1]) + ", " + Double.toString(rCIEXYZ[2]));

                    buffIn.reset();
                }

                if (tagSignature[i].contains("gXYZ"))
                {
                    Log.d(TAG, "Tag Signature = " + tagSignature[i]);

                    buffIn.skip(tagOffset[i]-tagDataStartPosition);

                    bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                    offset = offset + 4;

                    String gXYZSignature = unsigned4BytesToString(smallTempBuffer);
                    Log.d(TAG, "XYZ Signature = " + gXYZSignature);

                    //reserved Bytes
                    bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                    offset = offset + 4;

                    //XYZnumber
                    bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                    offset = offset + 4;
                    gCIEXYZ[0] = signed4BytesToDouble(smallTempBuffer);

                    bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                    offset = offset + 4;
                    gCIEXYZ[1] = signed4BytesToDouble(smallTempBuffer);

                    bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                    offset = offset + 4;
                    gCIEXYZ[2] = signed4BytesToDouble(smallTempBuffer);


                    Log.d(TAG, "green X,Y,Z = " + Double.toString(gCIEXYZ[0]) + ", " +
                            Double.toString(gCIEXYZ[1]) + ", " + Double.toString(gCIEXYZ[2]));

                    buffIn.reset();
                }

                if (tagSignature[i].contains("bXYZ"))
                {
                    Log.d(TAG, "Tag Signature = " + tagSignature[i]);

                    buffIn.skip(tagOffset[i]-tagDataStartPosition);

                    bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                    offset = offset + 4;

                    String bXYZSignature = unsigned4BytesToString(smallTempBuffer);
                    Log.d(TAG, "XYZ Signature = " + bXYZSignature);

                    //reserved Bytes
                    bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                    offset = offset + 4;

                    //XYZnumber
                    bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                    offset = offset + 4;
                    bCIEXYZ[0] = signed4BytesToDouble(smallTempBuffer);

                    bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                    offset = offset + 4;
                    bCIEXYZ[1] = signed4BytesToDouble(smallTempBuffer);

                    bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                    offset = offset + 4;
                    bCIEXYZ[2] = signed4BytesToDouble(smallTempBuffer);


                    Log.d(TAG, "blue X,Y,Z = " + Double.toString(bCIEXYZ[0]) + ", " +
                            Double.toString(bCIEXYZ[1]) + ", " + Double.toString(bCIEXYZ[2]));

                    buffIn.reset();
                }

                // Red Tonal Reproduction Curve (TRC)
                if (tagSignature[i].contains("rTRC"))
                {
                    Log.d(TAG, "Tag Signature = " + tagSignature[i]);

                    buffIn.skip(tagOffset[i]-tagDataStartPosition);

                    bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                    offset = offset + 4;
                    String rTRCtypeSignature = unsigned4BytesToString(smallTempBuffer);

                    Log.d(TAG, "Type Signature = " + rTRCtypeSignature);

                    if (rTRCtypeSignature.contains("para"))
                    {
                        buffIn.skip(4);

                        byte buffer2Byte[] = new byte[2];
                        bytesRead = buffIn.read(buffer2Byte, 0, 2);
                        offset = offset + 2;

                        int functionType = unsigned2BytesToInt(buffer2Byte);

                        Log.d(TAG, "functionType = " + Integer.toHexString(functionType));

                        switch (functionType)
                        {
                            case PARA_TYPE_G:
                                rTRC.setType(TRC.TYPE_PARA_G);
                                bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                                offset = offset + 4;
                                rTRC.setParamG(signed4BytesToDouble(smallTempBuffer));

                            case PARA_TYPE_GAB:
                                rTRC.setType(TRC.TYPE_PARA_GAB);
                                bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                                offset = offset + 4;
                                rTRC.setParamA(signed4BytesToDouble(smallTempBuffer));
                                bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                                offset = offset + 4;
                                rTRC.setParamB(signed4BytesToDouble(smallTempBuffer));

                            case PARA_TYPE_GABC:
                                rTRC.setType(TRC.TYPE_PARA_GABC);
                                bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                                offset = offset + 4;
                                rTRC.setParamC(signed4BytesToDouble(smallTempBuffer));

                            case PARA_TYPE_GABCD:
                                rTRC.setType(TRC.TYPE_PARA_GABCD);
                                bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                                offset = offset + 4;
                                rTRC.setParamD(signed4BytesToDouble(smallTempBuffer));

                            case PARA_TYPE_GABCDEF:
                                rTRC.setType(TRC.TYPE_PARA_GABCDEF);
                                bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                                offset = offset + 4;
                                rTRC.setParamE(signed4BytesToDouble(smallTempBuffer));
                                bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                                offset = offset + 4;
                                rTRC.setParamF(signed4BytesToDouble(smallTempBuffer));
                                break;

                            default:
                                break;
                        }
                    }

                    if (rTRCtypeSignature.contains("curv"))
                    {
                        buffIn.skip(4);

                        bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                        offset = offset + 4;
                        int curveTypeCount = (int) unsigned4BytesToLong(smallTempBuffer);

                        //identity response is assumed when count value = 0
                        if (curveTypeCount == 0)
                        {
                            rTRC.setType(TRC.TYPE_CURV_IDENTITY);
                            rTRC.setGamma(1);
                            Log.d(TAG, "Gamma = " + Float.toString(rTRC.getGamma()));
                        }

                        /* when count value = 1, the curve is interpreted as a gamma value where
                        ** y = x ^ gamma
                        */
                        if (curveTypeCount == 1)
                        {
                            rTRC.setType(TRC.TYPE_CURV_GAMMA);
                            byte buffer2Byte[] = new byte[2];
                            bytesRead = buffIn.read(buffer2Byte, 0, 2);
                            offset = offset + 2;
                            rTRC.setGamma(unsigned2BytesToFloat(buffer2Byte));
                            Log.d(TAG, "Gamma = " + Float.toString(rTRC.getGamma()));
                        }

                        if (curveTypeCount > 1)
                        {
                            rTRC.setType(TRC.TYPE_CURV_LUT);
                            for (int j = 0; j < curveTypeCount; j++) {
                                byte buffer2Byte[] = new byte[2];
                                bytesRead = buffIn.read(buffer2Byte, 0, 2);
                                offset = offset + 2;
                                rTRC.addLutElement(unsigned2BytesToInt(buffer2Byte));
                                Log.d(TAG, "Gamma LUT index " + Integer.toString(j) + " = " +
                                        Integer.toString(rTRC.getLutElement(j)));
                            }
                        }

                    }

                    buffIn.reset();
                }


                if (tagSignature[i].contains("gTRC"))
                {
                    Log.d(TAG, "Tag Signature = " + tagSignature[i]);

                    buffIn.skip(tagOffset[i]-tagDataStartPosition);

                    bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                    offset = offset + 4;
                    String gTRCtypeSignature = unsigned4BytesToString(smallTempBuffer);

                    Log.d(TAG, "Type Signature = " + gTRCtypeSignature);

                    if (gTRCtypeSignature.contains("para"))
                    {
                        buffIn.skip(4);

                        byte buffer2Byte[] = new byte[2];
                        bytesRead = buffIn.read(buffer2Byte, 0, 2);
                        offset = offset + 2;

                        int functionType = unsigned2BytesToInt(buffer2Byte);

                        Log.d(TAG, "functionType = " + Integer.toHexString(functionType));

                        switch (functionType)
                        {
                            case PARA_TYPE_G:
                                gTRC.setType(TRC.TYPE_PARA_G);
                                bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                                offset = offset + 4;
                                gTRC.setParamG(signed4BytesToDouble(smallTempBuffer));

                            case PARA_TYPE_GAB:
                                gTRC.setType(TRC.TYPE_PARA_GAB);
                                bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                                offset = offset + 4;
                                gTRC.setParamA(signed4BytesToDouble(smallTempBuffer));
                                bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                                offset = offset + 4;
                                gTRC.setParamB(signed4BytesToDouble(smallTempBuffer));

                            case PARA_TYPE_GABC:
                                gTRC.setType(TRC.TYPE_PARA_GABC);
                                bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                                offset = offset + 4;
                                gTRC.setParamC(signed4BytesToDouble(smallTempBuffer));

                            case PARA_TYPE_GABCD:
                                gTRC.setType(TRC.TYPE_PARA_GABCD);
                                bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                                offset = offset + 4;
                                gTRC.setParamD(signed4BytesToDouble(smallTempBuffer));

                            case PARA_TYPE_GABCDEF:
                                gTRC.setType(TRC.TYPE_PARA_GABCDEF);
                                bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                                offset = offset + 4;
                                gTRC.setParamE(signed4BytesToDouble(smallTempBuffer));
                                bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                                offset = offset + 4;
                                gTRC.setParamF(signed4BytesToDouble(smallTempBuffer));
                                break;

                            default:
                                break;
                        }
                    }

                    if (gTRCtypeSignature.contains("curv"))
                    {
                        buffIn.skip(4);

                        bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                        offset = offset + 4;
                        int curveTypeCount = (int) unsigned4BytesToLong(smallTempBuffer);

                        //identity response is assumed when count value = 0
                        if (curveTypeCount == 0)
                        {
                            gTRC.setType(TRC.TYPE_CURV_IDENTITY);
                            gTRC.setGamma(1);
                            Log.d(TAG, "Gamma = " + Float.toString(gTRC.getGamma()));
                        }

                        /* when count value = 1, the curve is interpreted as a gamma value where
                        ** y = x ^ gamma
                        */
                        if (curveTypeCount == 1)
                        {
                            gTRC.setType(TRC.TYPE_CURV_GAMMA);
                            byte buffer2Byte[] = new byte[2];
                            bytesRead = buffIn.read(buffer2Byte, 0, 2);
                            offset = offset + 2;
                            gTRC.setGamma(unsigned2BytesToFloat(buffer2Byte));
                            Log.d(TAG, "Gamma = " + Float.toString(gTRC.getGamma()));
                        }

                        if (curveTypeCount > 1)
                        {
                            gTRC.setType(TRC.TYPE_CURV_LUT);
                            for (int j = 0; j < curveTypeCount; j++) {
                                byte buffer2Byte[] = new byte[2];
                                bytesRead = buffIn.read(buffer2Byte, 0, 2);
                                offset = offset + 2;
                                gTRC.addLutElement(unsigned2BytesToInt(buffer2Byte));
                                Log.d(TAG, "Gamma LUT index " + Integer.toString(j) + " = " +
                                        Integer.toString(gTRC.getLutElement(j)));
                            }
                        }

                    }

                    buffIn.reset();
                }

                if (tagSignature[i].contains("bTRC"))
                {
                    Log.d(TAG, "Tag Signature = " + tagSignature[i]);

                    buffIn.skip(tagOffset[i]-tagDataStartPosition);

                    bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                    offset = offset + 4;
                    String bTRCtypeSignature = unsigned4BytesToString(smallTempBuffer);

                    Log.d(TAG, "Type Signature = " + bTRCtypeSignature);

                    if (bTRCtypeSignature.contains("para"))
                    {
                        buffIn.skip(4);

                        byte buffer2Byte[] = new byte[2];
                        bytesRead = buffIn.read(buffer2Byte, 0, 2);
                        offset = offset + 2;

                        int functionType = unsigned2BytesToInt(buffer2Byte);

                        Log.d(TAG, "functionType = " + Integer.toHexString(functionType));

                        switch (functionType)
                        {
                            case PARA_TYPE_G:
                                bTRC.setType(TRC.TYPE_PARA_G);
                                bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                                offset = offset + 4;
                                bTRC.setParamG(signed4BytesToDouble(smallTempBuffer));

                            case PARA_TYPE_GAB:
                                bTRC.setType(TRC.TYPE_PARA_GAB);
                                bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                                offset = offset + 4;
                                bTRC.setParamA(signed4BytesToDouble(smallTempBuffer));
                                bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                                offset = offset + 4;
                                bTRC.setParamB(signed4BytesToDouble(smallTempBuffer));

                            case PARA_TYPE_GABC:
                                bTRC.setType(TRC.TYPE_PARA_GABC);
                                bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                                offset = offset + 4;
                                bTRC.setParamC(signed4BytesToDouble(smallTempBuffer));

                            case PARA_TYPE_GABCD:
                                bTRC.setType(TRC.TYPE_PARA_GABCD);
                                bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                                offset = offset + 4;
                                bTRC.setParamD(signed4BytesToDouble(smallTempBuffer));

                            case PARA_TYPE_GABCDEF:
                                bTRC.setType(TRC.TYPE_PARA_GABCDEF);
                                bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                                offset = offset + 4;
                                bTRC.setParamE(signed4BytesToDouble(smallTempBuffer));
                                bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                                offset = offset + 4;
                                bTRC.setParamF(signed4BytesToDouble(smallTempBuffer));
                                break;

                            default:
                                break;
                        }
                    }

                    if (bTRCtypeSignature.contains("curv"))
                    {
                        buffIn.skip(4);

                        bytesRead = buffIn.read(smallTempBuffer, 0, 4);
                        offset = offset + 4;
                        int curveTypeCount = (int) unsigned4BytesToLong(smallTempBuffer);

                        //identity response is assumed when count value = 0
                        if (curveTypeCount == 0)
                        {
                            bTRC.setType(TRC.TYPE_CURV_IDENTITY);
                            bTRC.setGamma(1);
                            Log.d(TAG, "Gamma = " + Float.toString(bTRC.getGamma()));
                        }

                        /* when count value = 1, the curve is interpreted as a gamma value where
                        ** y = x ^ gamma
                        */
                        if (curveTypeCount == 1)
                        {
                            bTRC.setType(TRC.TYPE_CURV_GAMMA);
                            byte buffer2Byte[] = new byte[2];
                            bytesRead = buffIn.read(buffer2Byte, 0, 2);
                            offset = offset + 2;
                            bTRC.setGamma(unsigned2BytesToFloat(buffer2Byte));
                            Log.d(TAG, "Gamma = " + Float.toString(bTRC.getGamma()));
                        }

                        if (curveTypeCount > 1)
                        {
                            bTRC.setType(TRC.TYPE_CURV_LUT);
                            for (int j = 0; j < curveTypeCount; j++) {
                                byte buffer2Byte[] = new byte[2];
                                bytesRead = buffIn.read(buffer2Byte, 0, 2);
                                offset = offset + 2;
                                bTRC.addLutElement(unsigned2BytesToInt(buffer2Byte));
                                Log.d(TAG, "Gamma LUT index " + Integer.toString(j) + " = " +
                                        Integer.toString(bTRC.getLutElement(j)));
                            }
                        }

                    }

                    buffIn.reset();
                }



            }



        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            validProfile = false;
            return validProfile;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            validProfile = false;
            return validProfile;
        }

        validProfile = true;

        return validProfile;
    }

    //Assumes little Endian & unsigned
    private long unsigned4BytesToLong(byte[] inBytes) {
        return ( ((long)inBytes[0] & 0x00000000000000FF) << 24) |
                ( ((long)inBytes[1] & 0x00000000000000FF)<< 16) |
                ( ((long)inBytes[2] & 0x00000000000000FF)<< 8) |
                ( ((long)inBytes[3] & 0x00000000000000FF));
    }

    //Assumes little Endian & unsigned
    private int unsigned2BytesToInt(byte[] inBytes) {
        return  (((int)inBytes[0] & 0x000000FF)<< 8) |
                ( ((int)inBytes[1] & 0x000000FF));
    }

    //Assumes little Endian
    private String unsigned4BytesToString(byte[] inBytes) {
        /*
        return Byte.toString(inBytes[0]) + Byte.toString(inBytes[1]) +
                Byte.toString(inBytes[2]) + Byte.toString(inBytes[3]);
        */
        //return new StringBuilder((char)inBytes[0]).append((char)inBytes[1]).append((char)inBytes[2]).append((char)inBytes[3]).toString();
        return Character.toString((char)inBytes[0]) + Character.toString((char)inBytes[1]) +
                Character.toString((char)inBytes[2]) + Character.toString((char)inBytes[3]);

    }

    private double signed4BytesToDouble(byte[] inBytes){
        double result;
        long resultL;

        resultL = ((((long)inBytes[0] & 0xFF) << 24) | (((long)inBytes[1] & 0xFF) << 16) |
                (((long)inBytes[2] & 0xFF) << 8) | ((long)inBytes[3] & 0xFF) );

        if ( (inBytes[0] & 0x80) == 0x80)
        {
            resultL = resultL - (2L^32L);
        }

        Log.d(TAG,"resultL = " + Long.toString(resultL));

        result = ((double)resultL)/((double)65536L);

        return result;
    }

    private float unsigned2BytesToFloat(byte[] inBytes){
        float result;
        long resultL;

        resultL = ( (((long)inBytes[0] & 0xFF) << 8) | ((long)inBytes[1] & 0xFF) );


        Log.d(TAG,"resultL = " + Long.toString(resultL));

        result = ((float)resultL)/((float)256L);

        return result;
    }


    public TRC getrTRC () {
        return rTRC;
    }

    public TRC getgTRC () {
        return gTRC;
    }

    public TRC getbTRC () {
        return bTRC;
    }

    public double[] getrCIEXYZ () {
        return rCIEXYZ;
    }

    public double[] getgCIEXYZ () {
        return gCIEXYZ;
    }

    public double[] getbCIEXYZ () {
        return bCIEXYZ;
    }

    public double[] getnCIEXYZ () {
        return nCIEXYZ;
    }

    public boolean isValidProfile () {
        return validProfile;
    }

}

