package fr.iolabs.leaf.files;

import java.io.IOException;
import java.util.Optional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${leaf.protocol_hostname}")
    private String hostname;

	@Autowired
	private LeafFileRepository fileRepository;

	@Autowired
	private HttpServletRequest request;

	public LeafFileModel store(MultipartFile file) {
		LeafFileModel createdFile;
		try {
			createdFile = LeafFileModel.from(file.getBytes(), file.getContentType(), coreContext.getAccount().getId());
			createdFile = this.fileRepository.insert(createdFile);
			int serverPort = request.getServerPort();
			String hostname = request.getScheme() + "://" + request.getServerName();
			if(serverPort != 80) {
				hostname += ":" + serverPort;
			}
			createdFile.setUrl(hostname + "/api/files/" + createdFile.getId());
			return this.fileRepository.save(createdFile);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new InternalServerErrorException();
		}
	}

	public LeafFileModel store(byte [] file, String contentType, String accountId) {
		LeafFileModel createdFile;
		createdFile = LeafFileModel.from(file, contentType, accountId);
		createdFile = this.fileRepository.insert(createdFile);
		createdFile.setUrl(this.hostname + "/api/files/" + createdFile.getId());
		return this.fileRepository.save(createdFile);
	}

	public LeafFileModel getById(String id) {
		Optional<LeafFileModel> file = this.fileRepository.findById(id);
		if (!file.isPresent()) {
			throw new NotFoundException();
		}
		return file.get();
	}

}
