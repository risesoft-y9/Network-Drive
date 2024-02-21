package net.risesoft.support.comparator;

import java.util.Comparator;
import java.util.Date;

import net.risesoft.entity.FileNode;

public class UpdateTimeComparator implements Comparator<FileNode> {

    private boolean asc = true;

    public UpdateTimeComparator() {}

    public UpdateTimeComparator(boolean asc) {
        this.asc = asc;
    }

    @Override
    public int compare(FileNode o1, FileNode o2) {
        Date o1UpdateTime = o1.getUpdateTime();
        Date o2UpdateTime = o2.getUpdateTime();

        if (o1UpdateTime == null) {
            o1UpdateTime = o1.getCreateTime();
        }
        if (o2UpdateTime == null) {
            o2UpdateTime = o2.getCreateTime();
        }

        int result = o1UpdateTime.compareTo(o2UpdateTime);
        return asc ? result : -1 * result;
    }
}
