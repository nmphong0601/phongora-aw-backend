package com.aw.auth.runner;

import com.aw.auth.dto.req.HrEmployeeRequest;
import com.aw.auth.dto.res.HrEmployeeResponse;
import com.aw.auth.entity.User;
import com.aw.auth.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final RestTemplate restTemplate;
    private final UserMapper userMapper; // Inject MyBatis Mapper
    private final PasswordEncoder passwordEncoder;

    @Value("${service.hr.url:http://localhost:8088}")
    private String hrServiceUrl;

    @Override
    public void run(String... args) {
        // Sử dụng Mapper để đếm số lượng bản ghi
        if (userMapper.countUsers() == 0 || userMapper.countUsers() == 1) {
            log.info("Bắt đầu khởi tạo dữ liệu mẫu qua MyBatis...");
            seedUser("General Director","AW", "AW", "CEO", "ROLE_GD");
            seedUser("HCM Branch Director", "AW","BR_HCM", "BR_DIR", "ROLE_BR_DIRECTOR");
            seedUser("BO Division Director", "AW","DIV_TECH", "DIV_DIR", "ROLE_DIV_DIRECTOR");
            seedUser("TECH Division Director", "AW","DIV_TECH", "DIV_DIR", "ROLE_DIV_DIRECTOR");
            seedUser("HR Manager", "AW","DEPT_HR", "HR_MGR", "ROLE_HR_MANAGER");
            seedUser("HR Planner", "AW","DEPT_HR", "HR_EXEC", "ROLE_HR_EXEC");
            seedUser("IT Manager", "AW","DEPT_DEV", "IT_MGR", "ROLE_DEPT_MANAGER");
            log.info("Hoàn tất khởi tạo dữ liệu mẫu!");
        }
    }

    private void seedUser(String fullName, String companyCode, String orgUnitCode, String titleCode, String roleName) {
        HrEmployeeRequest hrRequest = new HrEmployeeRequest();
        hrRequest.setFullName(fullName);
        hrRequest.setCompanyCode(companyCode);
        hrRequest.setOrgUnitCode(orgUnitCode);
        hrRequest.setTitleCode(titleCode);

        try {
            String endpoint = hrServiceUrl + "/api/v1/employees/seed";
            ResponseEntity<HrEmployeeResponse> response = restTemplate.postForEntity(
                    endpoint,
                    hrRequest,
                    HrEmployeeResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                HrEmployeeResponse hrEmployeeData = response.getBody();

                // Sinh UUID thủ công cho User trước khi gọi MyBatis
                UUID newUserId = UUID.randomUUID();

                User newUser = new User();
                newUser.setId(newUserId);
                newUser.setUsername(hrEmployeeData.getEmployeeCode());
                newUser.setPassword(passwordEncoder.encode("123456"));
                newUser.setEmployeeId(hrEmployeeData.getId());
                newUser.setIsActive(true);

                // Lưu bằng MyBatis Mapper
                userMapper.insertUser(newUser);

                // Phân quyền
                // Lưu Role bằng MyBatis Mapper
                userMapper.insertUserRole(newUser.getUsername(), roleName);

                log.info("Đã tạo thành công {}: {} liên kết với Employee ID: {}",
                        titleCode, newUser.getUsername(), hrEmployeeData.getId());
            }
        } catch (Exception e) {
            log.error("Lỗi khi gọi HR Service để khởi tạo {}: {}", titleCode, e.getMessage());
        }
    }
}
