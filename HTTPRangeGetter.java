package DownloadManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

public class HTTPRangeGetter implements Runnable {
    private static Object m_TerminationLock = new Object();
    private LinkedList<ByteRange> m_RangesToDownload;
    private int m_SizeOfEachChunk;
    private int m_FinalChunkIndex;
    private String m_DownloadURL;
    // Timeouts are set reasonably so that a timeout indicates a connection cannot be established.
    private static final int sr_ConnectionTimeoutTime = 15000;
    private static final int sr_ReadTimeOutTime = 15000;


    public HTTPRangeGetter(LinkedList<ByteRange> i_RangesToDownload, int i_SizeOfEachChunk, int i_FinalChunkIndex, String i_DownloadUrl){
        this.m_RangesToDownload = i_RangesToDownload;
        this.m_DownloadURL = i_DownloadUrl;
        this.m_SizeOfEachChunk = i_SizeOfEachChunk;
        this.m_FinalChunkIndex = i_FinalChunkIndex;
    }

    public void run()
    {
        DownloadRanges();
    }

    private void DownloadRanges(){
        for (ByteRange range : this.m_RangesToDownload){
            DownloadRangeAndDivideToChunks(range);
        }
    }

    private void DownloadRangeAndDivideToChunks(ByteRange i_RangeToDownload){
        InputStream rangeStream = null;
        try{
            rangeStream = FileAndNetworkUtils.GetRangeInputStream(this.m_DownloadURL, i_RangeToDownload.m_BottomByteIndex, i_RangeToDownload.m_TopByteIndex, sr_ConnectionTimeoutTime, sr_ReadTimeOutTime);
        }
        catch(Exception e){
            synchronized(m_TerminationLock) {
                System.err.println("Cannot open connection to begin downloading a range of bytes : " + e);
                terminateDownload();
            }
        }
        long curChunkFirstByteIndex = i_RangeToDownload.m_BottomByteIndex;
        while (curChunkFirstByteIndex < i_RangeToDownload.m_TopByteIndex){
            int indexOfCurrentChunk = (int) curChunkFirstByteIndex / this.m_SizeOfEachChunk;
            if (indexOfCurrentChunk != this.m_FinalChunkIndex) {
                byte[] chunkBytes = new byte[this.m_SizeOfEachChunk];
                try {
                    rangeStream.readNBytes(chunkBytes, 0, this.m_SizeOfEachChunk);
                }
                catch (IOException e) {
                    synchronized(m_TerminationLock) {
                        System.err.println("Failed to download specific range of bytes from the file : " + e);
                        terminateDownload();
                    }
                }
                ContinuousFileChunk chunk = new ContinuousFileChunk(curChunkFirstByteIndex, curChunkFirstByteIndex + this.m_SizeOfEachChunk - 1, indexOfCurrentChunk, chunkBytes);
                PutFileChunkInQueue(chunk);
                curChunkFirstByteIndex += this.m_SizeOfEachChunk;
            }
            else{
                byte[] chunkBytes = new byte[(int)(i_RangeToDownload.m_TopByteIndex - curChunkFirstByteIndex + 1)];
                try{
                    rangeStream.readNBytes(chunkBytes, 0 ,((int)(i_RangeToDownload.m_TopByteIndex - curChunkFirstByteIndex + 1)));
                }
                catch(Exception e){
                    synchronized(m_TerminationLock) {
                        System.err.println("Failed to download specific range of bytes from the file : " + e);
                        terminateDownload();
                    }
                }
                ContinuousFileChunk chunk = new ContinuousFileChunk(curChunkFirstByteIndex, i_RangeToDownload.m_TopByteIndex, indexOfCurrentChunk,chunkBytes);
                PutFileChunkInQueue(chunk);
                curChunkFirstByteIndex = i_RangeToDownload.m_TopByteIndex + 1;
            }
        }
    }

    private void terminateDownload(){
        System.err.println("Download failed!");
        System.exit(-1);

    }

    private void PutFileChunkInQueue(ContinuousFileChunk i_FileChunkToPut){
        try{
            DownloadManagerQueue.GetInstance().put(i_FileChunkToPut);
        }
        catch (Exception e){
            synchronized(m_TerminationLock) {
                System.err.println("Failure to write bytes within a specific range to file chunk queue : " + i_FileChunkToPut.m_IndexOfFirstByteInChunk + "-" + i_FileChunkToPut.m_IndexOfLastByteInChunk + ":" + e);
                terminateDownload();
            }
        }
    }
}
