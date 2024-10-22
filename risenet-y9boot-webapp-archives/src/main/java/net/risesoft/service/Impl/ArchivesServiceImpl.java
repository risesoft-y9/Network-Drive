package net.risesoft.service.Impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import net.risesoft.entity.Archives;
import net.risesoft.service.ArchivesService;
import net.risesoft.util.TableUtil;

@Service("archivesService")
public class ArchivesServiceImpl implements ArchivesService {

    @Override
    public Map<String, Object> getArchivesFileList() {
        Map<String, Object> map = new HashMap<>(16);

        try {
            map.put("fileList", TableUtil.getTableFieldList(Archives.class));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return map;
    }
}
