package fr.iolabs.leaf.files;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
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
		return this.fileResponse(this.fileService.getById(id), null);
	}

	@PermitAll
	@CrossOrigin
	@GetMapping("/{id}/{filename:.+}")
	public ResponseEntity<byte[]> findFile(@PathVariable String id, @PathVariable String filename) {
		return this.fileResponse(this.fileService.getById(id), filename);
	}

	private ResponseEntity<byte[]> fileResponse(LeafFileModel file, String filename) {
		ResponseEntity.BodyBuilder response = ResponseEntity.ok().header("Content-Type", file.getContentType());
		if (filename != null) {
			response.header("Content-Disposition", this.inlineContentDisposition(filename, file.getContentType()));
		}
		return response.body(file.getFile());
	}

	/**
	 * Spring MVC remplace tout téléchargement dont l'URL se termine par une extension
	 * hors liste sûre (pdf notamment) par {@code Content-Disposition: inline;filename=f.txt}.
	 * Un en-tête déjà présent est conservé. Le nom reprend le segment d'URL, sans chemin
	 * ni caractère de contrôle, et l'extension est ramenée au type réel du fichier.
	 */
	private String inlineContentDisposition(String filename, String contentType) {
		String safeName = this.safeDownloadFilename(filename, contentType);
		String asciiName = this.asciiFilename(safeName);
		String disposition = "inline; filename=\"" + asciiName + "\"";
		if (!asciiName.equals(safeName)) {
			disposition += "; filename*=UTF-8''" + this.rfc5987(safeName);
		}
		return disposition;
	}

	private String safeDownloadFilename(String filename, String contentType) {
		String name = filename.replace('\\', '/');
		int slash = name.lastIndexOf('/');
		if (slash >= 0) {
			name = name.substring(slash + 1);
		}
		StringBuilder cleaned = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (Character.isISOControl(c) || c == '"' || c == '\\' || c == '\u2028' || c == '\u2029') {
				continue;
			}
			cleaned.append(c);
		}
		name = cleaned.toString().trim();
		if (name.isEmpty() || this.onlyDots(name)) {
			name = "file";
		}
		if (name.length() > 180) {
			name = name.substring(name.length() - 180);
		}
		if (this.extensionMatchesContentType(name, contentType)) {
			return name;
		}
		String base = name;
		int dot = name.lastIndexOf('.');
		if (dot > 0) {
			base = name.substring(0, dot);
		}
		if (base.isBlank() || this.onlyDots(base)) {
			base = "file";
		}
		String extension = this.extensionFor(contentType);
		if (extension == null) {
			return base;
		}
		return base + "." + extension;
	}

	private boolean onlyDots(String value) {
		for (int i = 0; i < value.length(); i++) {
			if (value.charAt(i) != '.') {
				return false;
			}
		}
		return true;
	}

	private boolean extensionMatchesContentType(String filename, String contentType) {
		if (contentType == null || contentType.isBlank()) {
			return false;
		}
		try {
			MediaType actual = MediaType.parseMediaType(contentType);
			return MediaTypeFactory.getMediaType(filename).map(actual::isCompatibleWith).orElse(false);
		} catch (InvalidMediaTypeException ex) {
			return false;
		}
	}

	private String extensionFor(String contentType) {
		if (contentType == null || contentType.isBlank()) {
			return null;
		}
		String type = contentType.toLowerCase(Locale.ROOT);
		int semi = type.indexOf(';');
		if (semi >= 0) {
			type = type.substring(0, semi).trim();
		}
		switch (type) {
		case "application/pdf":
			return "pdf";
		case "image/png":
			return "png";
		case "image/jpeg":
		case "image/jpg":
			return "jpg";
		case "image/gif":
			return "gif";
		case "image/webp":
			return "webp";
		case "image/svg+xml":
			return "svg";
		case "text/plain":
			return "txt";
		case "text/csv":
			return "csv";
		case "application/json":
			return "json";
		case "application/xml":
		case "text/xml":
			return "xml";
		case "application/zip":
			return "zip";
		case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
			return "docx";
		case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
			return "xlsx";
		case "application/msword":
			return "doc";
		case "application/vnd.ms-excel":
			return "xls";
		default:
			return null;
		}
	}

	private String asciiFilename(String name) {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (c >= 0x20 && c < 0x7f) {
				out.append(c);
			} else {
				out.append('_');
			}
		}
		String ascii = out.toString();
		return ascii.isBlank() ? "file" : ascii;
	}

	private String rfc5987(String value) {
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		StringBuilder out = new StringBuilder();
		for (byte valueByte : bytes) {
			int c = valueByte & 0xff;
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '!' || c == '#'
					|| c == '$' || c == '&' || c == '+' || c == '-' || c == '.' || c == '^' || c == '_' || c == '`'
					|| c == '|' || c == '~') {
				out.append((char) c);
			} else {
				out.append(String.format("%%%02X", c));
			}
		}
		return out.toString();
	}

}
