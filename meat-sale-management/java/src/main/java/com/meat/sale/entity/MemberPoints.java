package com.meat.sale.entity;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class MemberPoints {
    private Long id; private Long memberId; private Integer pointsChange;
    private String changeType; private Long orderId; private String remark;
    private LocalDateTime createTime;
}
