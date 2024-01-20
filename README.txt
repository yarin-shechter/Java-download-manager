List of software components :
- ByteRange - A data object representing a byte range.
- ContinuousFileChunk - A data object representing a chunk of the file - chunk objects are written and read from the queue.
- DownloadManager - This class manages the flow of execution and synchronizes the different component.
- DownloadManagerQueue - A class representing the queue that chunks are written to and read from.
- DownloadMetaData - A data object representing the download's meta-data.
- FileAndNetworkUtils - A static service class containing some utility methods.
- FileWriter - This class reads chunks from the queue and writes them to the file.
- HTTPRangeGetter - represents a single worker in the class - receives a range to download, downloads it one chunk at a time, and writes to queue.
- WorkDivider - This class is incharge of delegating work to the HTTPRangeGetters. It reviews the current metadata, and divides download ranges between workers evenly.