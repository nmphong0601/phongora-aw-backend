package com.aw.document.consumer;

import com.aw.common.event.ProposalEvent;
import com.aw.document.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class WorkflowConsumer {

    private final ReportService reportService;

    @KafkaListener(topics = "workflow-events", groupId = "document-group")
    public void handleApproved(ProposalEvent event) {
        log.info("Nhận được event phê duyệt thành công cho Proposal: {}", event.getProposalId());

        // Trong thực tế, bạn có thể gọi REST API sang MasterDataService
        // để lấy thêm thông tin chi tiết (ví dụ: tên người duyệt, nội dung) dựa vào proposalId.
        String approverName = "Ban Giám Đốc"; // Dummy data

        // Gọi JasperReport để xuất PDF
        String pdfFilePath = reportService.generateApprovalReport(event.getProposalId(), approverName);

        // Sau đó có thể update URL của file PDF này vào Database của DocumentService
        // để Frontend hiển thị cho người dùng tải về.
    }
}
