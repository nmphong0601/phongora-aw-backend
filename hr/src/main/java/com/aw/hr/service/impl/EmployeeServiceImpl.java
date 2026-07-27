package com.aw.hr.service.impl;

import com.aw.common.exception.AppException;
import com.aw.common.exception.ErrorCode;
import com.aw.hr.dto.req.CreateSeedEmployeeRequest;
import com.aw.hr.dto.res.CreateSeedEmployeeResponse;
import com.aw.hr.dto.res.EmployeeResponse;
import com.aw.hr.entity.EmployeeEntity;
import com.aw.hr.mapper.EmployeeMapper;
import com.aw.hr.mapper.SequenceMapper;
import com.aw.hr.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final SequenceMapper sequenceMapper;
    private final EmployeeMapper employeeMapper;

    /**
     * Sinh mã nhân viên mới.
     * Propagation.REQUIRES_NEW đảm bảo việc lấy sequence chạy ở một transaction độc lập.
     * Nếu transaction tạo nhân viên chính bị lỗi (rollback), chuỗi số này vẫn được tăng lên,
     * tránh việc bị lock table md_code_sequences quá lâu ảnh hưởng hệ thống.
     */
    // @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Transactional
    public String generateNextEmployeeCode(String companyCode) {
        // 1. Gọi DB để lấy và tăng số thứ tự (Atomic Update)
        Integer nextVal = sequenceMapper.getNextSequence(companyCode);

        // 2. Kiểm tra an toàn dữ liệu
        if (nextVal == null) {
            log.error("Không tìm thấy cấu hình sequence cho company_code: {}", companyCode);
            // throw new RuntimeException("Hệ thống chưa cấu hình Sequence cho công ty: " + companyCode);
            throw new AppException(ErrorCode.EMPLOYEE_NEW_CODE_NOT_GENERATED, companyCode);
        }

        // 3. Format chuỗi: <COMPANY_CODE> + <5 chữ số padding 0>
        // Ví dụ: AW + 00004 -> AW00004
        String employeeCode = companyCode + String.format("%05d", nextVal);

        log.debug("Đã sinh mã nhân viên mới: {}", employeeCode);
        return employeeCode;
    }

    @Override
    @Transactional
    public CreateSeedEmployeeResponse createSeedEmployee(CreateSeedEmployeeRequest request) {
        // 1. Lấy mã nhân viên tự động từ hệ thống
        String newEmployeeCode = generateNextEmployeeCode(request.getCompanyCode());

        // 2. Chuẩn bị Entity lưu vào Database
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(UUID.randomUUID());
        entity.setEmployeeCode(newEmployeeCode);
        entity.setFullName(request.getFullName());
        entity.setOrgUnitCode(request.getOrgUnitCode());
        entity.setTitleCode(request.getTitleCode());

        // 3. Lưu vào database
        employeeMapper.insertSeedEmployee(entity);

        // 4. Trả kết quả về cho client (Auth Service)
        CreateSeedEmployeeResponse response = new CreateSeedEmployeeResponse();
        response.setId(entity.getId());
        response.setEmployeeCode(entity.getEmployeeCode());

        return response;
    }

    @Override
    @Transactional
    public EmployeeResponse findById(UUID id) {
        return employeeMapper.findById(id);
    }

    @Override
    @Transactional
    public EmployeeResponse findByCode(String code) {
        return employeeMapper.findByCode(code);
    }
}
