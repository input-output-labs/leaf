package fr.iolabs.leaf.files;

import javax.annotation.security.PermitAll;

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

@RestController
@RequestMapping("/api/files")
public class LeafFileController {

    @Autowired
    private LeafFileService fileService;

    @CrossOrigin
    @PostMapping
    public LeafFileModel create(@RequestParam("file") MultipartFile file) {
        return this.fileService.store(file);
    }

    @PermitAll
    @CrossOrigin
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> findFile(@PathVariable String id) {
        LeafFileModel file = this.fileService.getById(id);
        return ResponseEntity.ok().header("Content-Type", file.getContentType()).body(file.getFile());
    }

}
