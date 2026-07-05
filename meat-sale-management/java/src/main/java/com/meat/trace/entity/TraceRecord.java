package com.meat.trace.entity;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class TraceRecord {
    private Long id; private String batchNo; private String nodeType;
    private Long nodeId; private String description; private LocalDateTime createTime;
}
