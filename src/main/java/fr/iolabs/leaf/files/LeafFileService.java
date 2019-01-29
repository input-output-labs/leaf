package fr.iolabs.leaf.files;

import java.io.IOException;
import java.util.Optional;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.common.errors.InternalServerErrorException;
import fr.iolabs.leaf.common.errors.NotFoundException;

@Service
public class LeafFileService {

    @Resource(name = "coreContext")
    private LeafContext coreContext;

    @Autowired
    private LeafFileRepository fileRepository;

    public LeafFileModel store(MultipartFile file) {
        LeafFileModel createdFile;
        try {
            createdFile = LeafFileModel.from(file.getBytes(), file.getContentType(), coreContext.getAccount().getId());
            return this.fileRepository.insert(createdFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw new InternalServerErrorException();
        }
    }

    public LeafFileModel getById(String id) {
        Optional<LeafFileModel> file = this.fileRepository.findById(id);
        if (!file.isPresent()) {
            throw new NotFoundException();
        }
        return file.get();
    }

}
