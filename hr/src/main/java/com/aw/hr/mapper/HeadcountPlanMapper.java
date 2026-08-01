package com.aw.hr.mapper;

import com.aw.hr.entity.HeadcountPlanEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface HeadcountPlanMapper {

    // Khởi tạo bản nháp Kế hoạch định biên
    void insertHeadcountPlan(HeadcountPlanEntity plan);

    // Truy vấn chi tiết theo ID
    HeadcountPlanEntity findById(@Param("id") UUID id);

    // Cập nhật thông tin (Chỉ áp dụng khi ở trạng thái DRAFT hoặc RETURNED)
    void updateHeadcountPlan(HeadcountPlanEntity plan);

    // Cập nhật riêng trạng thái và ID luồng duyệt (Dùng khi submit luồng duyệt)
    void updateWorkflowStatus(
            @Param("id") UUID id,
            @Param("status") String status,
            @Param("workflowInstanceId") UUID workflowInstanceId
    );

    // Cập nhật số lượng nhân sự hiện hành (Gọi sau khi Onboarding thành công)
    void updateCurrentCount(
            @Param("id") UUID id,
            @Param("currentCount") Integer currentCount
    );

    // Lấy danh sách định biên theo Phòng ban và Năm (Phục vụ Validate)
    List<HeadcountPlanEntity> findByDepartmentAndYear(
            @Param("departmentId") UUID departmentId,
            @Param("planYear") Integer planYear
    );

    // Xóa định biên (Thường chỉ cho phép xóa nếu đang là DRAFT)
    void deleteById(@Param("id") UUID id);

    // Thêm method update workflow_instance_id
    void updateWorkflowInstanceId(@Param("id") UUID id, @Param("workflowInstanceId") UUID workflowInstanceId);
}