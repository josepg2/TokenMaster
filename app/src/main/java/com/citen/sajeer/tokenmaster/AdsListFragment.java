package com.citen.sajeer.tokenmaster;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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
    int mCurrentPosition = 0;

    private String selectedFilePath;
    private String selectedFileName;
    private String selectedFileExt;

    DbHelper dbHelper;

    List<String> adList;
    AdSpaceRecyclerListAdapter adapter;
    ItemTouchHelper mItemTouchHelper;

    int totalSize = 0;

    DonutProgress donut_progress;
    View mView;
    LinearLayout uploader_area;
    LinearLayout progress_area;
    LinearLayout uploader_area_button;
    LinearLayout progress_area_button;
    String backgroundFileName;

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
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_FILE_REQUEST);
            }
        });

        return v;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)  {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = DbHelper.getInstance(getActivity().getApplicationContext());
        adList = dbHelper.getAdList(mCurrentPosition);

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
                Snackbar.make(coordinatorLayout,"Clicked", Snackbar.LENGTH_LONG).show();
                dbHelper.updateAdListStatus(adapter.getAdListItems(), mCurrentPosition);
                new SendPostRequest(mCurrentPosition, dbHelper.getAdListFileNames(mCurrentPosition)).execute("192.168.0.108:8000");
                return true;
            }
        });
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
        } else if(mCurrentPosition != -1){
            setAdSpace(mCurrentPosition);
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


                Uri selectedFileUri = data.getData();
                selectedFilePath = FilePath.getPath(getActivity(), selectedFileUri);
                String[] tokens = selectedFilePath.substring(selectedFilePath.lastIndexOf("/")+1).split("\\.(?=[^\\.]+$)");
                selectedFileName = tokens[0].trim().replaceAll("[\\-\\+\\.\\^:,(){}]", " ");
                selectedFileExt = (tokens.length > 1)?tokens[1].trim():"";
                Log.i(TAG, "Selected File Path:" + selectedFilePath);

                AlertDialog.Builder mBuilder = new AlertDialog.Builder(this.getActivity());
                mView = getActivity().getLayoutInflater().inflate(R.layout.upload_dialog, null);

                uploader_area = (LinearLayout) mView.findViewById(R.id.uploader_area);
                progress_area = (LinearLayout) mView.findViewById(R.id.progress_area);
                uploader_area_button = (LinearLayout) mView.findViewById(R.id.uploader_area_button);
                progress_area_button = (LinearLayout) mView.findViewById(R.id.progress_area_button);

                donut_progress = (DonutProgress) mView.findViewById(R.id.donut_progress);

                TextView originalFileName = (TextView) mView.findViewById(R.id.original_file_name);
                originalFileName.setText("Uploading file, " + selectedFilePath.substring(selectedFilePath.lastIndexOf("/")+1) + " to server, Update the display name if necessary...");

                final EditText fileDisplayName = (EditText) mView.findViewById(R.id.display_file_name);
                fileDisplayName.setText(selectedFileName);

                Button cancelButton = (Button) mView.findViewById(R.id.button_cancel);
                Button uploadButton = (Button) mView.findViewById(R.id.button_upload);
                uploadButton.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        backgroundFileName = fileDisplayName.getText().toString().trim();
                        backgroundFileName = backgroundFileName.replaceAll(" ", "_");
                        if(dbHelper.isFileNamePersent(mCurrentPosition, backgroundFileName)){
                            Snackbar.make(coordinatorLayout,"File name already available!!", Snackbar.LENGTH_LONG).show();
                        }else{
                            new UploadFileToServer().execute();
                        }
                    }
                });
                Button closeButton = (Button) mView.findViewById(R.id.button_close);


                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
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

            }
        }
    }


    public void setAdSpace(int adSpaceIndex){
        mCurrentPosition = adSpaceIndex;
        adapter.updateAdList(dbHelper.getAdList(adSpaceIndex));
    }

    @Override
    public void deleteAdItem(String adName) {
        Toast.makeText(getActivity(), adName, Toast.LENGTH_SHORT).show();
        dbHelper.removeSingleAd(adName, mCurrentPosition);
    }


    private class UploadFileToServer extends AsyncTask<String, String, Integer> {

        File selectedFile;
        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int serverResponseCode = 0;

        @Override
        protected void onPreExecute() {
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
            donut_progress.setProgress(Integer.parseInt(progress[0])); //Updating progress
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
                    connection.setRequestProperty("new_name", backgroundFileName);

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
                adapter.appendToAdList(selectedFileName);
                dbHelper.insertAd(mCurrentPosition, adList.size(), backgroundFileName, selectedFileExt);
            }

        }
    }

    public class SendPostRequest extends AsyncTask<String, Void, String> {

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

    private Bitmap loadImageFromStorage(String path, String fileName)
    {
        Bitmap b = null;
        try {
            File f = new File(path, fileName);
            b = BitmapFactory.decodeStream(new FileInputStream(f));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return b;
    }
}
