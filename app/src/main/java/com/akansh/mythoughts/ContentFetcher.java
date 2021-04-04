package com.akansh.mythoughts;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.net.ssl.HttpsURLConnection;

public class ContentFetcher extends AsyncTask<Void, Void, Void> {
    String packageName="";

    public ContentFetcher(String packageName) {
        this.packageName = packageName;
    }

    DownloadListeners downloadListeners;
    boolean status=false;

    @Override
    protected void onPreExecute() {
        downloadListeners.onDownloadStarted();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            URL file = new URL(Constants.ZIP_FILE_URL);
            HttpsURLConnection urlConnection = (HttpsURLConnection) file.openConnection();
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            File f=new File("/data/data/"+packageName,"app_data.zip");
            FileOutputStream stream = new FileOutputStream(f);
            int bytesRead;
            byte[] buffer = new byte[2048];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                stream.write(buffer, 0, bytesRead);
            }
            stream.close();
            inputStream.close();
            status=unzip(new File("/data/data/"+packageName+"/app_data.zip"),new File("/data/data/"+packageName+"/"+Constants.NEW_DIR));
            if(!status) {
                f.delete();
                Log.d("ANSOFT","Failed To Unzip File!");
            }

            // Delete Prev-Ver Files
            File pV=new File("/data/data/"+packageName+"/"+Constants.OLD_DIR+"/index.html");
            if(pV.exists()) {
                deleteDirectory(pV);
            }
        }catch (Exception e) {
            Log.d("ANSOFT",e.getMessage());
            status=false;
            Log.d("ANSOFT","Failed To Download File!");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        downloadListeners.onDownloadCompeted(status);
    }


    public interface DownloadListeners {
        void onDownloadCompeted(boolean status);
        void onDownloadStarted();
    }

    public boolean unzip(File zipFile, File targetDirectory) {
        try {
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    return false;
                if (ze.isDirectory())
                    continue;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                }catch (Exception e){
                    Log.d("ANSOFT",e.getMessage());
                }finally {
                    fout.close();
                }
            }
            zis.close();
            zipFile.delete();
            return true;
        }catch(Exception e) {
            Log.d("ANSOFT",e.getMessage());
        }
        return false;
    }

    public void deleteDirectory(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] files=fileOrDirectory.listFiles();
            if(files!=null) {
                for (File child : files) {
                    deleteDirectory(child);
                }
            }
        }
        fileOrDirectory.delete();
    }
}
