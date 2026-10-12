package com.jansetu.backend.audit.api;

import com.jansetu.backend.audit.AdminAuditService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Read-only administrator history. There is intentionally no normal API to alter audit records. */
@RestController
@RequestMapping("/api/v1/admin/audit")
public class AdminAuditController {
    private final AdminAuditService audit;
    public AdminAuditController(AdminAuditService audit) { this.audit = audit; }

    @GetMapping
    public List<AdminAuditResponse> recent() {
        return audit.recent().stream().map(AdminAuditResponse::from).toList();
    }
}
