package DownloadManager;

import java.util.concurrent.LinkedBlockingQueue;

public class DownloadManagerQueue {
    private static LinkedBlockingQueue<ContinuousFileChunk> m_DownloadManagerQueue = new LinkedBlockingQueue<>();

    private DownloadManagerQueue(){}

    public static LinkedBlockingQueue<ContinuousFileChunk> GetInstance(){
        return m_DownloadManagerQueue;
    }
}
