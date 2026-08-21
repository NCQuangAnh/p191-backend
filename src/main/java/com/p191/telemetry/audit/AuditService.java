package com.p191.telemetry.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

@Service
public class AuditService {
    private final AuditEventRepository repo;
    public AuditService(AuditEventRepository repo) { this.repo = repo; }

    /** REQUIRES_NEW: audit commit độc lập → login FAIL rollback vẫn giữ được bản ghi. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String actor, AuditAction action, String target, String detail, String ip) {
        repo.save(new AuditEvent(actor, action, target, detail, ip));
    }
}