package com.creditapp.shared.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ESignatureProviderConfig {
    
    @Value("${docusign.api-key:#{null}}")
    private String docuSignApiKey;
    
    @Value("${docusign.account-id:#{null}}")
    private String docuSignAccountId;
    
    @Value("${docusign.api-endpoint:https://na3.docusign.net}")
    private String docuSignApiEndpoint;
    
    @Value("${docusign.integration-key:#{null}}")
    private String docuSignIntegrationKey;
    
    public enum Provider {
        DOCUSIGN("DocuSign", "REST API", "https://developers.docusign.com");
        
        private final String displayName;
        private final String apiType;
        private final String docUrl;
        
        Provider(String displayName, String apiType, String docUrl) {
            this.displayName = displayName;
            this.apiType = apiType;
            this.docUrl = docUrl;
        }
        
        public String getDisplayName() { return displayName; }
        public String getApiType() { return apiType; }
        public String getDocUrl() { return docUrl; }
    }
    
    public Provider getSelectedProvider() {
        return Provider.DOCUSIGN;
    }
    
    public boolean isConfigured() {
        return docuSignApiKey != null && !docuSignApiKey.isBlank() &&
               docuSignAccountId != null && !docuSignAccountId.isBlank();
    }
}