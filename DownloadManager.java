package DownloadManager;

import ProofsOfConcept.FileBytePrinter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DownloadManager {
    private DownloadMetaData m_CurDownloadData;
    private String[] m_FileUrlsList;
    private int m_NumberOfThreadsUsed;
    private static final int sr_SizeOfEachChunk = 64000;

    public DownloadManager(DownloadMetaData i_CurDownloadData,int i_NumberOfThreadsToUse, String[] i_FileUrlsList){
        this.m_CurDownloadData = i_CurDownloadData;
        this.m_NumberOfThreadsUsed = i_NumberOfThreadsToUse;
        this.m_FileUrlsList = i_FileUrlsList;
    }

    public void ResumeDownload(){
        System.err.println("Downloading using " + this.m_NumberOfThreadsUsed + " connections via " + this.m_FileUrlsList.length + " mirror(s).");
        WorkDivider.DivideWorkBetweenWorkers(this.m_CurDownloadData.m_DownloadChunksBitMap, this.m_CurDownloadData.m_FileSize, this.m_NumberOfThreadsUsed, this.m_FileUrlsList);
        SubmitFileWriter();

        if (this.m_CurDownloadData.m_DownloadIsComplete){
            System.err.println("Download succeeded");
            DownloadMetaData.DeleteMetaData(this.m_CurDownloadData.m_FileName);
        }
    }

    private void SubmitFileWriter(){
        Thread writer = new Thread(new FileWriter(this.m_CurDownloadData));
        writer.start();
        boolean writerHasFinished = false;
        while(!writerHasFinished){
            try{
                writer.join();
                writerHasFinished = true;
            }
            catch(InterruptedException e){
            }
        }
    }

    public static void ManageDownload(String[] args){
        if (args.length == 0){
            System.out.println("usage:\n" +
                    "java IdcDm URL|URL-LIST-FILE [MAX-CONCURRENT-CONNECTIONS]");
        }
        else{
            Integer maxNumberOfConnections = null;
            String Address = args[0];
            Address = removeParentheses(Address);
            if (args.length > 1){
                maxNumberOfConnections = Integer.parseInt(args[1]);
            }
            if (FileAndNetworkUtils.isValidURL(Address)){
                PrepareForDownload(new String[]{Address}, maxNumberOfConnections);
            }
            else{
                String[] MirrorList = CompileMirrorListFromFile(Address);
                PrepareForDownload(MirrorList, maxNumberOfConnections);
            }
        }
    }

    private static void PrepareForDownload(String[] i_DownloadUrls, Integer i_MaxNumberOfConnections){
        DownloadMetaData metaData = GetDownloadMetaDataObject(i_DownloadUrls[0]);

        int NumberOfThreadsToUse = 0;
        if (i_MaxNumberOfConnections != null){
            NumberOfThreadsToUse = i_MaxNumberOfConnections;
        }
        else{
            NumberOfThreadsToUse = 1;
        }
        DownloadManager manager = new DownloadManager(metaData, NumberOfThreadsToUse, i_DownloadUrls);
        manager.ResumeDownload();
    }

    private static DownloadMetaData GetDownloadMetaDataObject(String i_DownloadUrl){
        DownloadMetaData metaData = null;
        String fileName = FileAndNetworkUtils.RetrieveFileNameFromUrl(i_DownloadUrl);
        DownloadMetaData metaDataFromDisk = DownloadMetaData.DeserializeObject(fileName);

        boolean metaDataHasBeenLoadedFromDisk = (metaDataFromDisk != null);
        if (metaDataHasBeenLoadedFromDisk){
            metaData = metaDataFromDisk;
        }
        else{
            metaData = CreateNewMetaData(i_DownloadUrl);
        }
        return metaData;
    }

    private static DownloadMetaData CreateNewMetaData(String i_DownloadUrl){
        long fileSize = FileAndNetworkUtils.retrieveFileSize(i_DownloadUrl);
        int numberOfChunks = DecideOnNumberOfChunksForDownload(fileSize);
        String fileName = FileAndNetworkUtils.RetrieveFileNameFromUrl(i_DownloadUrl);
        DownloadMetaData newMetaData = new DownloadMetaData(numberOfChunks, fileName, fileSize);
        return newMetaData;
    }

    private static int DecideOnNumberOfChunksForDownload(long i_FileSize){
        return (int) i_FileSize / sr_SizeOfEachChunk;
    }

    private static String[] CompileMirrorListFromFile(String i_ListAddress){
       LinkedList<String> listOfMirrors = new LinkedList<String>();
        try (BufferedReader mirrorReader = new BufferedReader(new FileReader(i_ListAddress))) {
            String line;
            while ((line = mirrorReader.readLine()) != null) {
                listOfMirrors.add(line);
            }
        }
        catch(Exception e){
            System.err.println("Failed to open mirror list - " + e);
            System.err.println("Download failed");
            System.exit(-1);
        }
        String[] mirrorArray = new String[listOfMirrors.size()];
        listOfMirrors.toArray(mirrorArray);
        return mirrorArray;
    }

    private static String removeParentheses(String i_StringToTreat){
        String treatedString = i_StringToTreat;
        if (i_StringToTreat.charAt(0) == '"'){
            treatedString = treatedString.substring(1);
        }
        if (i_StringToTreat.charAt(i_StringToTreat.length() - 1) == '"'){
            treatedString = treatedString.substring(0, treatedString.length() - 1);
        }

        return treatedString;
    }
}