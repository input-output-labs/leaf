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

	public LeafFileModel store(MultipartFile file, StringBuffer preUrl) {
		LeafFileModel createdFile;
		try {
			createdFile = LeafFileModel.from(file.getBytes(), file.getContentType(), coreContext.getAccount().getId());
			createdFile = this.fileRepository.insert(createdFile);
			createdFile.setUrl(preUrl.append("/").append(createdFile.getId()).toString());
			return this.fileRepository.save(createdFile);
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
