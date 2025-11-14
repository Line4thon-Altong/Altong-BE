package com.altong.altong_backend.manual.service;

import com.altong.altong_backend.employee.model.Employee;
import com.altong.altong_backend.employee.repository.EmployeeRepository;
import com.altong.altong_backend.global.exception.BusinessException;
import com.altong.altong_backend.global.exception.ErrorCode;
import com.altong.altong_backend.global.jwt.JwtTokenProvider;
import com.altong.altong_backend.manual.dto.response.ManualDetailResponse;
import com.altong.altong_backend.manual.model.Manual;
import com.altong.altong_backend.manual.repository.ManualRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeManualService {

    private final ManualRepository manualRepository;
    private final EmployeeRepository employeeRepository;
    private final JwtTokenProvider jwt;

    // 알바생용 메뉴얼 조회
    @Transactional(readOnly = true)
    public ManualDetailResponse getManualByTrainingId(String token, Long trainingId) {
        // JWT 파싱
        String accessToken = token.replace("Bearer ", "");
        Claims claims;
        try {
            claims = jwt.parse(accessToken).getBody();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String subject = claims.getSubject();
        if (!subject.startsWith("EMPLOYEE:")) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ROLE);
        }
        Long employeeId = Long.parseLong(subject.substring(9));

        // 메뉴얼 조회
        Manual manual = manualRepository.findByTraining_Id(trainingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MANUAL_NOT_FOUND));

        // 알바생의 소속 가게와 메뉴얼의 가게 일치 여부 확인
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));

        Long employeeStoreId = employee.getStore().getId();
        Long trainingStoreId = manual.getTraining().getStore().getId();

        if (!employeeStoreId.equals(trainingStoreId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        String cardnewsUrl = null;
        if (manual.getTraining().getCardNews() != null) {
            cardnewsUrl = manual.getTraining().getCardNews().getImageUrl();
        }

        // 알바생은 접근 제한 없이 동일 내용 열람 가능
        return ManualDetailResponse.from(manual,cardnewsUrl);
    }
}