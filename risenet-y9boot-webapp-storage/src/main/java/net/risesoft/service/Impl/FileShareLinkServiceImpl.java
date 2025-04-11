package net.risesoft.service.Impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.FileShareLink;
import net.risesoft.repository.FileShareLinkRepository;
import net.risesoft.service.FileShareLinkService;

@RequiredArgsConstructor
@Service
public class FileShareLinkServiceImpl implements FileShareLinkService {

    private final FileShareLinkRepository fileShareLinkRepository;

    @Override
    public FileShareLink findByLinkKey(String linkKey) {
        return fileShareLinkRepository.findByLinkKey(linkKey);
    }

    @Override
    public FileShareLink save(FileShareLink fileShareLink) {
        return fileShareLinkRepository.save(fileShareLink);
    }
}
