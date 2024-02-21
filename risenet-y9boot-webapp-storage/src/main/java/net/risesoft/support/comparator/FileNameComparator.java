package net.risesoft.support.comparator;

import java.util.Comparator;

import net.risesoft.entity.FileNode;

public class FileNameComparator implements Comparator<FileNode> {

    private boolean asc = true;

    public FileNameComparator() {}

    public FileNameComparator(boolean asc) {
        this.asc = asc;
    }

    @Override
    public int compare(FileNode o1, FileNode o2) {
        int result = o1.getName().compareTo(o2.getName());
        return asc ? result : -1 * result;
    }
}
