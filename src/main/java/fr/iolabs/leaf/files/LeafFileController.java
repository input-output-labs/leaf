package fr.iolabs.leaf.files;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import fr.iolabs.leaf.common.annotations.AdminOnly;

public class LeafFileController {

    @Autowired
    private LeafFileService fileService;

    @CrossOrigin
    @AdminOnly
    @RequestMapping(method = RequestMethod.POST, path = "/files")
    public LeafFileModel create(@RequestParam("file") MultipartFile file) {
        return this.fileService.store(file);
    }

    @PermitAll
    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, path = "/files/{id}")
    public ResponseEntity<byte[]> findTeam(@PathVariable String id) {
        LeafFileModel file = this.fileService.getById(id);
        return ResponseEntity.ok().header("Content-Type", file.getContentType()).body(file.getFile());
    }

}
