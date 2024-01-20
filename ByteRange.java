package DownloadManager;

import java.util.LinkedList;

public class ByteRange {
    public long m_BottomByteIndex;
    public long m_TopByteIndex;

    public ByteRange(long i_BottomByteIndex, long i_TopByteIndex){
        this.m_BottomByteIndex = i_BottomByteIndex;
        this.m_TopByteIndex = i_TopByteIndex;
    }

    private static ByteRange UniteConsecutiveByteRanges(ByteRange i_Range1, ByteRange i_Range2){
        if (i_Range1.m_TopByteIndex + 1 == i_Range2.m_BottomByteIndex){
            return new ByteRange(i_Range1.m_BottomByteIndex, i_Range2.m_TopByteIndex);
        }
        else{
            return null;
        }
    }

    public static LinkedList<ByteRange> UniteByteRanges(LinkedList<ByteRange> i_ListOfRangesToUnite){
        LinkedList<ByteRange> unitedRanges = new LinkedList<>();
        ByteRange previousRange = null;
        for (ByteRange curRange : i_ListOfRangesToUnite){
            if (previousRange == null){
                previousRange = curRange;
            }
            else{
                ByteRange unitedResult = UniteConsecutiveByteRanges(previousRange, curRange);
                if (unitedResult == null){
                    unitedRanges.add(previousRange);
                    previousRange = curRange;
                }
                else{
                    previousRange = unitedResult;
                }
            }
        }

        if (previousRange != null){
            unitedRanges.add(previousRange);
        }

        return unitedRanges;
    }

    public String toString(){
        return "[" + m_BottomByteIndex + " - " + m_TopByteIndex + "]";
    }
}
