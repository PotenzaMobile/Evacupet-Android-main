package com.evacupet.utility;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.SendCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class BaseUtility {
    private int YOUR_SELECT_PICTURE_REQUEST_CODE = 101;
    private int ZXING_CAMERA_PERMISSION = 102;

    public String getPath(Context context, Uri uri) {
        String[] data = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(context, uri, data, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public Uri checkAndRequestPermissions(Activity context) {
        Uri outputImage = null;
        int camera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        int read = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        int write = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (camera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (read != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (write != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(context, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), ZXING_CAMERA_PERMISSION);
            return outputImage;
        } else {
            outputImage = openImageIntent(context);
        }
        return outputImage;
    }

    public Uri openImageIntent(Activity context) {
        Uri outputFileUri;
        //final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "DriverLocation" + File.separator);
        final File root = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + "DriverLocation" + File.separator);

        root.mkdirs();
        final String fname = "img_" + System.currentTimeMillis() + ".jpg";
        final File sdImageMainDirectory = new File(root, fname);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);
        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        final Intent chooserIntent = Intent.createChooser(photoPickerIntent, "Select Source");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));
        context.startActivityForResult(chooserIntent, YOUR_SELECT_PICTURE_REQUEST_CODE);
        return outputFileUri;
    }

    public void showDialogOK(Context context, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    public byte[] readInFile(String path) throws IOException {
        // TODO Auto-generated method stub
        byte[] data = null;
        File file = new File(path);
        InputStream input_stream = new BufferedInputStream(new FileInputStream(
                file));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        data = new byte[16384]; // 16K
        int bytes_read;
        while ((bytes_read = input_stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytes_read);
        }
        input_stream.close();
        return buffer.toByteArray();

    }

    public Date convertDate(String bDate) {
        Date date = null;
        //DateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
        DateFormat inputFormat = new SimpleDateFormat("MM-dd-yyyy");
        try {
            date = inputFormat.parse(bDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public String convertDateToString(Date bDate) {
        String date = "";
        //DateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
        DateFormat inputFormat = new SimpleDateFormat("MM-dd-yyyy");
        date = inputFormat.format(bDate);
        return date;
    }

    public static String currentDateTime(){
        String currentDate = "";
        Date date= new Date();
       // date.getTime();
        currentDate = String.valueOf(date.getTime());
        return currentDate;

    }

    public JSONObject parseObjectToJson(ParseObject parseObject) throws com.parse.ParseException, JSONException {
        JSONObject jsonObject = new JSONObject();
        parseObject.fetchIfNeeded();
        Set<String> keys = parseObject.keySet();
        for (String key : keys) {
            Object objectValue = parseObject.get(key);
            if (objectValue instanceof ParseObject) {
                jsonObject.put(key, parseObjectToJson(parseObject.getParseObject(key)));
                // keep in mind about "pointer" to it self, will gain stackoverlow
            } else if (objectValue instanceof ParseRelation) {
                // handle relation
            } else {
                jsonObject.put(key, objectValue.toString());
            }
        }
        return jsonObject;
    }

    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                            Log.e("isAppIsInBackground = ", isInBackground + "");
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
                Log.e("isAppIsInBackground == ", isInBackground + "");
            }
        }

        return isInBackground;
    }

    public void sendNotification(String id, String msg) {
        JSONObject data = new JSONObject();
// Put data in the JSON object
        try {
            data.put("alert", "EvacuPet");
            data.put("title", msg);
//            data.put("badge", "Increment");

        } catch (JSONException e) {
            // should not happen
            throw new IllegalArgumentException("unexpected parsing error", e);
        }
// Configure the push
        ParseQuery query = ParseInstallation.getQuery();
        query.whereEqualTo("userID", id);
        ParsePush push = new ParsePush();
        push.setQuery(query);
        push.setMessage("this is chat message");
        push.setData(data);
        push.sendInBackground(new SendCallback() {
            @Override
            public void done(com.parse.ParseException e) {
                if (e == null) {
                    Log.e("send", "Push notification to sent successfully");
                } else {
                    Log.e("notification error = ", "Private chat push notification sending error:" + e.getMessage());
                }
            }
        });
    }
}