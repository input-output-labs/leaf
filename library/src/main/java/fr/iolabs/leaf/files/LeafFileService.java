package fr.iolabs.leaf.files;

import java.io.IOException;
import java.util.Optional;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.common.errors.InternalServerErrorException;
import fr.iolabs.leaf.common.errors.NotFoundException;

@Service
public class LeafFileService {
	
	private static Logger logger = LoggerFactory.getLogger(LeafFileService.class);

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Autowired
	private LeafFileRepository fileRepository;

	public LeafFileModel store(MultipartFile file, StringBuilder preUrl) {
		LeafFileModel createdFile;
		try {
			createdFile = LeafFileModel.from(file.getBytes(), file.getContentType(), coreContext.getAccount().getId());
			createdFile = this.fileRepository.insert(createdFile);
			createdFile.setUrl(preUrl.append("/").append(createdFile.getId()).toString());
			return this.fileRepository.save(createdFile);
		} catch (IOException e) {
			logger.error(e.getMessage());
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
