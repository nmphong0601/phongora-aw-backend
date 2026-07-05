package com.aw.document.service.impl;

import com.aw.document.service.ReportService;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Value("${app.storage.path}")
    private String storagePath;

    public String generateApprovalReport(String proposalId, String approverName) {
        try {
            // 1. Tạo thư mục lưu trữ nếu chưa có
            File dir = new File(storagePath);
            if (!dir.exists()) dir.mkdirs();

            // 2. Load template JasperReport từ thư mục resources
            InputStream reportStream = getClass().getResourceAsStream("/reports/approval_template.jrxml");
            if (reportStream == null) {
                throw new RuntimeException("Không tìm thấy template JasperReport!");
            }
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // 3. Truyền dữ liệu vào (Parameters)
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("PROPOSAL_ID", proposalId);
            parameters.put("APPROVER_NAME", approverName);
            parameters.put("STATUS", "APPROVED");
            // Có thể truyền thêm Logo công ty, ngày tháng...

            // 4. Đổ dữ liệu vào report (Dùng JREmptyDataSource nếu không lặp data dạng bảng)
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

            // 5. Xuất ra file PDF
            String outputPath = storagePath + "Proposal_Approved_" + proposalId + ".pdf";
            JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);

            log.info("Đã tạo file PDF thành công tại: {}", outputPath);
            return outputPath;

        } catch (JRException e) {
            log.error("Lỗi khi tạo PDF từ JasperReport", e);
            throw new RuntimeException("Lỗi sinh tài liệu PDF", e);
        }
    }
}
