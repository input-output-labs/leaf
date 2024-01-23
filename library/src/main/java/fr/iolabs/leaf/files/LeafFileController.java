package fr.iolabs.leaf.files;

import java.io.IOException;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.common.errors.InternalServerErrorException;

@RestController
@RequestMapping("/api/files")
public class LeafFileController {
	private static Logger logger = LoggerFactory.getLogger(LeafFileController.class);

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Autowired
	private LeafFileService fileService;

	@CrossOrigin
	@PostMapping
	public LeafFileModel create(@RequestParam("file") MultipartFile file) {
		String contentType = file.getContentType();
		String accountId = this.coreContext.getAccount().getId();
		try {
			return this.fileService.save(file.getBytes(), contentType, accountId, null);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new InternalServerErrorException();
		}
	}

	@PermitAll
	@CrossOrigin
	@GetMapping("/{id}")
	public ResponseEntity<byte[]> findFile(@PathVariable String id) {
		LeafFileModel file = this.fileService.getById(id);
		return ResponseEntity.ok().header("Content-Type", file.getContentType()).body(file.getFile());
	}

}
