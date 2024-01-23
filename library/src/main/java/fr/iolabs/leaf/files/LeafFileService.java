package fr.iolabs.leaf.files;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Optional;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.common.errors.InternalServerErrorException;
import fr.iolabs.leaf.common.errors.NotFoundException;

@Service
public class LeafFileService {

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Value("${leaf.apiDomain}")
	private String apiDomain;

	@Value("${leaf.appDomain}")
	private String appDomain;

	@Value("${leaf.filestorage.mode}")
	String mode;

	@Value("${leaf.filestorage.localfolder}")
	String localStorageFolder;

	@Value("${leaf.filestorage.publicfolder}")
	String publicfolder;

	@Autowired
	private AmazonS3 amazonS3;

	@Autowired
	private AWSClientConfig awsClientConfig;

	@Autowired
	private LeafFileRepository fileRepository;

	public LeafFileModel getById(String id) {
		Optional<LeafFileModel> file = this.fileRepository.findById(id);
		if (!file.isPresent()) {
			throw new NotFoundException();
		}
		return file.get();
	}

	public LeafFileModel save(byte[] content, String contentType, String accountId, String filename) {
		return this.save(content, contentType, accountId, filename, new FileSaveOptions());
	}

	public LeafFileModel save(byte[] content, String contentType, String accountId, String filename,
			FileSaveOptions options) {
		switch (this.mode) {
		case "localfolder":
			return this.saveInLocalFolder(content, filename, contentType, accountId, options);
		case "s3":
			return this.saveInS3(content, filename, contentType, accountId, options);
		case "database":
		default:
			return this.saveInDatabase(content, contentType, accountId, options);
		}
	}

	private LeafFileModel saveInDatabase(byte[] content, String contentType, String accountId,
			FileSaveOptions options) {
		LeafFileModel createdFile = LeafFileModel.from(content, contentType, accountId);
		createdFile = this.fileRepository.insert(createdFile);
		String url = this.apiDomain + "/api/files/" + createdFile.getId();
		createdFile.setUrl(this.cleanUrl(url));
		return this.fileRepository.save(createdFile);
	}

	private String cleanUrl(String url) {
		return url.replace("://", "/:&:/").replace("//", "/").replace("/:&:/", "://");
	}

	private LeafFileModel saveInS3(byte[] content, String filename, String contentType, String accountId,
			FileSaveOptions options) {
		if (options.getPublicfolder() == null) {
			options.publicfolder(this.publicfolder);
		}

		LeafFileModel createdFile = null;
		if (filename == null) {
			createdFile = LeafFileModel.from(contentType, accountId);
			createdFile = this.fileRepository.insert(createdFile);
			filename = createdFile.getId();
		}

		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentLength(content.length);
		objectMetadata.setContentType(contentType);
		InputStream targetStream = new ByteArrayInputStream(content);

		amazonS3.putObject(new PutObjectRequest(awsClientConfig.getBucketName(),
				options.getPublicfolder() + "/" + filename, targetStream, objectMetadata));
		String url = this.cleanUrl(this.appDomain + "/" + options.getPublicfolder() + "/" + filename);

		if (createdFile == null) {
			createdFile = LeafFileModel.from(contentType, accountId);
			createdFile = this.fileRepository.insert(createdFile);
			createdFile.setUrl(url);
		} else {
			createdFile.setUrl(url);
			createdFile = this.fileRepository.save(createdFile);
		}

		return createdFile;
	}

	private LeafFileModel saveInLocalFolder(byte[] content, String filename, String contentType, String accountId,
			FileSaveOptions options) {
		if (options.getPublicfolder() == null) {
			options.publicfolder(this.publicfolder);
		}
		LeafFileModel createdFile = null;
		if (filename == null) {
			createdFile = LeafFileModel.from(contentType, accountId);
			createdFile = this.fileRepository.insert(createdFile);
			filename = createdFile.getId();
		}

		try {
			File filelocation = FileUtils.getFile(this.localStorageFolder, options.getPublicfolder(), filename);
			File file = FileUtils.getFile(filelocation);
			if (!Files.exists(filelocation.getParentFile().toPath())) {
				Files.createDirectories(filelocation.getParentFile().toPath());
			}

			FileOutputStream fos = new FileOutputStream(file, false);
			fos.write(content);
			fos.close();

			String url = this.cleanUrl(this.appDomain + "/" + options.getPublicfolder() + "/" + filename);

			if (createdFile == null) {
				createdFile = LeafFileModel.from(contentType, accountId);
				createdFile = this.fileRepository.insert(createdFile);
				createdFile.setUrl(url);
			} else {
				createdFile.setUrl(url);
				createdFile = this.fileRepository.save(createdFile);
			}

			return createdFile;
		} catch (IOException e) {
			e.printStackTrace();
			throw new InternalServerErrorException("Cannot save file.");
		}
	}
}
