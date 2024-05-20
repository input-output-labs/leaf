package fr.iolabs.leaf.common.sms;

import fr.iolabs.leaf.common.annotations.AdminOnly;
import fr.iolabs.leaf.common.sms.model.LeafSmsTemplate;
import fr.iolabs.leaf.common.sms.model.SmsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/messaging")
public class LeafSmsController {

    @Autowired
    private LeafSmsTemplateService smsTemplateService;

    @Autowired
    private LeafSmsTemplateRepository smsTemplateRepository;

    @AdminOnly
    @CrossOrigin
    @PostMapping("/templates/{id}/render")
    public ResponseEntity<String> generateSmsFromTemplate(@PathVariable String id, @RequestBody SmsRequest smsRequest) {
        if (smsRequest == null || id == null || smsRequest.getLanguage() == null || smsRequest.getPayload() == null) {
            return ResponseEntity.badRequest().body("Request missing required data !");
        }
        if (!smsTemplateRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("template with id " + id + " is not found");
        }

        return ResponseEntity.ok(this.smsTemplateService.renderSmsMessage(id, smsRequest.getLanguage().toLowerCase(), smsRequest.getPayload()));
    }

    @AdminOnly
    @CrossOrigin
    @GetMapping("/templates")
    public ResponseEntity<List<LeafSmsTemplate>> getAllTemplates() {
        List<LeafSmsTemplate> templates = this.smsTemplateRepository.findAll();
        return ResponseEntity.ok(templates);
    }

    @AdminOnly
    @CrossOrigin
    @GetMapping("/templates/{id}")
    public ResponseEntity<?> getTemplateById(@PathVariable String id) {
        Optional<LeafSmsTemplate> optionalTemplate = this.smsTemplateRepository.findById(id);
        return optionalTemplate.isPresent() ? ResponseEntity.ok(optionalTemplate.get()) : ResponseEntity.status(HttpStatus.NOT_FOUND).body("template with ID " + id + " is not found");
    }

    @AdminOnly
    @CrossOrigin
    @PostMapping("/templates")
    public ResponseEntity<String> generateSMSTemplate(@RequestBody LeafSmsTemplate smsTemplate) {
        if (smsTemplate == null || smsTemplate.getId() == null ||  smsTemplate.getContentPerLanguage() == null || smsTemplate.getContentPerLanguage().isEmpty()) {
            return ResponseEntity.badRequest().body("Request missing required data !");
        }
        if (this.smsTemplateRepository.existsById(smsTemplate.getId())) {
            return ResponseEntity.badRequest().body("SMS template " + smsTemplate.getId() + " already exists");
        }
        smsTemplate.setMetadata(smsTemplate.getMetadata());
        LeafSmsTemplate savedTemplate = this.smsTemplateRepository.save(smsTemplate);

        return savedTemplate.getId() != null ? ResponseEntity.ok(savedTemplate.getId()) : ResponseEntity.internalServerError().body("Failed to save SMS template");
    }
}
