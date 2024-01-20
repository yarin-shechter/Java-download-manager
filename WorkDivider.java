package DownloadManager;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;

public class WorkDivider {
    public static int curChunk;

    public static void DivideWorkBetweenWorkers(boolean[] i_ChunkBitMap, long i_TotalNumberOfBytes, int i_NumberOfWorkers, String[] i_ListOfMirrors){
        int sizeOfEachChunkInBytes = (int) i_TotalNumberOfBytes / i_ChunkBitMap.length;
        int numberOfUnfinishedChunks = 0;
        int mirrorNumber = 0;
        for(boolean chunk : i_ChunkBitMap){
            numberOfUnfinishedChunks += chunk ? 0 : 1;
        }
        for (int i = 0; i < numberOfUnfinishedChunks % i_NumberOfWorkers; i++){
            int numberOfRequiredChunksForCurWorker = (numberOfUnfinishedChunks / i_NumberOfWorkers + 1);
            LinkedList<ByteRange> rangesForCurWorker = retrieveByteRangesForWorker(i_ChunkBitMap, numberOfRequiredChunksForCurWorker, sizeOfEachChunkInBytes, i_TotalNumberOfBytes);
            Thread worker = new Thread(new HTTPRangeGetter(rangesForCurWorker, sizeOfEachChunkInBytes, i_ChunkBitMap.length - 1, i_ListOfMirrors[mirrorNumber]));
            worker.start();
            mirrorNumber = (mirrorNumber + 1) % i_ListOfMirrors.length;
        }

        for (int i = numberOfUnfinishedChunks % i_NumberOfWorkers; i < i_NumberOfWorkers; i++){
            int numberOfRequiredChunksForCurWorker = (numberOfUnfinishedChunks / i_NumberOfWorkers);
            LinkedList<ByteRange> rangesForCurWorker = retrieveByteRangesForWorker(i_ChunkBitMap, numberOfRequiredChunksForCurWorker, sizeOfEachChunkInBytes, i_TotalNumberOfBytes);
            Thread worker = new Thread(new HTTPRangeGetter(rangesForCurWorker, sizeOfEachChunkInBytes, i_ChunkBitMap.length - 1, i_ListOfMirrors[mirrorNumber]));
            worker.start();
            mirrorNumber = (mirrorNumber + 1) % i_ListOfMirrors.length;
        }
    }

    private static LinkedList<ByteRange> retrieveByteRangesForWorker(boolean[] i_ChunkBitMap, int i_NumberOfChunksRequiredForWorker, int i_SizeOfEachChunkInBytes, long i_TotalNumberOfBytes){
        LinkedList<Integer> i_ChunkIndices = new LinkedList<>();
        while(i_ChunkIndices.size() < i_NumberOfChunksRequiredForWorker){
            if (i_ChunkBitMap[curChunk] == false){
                i_ChunkIndices.add(curChunk);
            }
            curChunk++;
        }
        LinkedList<ByteRange> RangesForCurWorker = ConvertChunkIndicesToRanges(i_ChunkIndices, i_ChunkBitMap.length, i_SizeOfEachChunkInBytes, i_TotalNumberOfBytes);
        RangesForCurWorker = ByteRange.UniteByteRanges(RangesForCurWorker);
        return RangesForCurWorker;
    }

    public static LinkedList<ByteRange> ConvertChunkIndicesToRanges(LinkedList<Integer> i_ChunkIndices, int i_TotalNumberOfChunks, int i_SizeOfEachChunkInBytes, long i_TotalNumberOfBytes){
        LinkedList<ByteRange> rangesList = new LinkedList<>();
        for(Integer index : i_ChunkIndices){
            if (index != i_TotalNumberOfChunks - 1){
                rangesList.add(new ByteRange(index * i_SizeOfEachChunkInBytes, (index + 1) * i_SizeOfEachChunkInBytes - 1));
            }
            else{
                rangesList.add(new ByteRange((i_TotalNumberOfChunks - 1) * i_SizeOfEachChunkInBytes, i_TotalNumberOfBytes - 1));
            }
        }
        return rangesList;
    }
}
