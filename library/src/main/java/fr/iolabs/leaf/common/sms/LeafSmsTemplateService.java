package fr.iolabs.leaf.common.sms;

import fr.iolabs.leaf.common.sms.model.LeafSmsTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
public class LeafSmsTemplateService {

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LeafSmsTemplateRepository smsTemplateRepository;

    public String renderSmsMessage(String templateId, String language, Map<String, Object> variables) {
        LeafSmsTemplate smsTemplate = this.smsTemplateRepository.findById(templateId).orElse(null);
        if (smsTemplate == null) {
            throw new IllegalArgumentException("Template not found for ID: " + templateId);
        }

        String templateContent = smsTemplate.getContentPerLanguage().get(language);
        if (templateContent == null) {
            throw new IllegalArgumentException("Template content not found for language: " + language);
        }

        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateContent, context);
    }
}
