package DownloadManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DownloadMetaData implements Serializable {
    public static final String s_TempFileSuffix = ".tmp2";
    public static final String s_MetadataFileSuffix = ".tmp";
    public boolean[] m_DownloadChunksBitMap;
    public String m_FileName;
    public long m_FileSize;
    public long m_NumberOfBytesCompleted;
    public int m_NumberOfFileChunks;
    public boolean m_DownloadIsComplete;


    public DownloadMetaData(int i_NumberOfFileChunks, String i_FileName, long i_FileSize){
        this.m_DownloadChunksBitMap = new boolean[i_NumberOfFileChunks];
        this.m_NumberOfBytesCompleted = 0;
        this.m_FileName = i_FileName;
        this.m_FileSize = i_FileSize;
        this.m_NumberOfFileChunks = i_NumberOfFileChunks;
        this.m_DownloadIsComplete = false;
    }

    public void serializeObject(){
        ObjectOutputStream tempStream = null;

        try{
            File tempSerializationFile = new File(this.m_FileName + s_TempFileSuffix);
            tempStream = new ObjectOutputStream(new FileOutputStream(tempSerializationFile));
            tempStream.writeObject(this);
        }
        catch (IOException e){
            System.err.println("Failure occurred while attempting to create temporary copy of download meta-data : " + e);
            System.err.println("Download failed.");
            System.exit(-1);
        }
        finally{
            if (tempStream != null){
                try{
                    tempStream.close();
                }
                catch (IOException e){}
            }
        }

        boolean fileHasBeenSuccesfullyMoved = false;
        while(!fileHasBeenSuccesfullyMoved){
            try{
                Path tempFilePath = Paths.get(m_FileName + s_TempFileSuffix);
                Path serializationFilePath = Paths.get(m_FileName + s_MetadataFileSuffix);
                Files.move(tempFilePath, serializationFilePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                fileHasBeenSuccesfullyMoved = true;
            }
            catch(IOException e){
            }
        }

    }

    public static DownloadMetaData DeserializeObject(String i_DownloadedFileName){
        DeleteResidualTMPFiles(i_DownloadedFileName);
        DownloadMetaData DeserializedObject = null;
        try{
            ObjectInputStream tempStream = new ObjectInputStream(new FileInputStream(i_DownloadedFileName + s_MetadataFileSuffix));
            DeserializedObject = (DownloadMetaData)tempStream.readObject();
        }
        catch(Exception e){

        }
        return DeserializedObject;
    }

    public static void DeleteMetaData(String i_DownloadedFileName){
        File metaDataFile = new File(i_DownloadedFileName + s_MetadataFileSuffix);
        metaDataFile.delete();
    }

    private static void DeleteResidualTMPFiles(String i_DownloadedFileName){
        File tempSerializationFile = new File(i_DownloadedFileName + s_TempFileSuffix);
        while(tempSerializationFile.exists()){
            if(tempSerializationFile.delete()){
                System.err.println("I deleted the tmp file successfully");
            }
        }

    }
}
