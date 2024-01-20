package DownloadManager;

public class ContinuousFileChunk {
    public long m_IndexOfFirstByteInChunk;
    public long m_IndexOfLastByteInChunk;
    public int m_ChunkIndex;
    public byte[] m_ChunkData;

    public ContinuousFileChunk(long i_IndexOfFirstByteInChunk, long i_IndexOfLastByteInChunk, int i_ChunkIndex, byte[] i_ChunkData){
        this.m_IndexOfFirstByteInChunk = i_IndexOfFirstByteInChunk;
        this.m_IndexOfLastByteInChunk = i_IndexOfLastByteInChunk;
        this.m_ChunkData = i_ChunkData;
        this.m_ChunkIndex = i_ChunkIndex;
    }
}