package net.risesoft.support.comparator;

import java.util.Comparator;
import java.util.Date;

import net.risesoft.entity.FileNode;

public class CreateTimeComparator implements Comparator<FileNode> {

    private boolean asc = true;

    public CreateTimeComparator() {}

    public CreateTimeComparator(boolean asc) {
        this.asc = asc;
    }

    @Override
    public int compare(FileNode o1, FileNode o2) {
        Date o1CreateTime = o1.getCreateTime();
        Date o2CreateTime = o2.getCreateTime();

        if (o1CreateTime == null) {
            o1CreateTime = o1.getUpdateTime();
        }
        if (o2CreateTime == null) {
            o2CreateTime = o2.getUpdateTime();
        }

        int result = o1CreateTime.compareTo(o2CreateTime);
        return asc ? result : -1 * result;
    }
}
