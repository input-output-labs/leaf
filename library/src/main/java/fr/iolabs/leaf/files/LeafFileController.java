package fr.iolabs.leaf.files;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fr.iolabs.leaf.common.annotations.AdminOnly;

@RestController
@RequestMapping("/api/files")
public class LeafFileController {

    @Autowired
    private LeafFileService fileService;

    @Autowired
    private HttpServletRequest request;

    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST)
    public LeafFileModel create(@RequestParam("file") MultipartFile file) {
        return this.fileService.store(file, request.getRequestURL());
    }

    @PermitAll
    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    public ResponseEntity<byte[]> findTeam(@PathVariable String id) {
        LeafFileModel file = this.fileService.getById(id);
        return ResponseEntity.ok().header("Content-Type", file.getContentType()).body(file.getFile());
    }

}
