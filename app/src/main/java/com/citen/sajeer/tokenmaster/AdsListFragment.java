package com.citen.sajeer.tokenmaster;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.citen.sajeer.tokenmaster.helper.SimpleItemTouchHelperCallback;
import com.github.lzyzsd.circleprogress.DonutProgress;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class AdsListFragment extends Fragment implements AdSpaceRecyclerListAdapter.OnStartDragListener, OnListChangeToDbListner {

    protected MenuItem mSendMenuItem;
    CoordinatorLayout coordinatorLayout;
    View v;

    private String SERVER_URL = "http://192.168.0.108:3000/upload";

    private static final String TAG = "AdsListFragment";
    final static String KEY_POSITION = "position";
    private int PICK_FILE_REQUEST = 1;
    int currentAdSpace = 0;

    DbHelper dbHelper;

    ArrayList<AdData> adList;
    AdSpaceRecyclerListAdapter adapter;
    ItemTouchHelper mItemTouchHelper;

    int totalSize = 0;

    public AdsListFragment() {
        // Required empty public constructor
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_ads_list, container, false);
        coordinatorLayout = (CoordinatorLayout) v.findViewById(R.id.main_content);

        FloatingActionButton addNewAds = (FloatingActionButton) v.findViewById(R.id.addNewAds);
        addNewAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                if((currentAdSpace == 0 || currentAdSpace == 1) && CheckStoragePermission()){
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_FILE_REQUEST);
                }else if((currentAdSpace == 2 || currentAdSpace == 3 ) && CheckStoragePermission()){
                    intent.setType("video/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_FILE_REQUEST);
                }else {
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                    final View mView = getActivity().getLayoutInflater().inflate(R.layout.scroll_text_dialog, null);

                    Button cancelButton = (Button) mView.findViewById(R.id.button_cancel);
                    Button addButton = (Button) mView.findViewById(R.id.button_add);

                    mBuilder.setView(mView);
                    final AlertDialog dialog = mBuilder.create();
                    if(dialog.getWindow() != null)
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    addButton.setOnClickListener(new View.OnClickListener(){

                        @Override
                        public void onClick(View v) {

                            AdData adData = new AdData();

                            EditText textAdTitleView = (EditText) mView.findViewById(R.id.text_ad_title_value);
                            EditText textAdContentView = (EditText) mView.findViewById(R.id.text_ad_datail_value);

                            String textAdTitle = textAdTitleView.getText().toString().trim();
                            String textAdContent = textAdContentView.getText().toString().trim();

                            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

                            if(textAdContent.isEmpty() || textAdTitle.isEmpty()){
                                Snackbar.make(coordinatorLayout,"VATUES CANNOT BE EMPTY", Snackbar.LENGTH_LONG).show();
                            }else{
                                adData.setFileName(textAdContent);
                                adData.setDisplayName(textAdTitle);
                                adData.setAdSpaceId(currentAdSpace);
                                adData.setDirectoryPath(sharedPref.getString("LETTERTPATH", "Not saved yet"));
                                adapter.appendToAdList(adData);
                                dbHelper.insertAd(adData, adList.size());
                                dialog.dismiss();

                            }
                        }
                    });


                    cancelButton.setOnClickListener(new View.OnClickListener(){

                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();

                    WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                    lp.dimAmount=0.2f; // Dim level. 0.0 - no dim, 1.0 - completely opaque
                    dialog.getWindow().setAttributes(lp);
                }
            }
        });

        return v;

    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean CheckStoragePermission() {
        int permissionCheckRead = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheckRead != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) getContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions((Activity) getContext(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            return false;
        } else
            return true;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)  {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = DbHelper.getInstance(getActivity().getApplicationContext());
        adList = dbHelper.getAdList(currentAdSpace);

        adapter = new AdSpaceRecyclerListAdapter(this, this, adList, coordinatorLayout);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.ad_list_recycler_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu, menu);
        mSendMenuItem = menu.getItem(0);
        mSendMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                dbHelper.updateAdListStatus(adapter.getAdListItems(), currentAdSpace);
                new SendPostRequest(currentAdSpace, getAdListFileNames()).execute("192.168.0.108:8000");
                return true;
            }
        });
    }

    public ArrayList<String> getAdListFileNames(){

        List<AdData> adData  =  adapter.getAdListItems();
        ArrayList<String> fileNames = new ArrayList<>();

        for(int i=0; i<adData.size(); i++){
            fileNames.add(adData.get(i).getFileName());
        }

        return fileNames;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle args = getArguments();
        if (args != null){
            setAdSpace(args.getInt(KEY_POSITION));
        } else if(currentAdSpace != -1){
            setAdSpace(currentAdSpace);
        }
    }


    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_FILE_REQUEST) {
                if (data == null) {
                    return;
                 }

                final View mView;
                final AdData adListData = new AdData();
                Uri selectedFileUri = data.getData();
                final String selectedFilePath = FilePath.getPath(getActivity(), selectedFileUri);
                String[] tokens = selectedFilePath.substring(selectedFilePath.lastIndexOf("/")+1).split("\\.(?=[^\\.]+$)");
                String selectedFileName = tokens[0].trim().replaceAll("[\\-\\+\\.\\^:,{}_]", " ");
                final String selectedFileExt = (tokens.length > 1)?tokens[1].trim():"";
                Log.i(TAG, "Selected File Path:" + selectedFilePath);

                final int THUMBSIZE = 64;
                final Bitmap ThumbImage;

                if(currentAdSpace == 0 || currentAdSpace == 1){
                    ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(selectedFilePath), THUMBSIZE, THUMBSIZE);
                }else{
                    ThumbImage = ThumbnailUtils.createVideoThumbnail(selectedFilePath, MediaStore.Video.Thumbnails.MINI_KIND);
                }



                AlertDialog.Builder mBuilder = new AlertDialog.Builder(this.getActivity());
                mBuilder.setCancelable(false);
                mView = getActivity().getLayoutInflater().inflate(R.layout.upload_dialog, null);

                TextView originalFileName = (TextView) mView.findViewById(R.id.original_file_name);
                originalFileName.setText(selectedFilePath.substring(selectedFilePath.lastIndexOf("/")+1));

                final EditText fileDisplayName = (EditText) mView.findViewById(R.id.display_file_name);
                fileDisplayName.setText(selectedFileName);

                Button cancelButton = (Button) mView.findViewById(R.id.button_cancel);
                Button uploadButton = (Button) mView.findViewById(R.id.button_upload);
                uploadButton.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {

                        adListData.setFileName(fileDisplayName.getText().toString().trim().replaceAll(" ", "_")+"."+selectedFileExt);
                        adListData.setDisplayName(fileDisplayName.getText().toString().trim());
                        adListData.setAdSpaceId(currentAdSpace);
                        try {
                            adListData.setDirectoryPath(saveToInternalStorage(ThumbImage, adListData.getFileName()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if(dbHelper.isFileNamePersent(currentAdSpace, adListData.getFileName())){
                            Snackbar.make(coordinatorLayout,"File name already available!!", Snackbar.LENGTH_LONG).show();
                        }else{
                            new UploadFileToServer(mView, adListData, selectedFilePath).execute();
                        }
                    }
                });
                Button closeButton = (Button) mView.findViewById(R.id.button_close);
                closeButton.setEnabled(false);


                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                if(dialog.getWindow() != null)
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setCanceledOnTouchOutside(false);




                cancelButton.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                closeButton.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();

                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                lp.dimAmount=0.2f; // Dim level. 0.0 - no dim, 1.0 - completely opaque
                dialog.getWindow().setAttributes(lp);

            }
        }
    }


    public void setAdSpace(int adSpaceIndex){
        currentAdSpace = adSpaceIndex;
        adapter.updateAdList(dbHelper.getAdList(adSpaceIndex));
    }

    @Override
    public void deleteAdItem(String adName) {
        Toast.makeText(getActivity(), adName, Toast.LENGTH_SHORT).show();
        dbHelper.removeSingleAd(adName, currentAdSpace);
    }


    private class UploadFileToServer extends AsyncTask<String, String, Integer> {

        DonutProgress donut_progress;
        LinearLayout uploader_area;
        LinearLayout progress_area;
        LinearLayout uploader_area_button;
        LinearLayout progress_area_button;
        Button closeButton;

        File selectedFile;
        String selectedFilePath;
        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int serverResponseCode = 0;
        View mView;
        AdData adData = new AdData();

        UploadFileToServer(View mView, AdData adData, String selectedFilePath){
            this.mView = mView;
            this.adData = adData;
            this.selectedFilePath = selectedFilePath;
        }

        @Override
        protected void onPreExecute() {
            uploader_area = (LinearLayout) mView.findViewById(R.id.uploader_area);
            progress_area = (LinearLayout) mView.findViewById(R.id.progress_area);
            uploader_area_button = (LinearLayout) mView.findViewById(R.id.uploader_area_button);
            progress_area_button = (LinearLayout) mView.findViewById(R.id.progress_area_button);
            donut_progress = (DonutProgress) mView.findViewById(R.id.donut_progress);
            closeButton = (Button) mView.findViewById(R.id.button_close);
            donut_progress.setProgress(0);
            uploader_area.setVisibility(View.GONE);
            uploader_area_button.setVisibility(View.GONE);
            progress_area.setVisibility(View.VISIBLE);
            progress_area_button.setVisibility(View.VISIBLE);
            selectedFile = new File(selectedFilePath);
            totalSize = (int)selectedFile.length();
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            donut_progress.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected Integer doInBackground(String... params) {

                try {
                    FileInputStream fileInputStream = new FileInputStream(selectedFile);
                    BufferedInputStream bufInput = new BufferedInputStream(fileInputStream);
                    URL url = new URL(SERVER_URL);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);
                    connection.setChunkedStreamingMode(1024);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                    connection.setRequestProperty(
                            "Content-Type", "multipart/form-data;boundary=" + boundary);
                    connection.setRequestProperty("uploaded_file",selectedFilePath);
                    connection.setRequestProperty("new_name", adData.getFileName());

                    dataOutputStream = new DataOutputStream(connection.getOutputStream());

                    dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                    dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                            + selectedFilePath + "\"" + lineEnd);

                    dataOutputStream.writeBytes(lineEnd);

                    int totalSize = (int)selectedFile.length();


                    long progress = 0;
                    long bytesRead;
                    byte buf[] = new byte[1024];

                    while ((bytesRead = bufInput.read(buf)) != -1) {
                        dataOutputStream.write(buf, 0, (int)bytesRead);
                        dataOutputStream.flush();
                        progress += bytesRead;
                        publishProgress(""+(int)((progress*100)/totalSize));
                    }

                    publishProgress(""+(int)((progress*100)/totalSize));

                    dataOutputStream.writeBytes(lineEnd);
                    dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    try{
                        serverResponseCode = connection.getResponseCode();
                    }catch (OutOfMemoryError e){
                        Toast.makeText(getActivity(), "Memory Insufficient!", Toast.LENGTH_SHORT).show();
                    }

                    fileInputStream.close();
                    dataOutputStream.flush();
                    dataOutputStream.close();

                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                }
                return serverResponseCode;

        }


        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if(result == 200){
                adapter.appendToAdList(adData);
                dbHelper.insertAd(adData, adList.size());
            }
            closeButton.setEnabled(true);

        }
    }

    private class SendPostRequest extends AsyncTask<String, Void, String> {

        int adSpaceId;
        ArrayList<String> adList;

        SendPostRequest(int adSpaceId, ArrayList<String> adList){
            this.adSpaceId = adSpaceId;
            this.adList = adList;
        }


        @Override
        protected String doInBackground(String... params) {
            return POST(params[0], adSpaceId, adList);
        }

        @Override
        protected void onPostExecute(String result) {

            if (!result.equals("SUCCESS")) {
                Snackbar.make(coordinatorLayout, "Error Contacting Server ... Sorry Try Again.", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    public static String POST(String url, int adSpaceId, ArrayList<String> adList) {
        String result = "";
        try {
            URL serverUrl = new URL("http://" + url);

            JSONArray fileList = new JSONArray();
            for(int i=0; i<adList.size();i++)
                fileList.put(adList.get(i));

            JSONObject postDataParameters = new JSONObject();
            postDataParameters.put("adSpaceID", adSpaceId);
            postDataParameters.put("adList", fileList);


            HttpURLConnection httpURLConnection = (HttpURLConnection) serverUrl.openConnection();
            httpURLConnection.setReadTimeout(3000 /* milliseconds */);
            httpURLConnection.setConnectTimeout(3000 /* milliseconds */);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            OutputStream os = httpURLConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParameters));
            writer.flush();
            writer.close();
            os.close();

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder sb = new StringBuilder("");
                String line;

                while ((line = in.readLine()) != null) {

                    sb.append(line);
                    //break;
                }

                in.close();
                return sb.toString();

            } else {
                return "false : " + String.valueOf(responseCode);
            }

            //HttpURLConnection
        } catch (Exception e) {
            e.printStackTrace();
        }


        return result;
    }

    @NonNull
    private static String getPostDataString(JSONObject postDataParameters) throws Exception {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = postDataParameters.keys();

        while (itr.hasNext()) {

            String key = itr.next();
            Object value = postDataParameters.get(key);


            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }

        return result.toString();
    }

    @NonNull
    private String saveToInternalStorage(Bitmap thumbnail, String fileName) throws IOException {

        ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File path = new File(directory, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            // Use the compress method on the BitMap object to write image to the OutputStream
            thumbnail.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(fos != null)
                fos.close();
        }
        return directory.getAbsolutePath();
    }

}
