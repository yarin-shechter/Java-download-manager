package DownloadManager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileWriter implements Runnable{
    private DownloadMetaData m_CurDownloadData;
    private RandomAccessFile m_DiskWriter;

    public FileWriter(DownloadMetaData i_CurDownloadData){
        this.m_CurDownloadData = i_CurDownloadData;
    }

    public void run(){
        try{
            this.m_DiskWriter = new RandomAccessFile(new File(this.m_CurDownloadData.m_FileName), "rw");
        }
        catch (IOException e){
            System.err.println("Failure while trying to access disk for writing the downloaded file : " + e);
            System.err.println("Download failed.");
            System.exit(-1);

        }

        while(!this.m_CurDownloadData.m_DownloadIsComplete){
            ContinuousFileChunk nextChunkToWriteToDisk = RetrieveNextChunkFromQueue();

            if (nextChunkToWriteToDisk != null){
                try{
                    WriteChunkToDisk(nextChunkToWriteToDisk);
                }
                catch (IOException e){
                    System.err.println("Failure while trying to write chunk to file : " + e);
                    System.err.println("Download failed");
                    System.exit(-1);
                    return;
                }
            }
            UpdateMetaData(nextChunkToWriteToDisk);
        }
        FileAndNetworkUtils.SafelyCloseCloseable(this.m_DiskWriter);
    }

    private void WriteChunkToDisk(ContinuousFileChunk i_ChunkToWrite) throws IOException{

        this.m_DiskWriter.seek(i_ChunkToWrite.m_IndexOfFirstByteInChunk);
        this.m_DiskWriter.write(i_ChunkToWrite.m_ChunkData);
    }

    private ContinuousFileChunk RetrieveNextChunkFromQueue(){
        ContinuousFileChunk nextChunkToWriteToDisk = null;
        try{
            nextChunkToWriteToDisk = DownloadManagerQueue.GetInstance().take();
        }
        catch(InterruptedException e){}
        return nextChunkToWriteToDisk;
    }

    private void UpdateMetaData(ContinuousFileChunk i_ChunkThatHasBeenWrittenToDisk){
        long totalNumberOfBytesWrittenBeforeChunk = this.m_CurDownloadData.m_NumberOfBytesCompleted;

        UpdateNumberOfBytesWritten(i_ChunkThatHasBeenWrittenToDisk);
        UpdateChunkBitMap(i_ChunkThatHasBeenWrittenToDisk);
        HandleProgressInDownload(totalNumberOfBytesWrittenBeforeChunk);
    }

    private void UpdateNumberOfBytesWritten(ContinuousFileChunk i_ChunkThatHasBeenWrittenToDisk){
        this.m_CurDownloadData.m_NumberOfBytesCompleted = this.m_CurDownloadData.m_NumberOfBytesCompleted + i_ChunkThatHasBeenWrittenToDisk.m_ChunkData.length;
    }

    private void UpdateChunkBitMap(ContinuousFileChunk i_ChunkThatHasBeenWrittenToDisk){
        this.m_CurDownloadData.m_DownloadChunksBitMap[i_ChunkThatHasBeenWrittenToDisk.m_ChunkIndex] = true;
    }

    private void HandleProgressInDownload(long i_NumberOfBytesWrittenBeforeChunk){
        // Multiplication by 100 is used for percentage calculations purposes.
        long percentCompletedBeforeChunk = i_NumberOfBytesWrittenBeforeChunk * 100 / this.m_CurDownloadData.m_FileSize;
        long percentCompletedAfterChunk = this.m_CurDownloadData.m_NumberOfBytesCompleted * 100 / this.m_CurDownloadData.m_FileSize;
        if (percentCompletedAfterChunk > percentCompletedBeforeChunk){
            this.m_CurDownloadData.serializeObject();
            System.err.println("Downloaded " + percentCompletedAfterChunk + "%");
        }
        this.m_CurDownloadData.m_DownloadIsComplete = percentCompletedAfterChunk == 100;
    }
}
