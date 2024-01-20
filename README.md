# Java-download-manager
A Java command line application that implements a download manager/accelerator (remember FlashGet?). The application utilizes concurrent HTTP connections to accelerate downloads.
The application receives up to two parameters - A download URL, and the maximum allowed number of concurrent HTTP connections.
The application will properly resume download after the previous invocation was terminated due to a signal (any signal) or network disconnection, and prints its progress in "percentage completed" terms.
### Recommended download for demo (allows concurrent HTTP connections): https://archive.org/download/Mario1_500/Mario1_500.avi

List of software components :
- IdcDM - program entry point, containing the main method which calls DownloadManager.
- DownloadManager - This class manages the flow of execution and synchronizes the different components.
- DownloadMetaData - A data object representing the download's meta-data.
- ContinuousFileChunk - A data object representing a chunk of the file - chunk objects are written and read from the queue.
- DownloadManagerQueue - A class representing a queue that is used for storing chunks of the downloaded file to be written to disk.
- HTTPRangeGetter - represents a single "worker" - receives a range to download, initiates an HTTP connection and downloads the range one chunk at a time, while writing it to the DownloadManagerQueue.
- WorkDivider - This class is incharge of delegating work to each HTTPRangeGetter. It reviews the current metadata, and divides download ranges between workers evenly.
- FileAndNetworkUtils - A static service class containing some utility methods.
- FileWriter - This class reads chunks from the queue and writes them to disk.
- ByteRange - A data object representing a byte range.
