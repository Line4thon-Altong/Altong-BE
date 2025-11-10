package com.altong.altong_backend.quiz.service;

import com.altong.altong_backend.employee.model.Employee;
import com.altong.altong_backend.employee.repository.EmployeeRepository;
import com.altong.altong_backend.global.exception.BusinessException;
import com.altong.altong_backend.global.exception.ErrorCode;
import com.altong.altong_backend.global.jwt.JwtTokenProvider;
import com.altong.altong_backend.quiz.dto.response.QuizDetailResponse;
import com.altong.altong_backend.quiz.model.Quiz;
import com.altong.altong_backend.quiz.repository.QuizRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeQuizService {

    private final QuizRepository quizRepository;
    private final EmployeeRepository employeeRepository;
    private final JwtTokenProvider jwt;

    // 알바생용 퀴즈 조회
    @Transactional(readOnly = true)
    public List<QuizDetailResponse> getQuizByTrainingId(String token, Long trainingId) {
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

        // 퀴즈 조회
        List<Quiz> quizzes = quizRepository.findByTraining_Id(trainingId);
        if (quizzes.isEmpty()) {
            throw new BusinessException(ErrorCode.QUIZ_NOT_FOUND);
        }

        // 퀴즈가 속한 training의 store ID 가져오기
        Long trainingStoreId = quizzes.get(0).getTraining().getStore().getId();

        // 알바생의 store ID 확인
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));
        Long employeeStoreId = employee.getStore().getId();

        if (!employeeStoreId.equals(trainingStoreId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // isCompleted 기준으로 필드 노출 제어
        return quizzes.stream()
                .map(quiz -> quiz.getIsCompleted()
                        ? QuizDetailResponse.from(quiz)     // 푼 경우 → 정답, 해설 포함
                        : QuizDetailResponse.forEmployee(quiz)) // 안 푼 경우 → 숨김
                .collect(Collectors.toList());
    }
}