package net.risesoft.support.comparator;

import java.util.Comparator;

import net.risesoft.entity.FileNode;

public class FileSizeComparator implements Comparator<FileNode> {

    private boolean asc = true;

    public FileSizeComparator() {}

    public FileSizeComparator(boolean asc) {
        this.asc = asc;
    }

    @Override
    public int compare(FileNode o1, FileNode o2) {
        Long o1FileSize = o1.getFileSize();
        Long o2FileSize = o2.getFileSize();

        if (o1FileSize == null) {
            o1FileSize = 0L;
        }
        if (o2FileSize == null) {
            o2FileSize = 0L;
        }

        int result = o1FileSize.compareTo(o2FileSize);
        return asc ? result : -1 * result;
    }
}
