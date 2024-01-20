package DownloadManager;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;

public class FileAndNetworkUtils {
    private FileAndNetworkUtils(){}

    public static void SafelyCloseConnection(HttpURLConnection i_ConnectionToClose){
        if (i_ConnectionToClose != null){
            try{
                i_ConnectionToClose.getInputStream().close();
            }
            catch(IOException e){}
            i_ConnectionToClose.disconnect();
        }
    }

    public static long retrieveFileSize(String i_DownloadUrl) {
        URL downloadUrl;
        HttpURLConnection fileSizeQueryConnection = null;
        long sizeOfFileInBytes = 0;

        try {
            downloadUrl = new URL(i_DownloadUrl);
            fileSizeQueryConnection = (HttpURLConnection) downloadUrl.openConnection();
            fileSizeQueryConnection.setRequestMethod("HEAD");
            sizeOfFileInBytes = fileSizeQueryConnection.getContentLength();
        }
        catch (Exception e) {
            System.out.println("Failure in attempting to ascertain file size : " + e);
        }
        finally {
            FileAndNetworkUtils.SafelyCloseConnection(fileSizeQueryConnection);
        }

        return sizeOfFileInBytes;
    }

    public static InputStream GetRangeInputStream(String i_URL, long i_FirstByteToDownloadIndex, long i_LastByteToDownloadIndex, int i_ConnectTimeout, int i_ReadTimeout) throws Exception{
        HttpURLConnection downloadConnection = null;
        URL fileUrl = new URL(i_URL);
        downloadConnection = (HttpURLConnection) fileUrl.openConnection();
        downloadConnection.setConnectTimeout(i_ConnectTimeout);
        downloadConnection.setReadTimeout(i_ReadTimeout);
        downloadConnection.setRequestMethod("GET");
        downloadConnection.setRequestProperty("Range", "bytes=" + i_FirstByteToDownloadIndex + "-" + i_LastByteToDownloadIndex);
        InputStream inclusiveRangeResponse = downloadConnection.getInputStream();
        return inclusiveRangeResponse;
    }

    public static boolean isValidURL(String urlString)
    {
        try
        {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        } catch (Exception exception)
        {
            return false;
        }
    }

    public static String RetrieveFileNameFromUrl(String i_FileUrl){
        String url = i_FileUrl;
        String filename = "";
        try{
            filename = Paths.get(new URL(url).getPath()).getFileName().toString();
        }
        catch(IOException e){

        }
        return filename;
    }

    public static void SafelyCloseCloseable(Closeable i_ObjectToClose){
        try{
            i_ObjectToClose.close();
        }
        catch(IOException e){

        }
    }
}
