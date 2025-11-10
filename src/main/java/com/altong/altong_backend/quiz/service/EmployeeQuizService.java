package com.altong.altong_backend.quiz.service;

import com.altong.altong_backend.employee.model.Employee;
import com.altong.altong_backend.employee.repository.EmployeeRepository;
import com.altong.altong_backend.global.exception.BusinessException;
import com.altong.altong_backend.global.exception.ErrorCode;
import com.altong.altong_backend.global.jwt.JwtTokenProvider;
import com.altong.altong_backend.quiz.dto.request.QuizSubmitRequest;
import com.altong.altong_backend.quiz.dto.response.QuizDetailResponse;
import com.altong.altong_backend.quiz.dto.response.QuizSubmitResponse;
import com.altong.altong_backend.quiz.model.Quiz;
import com.altong.altong_backend.quiz.model.QuizAttempt;
import com.altong.altong_backend.quiz.repository.QuizAttemptRepository;
import com.altong.altong_backend.quiz.repository.QuizRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final QuizAttemptRepository quizAttemptRepository;
    private final JwtTokenProvider jwt;
    private final ObjectMapper objectMapper;

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
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));

        // 알바생의 store ID 확인
        Long employeeStoreId = employee.getStore().getId();

        // 퀴즈 조회
        List<Quiz> quizzes = quizRepository.findByTraining_IdOrderByIdAsc(trainingId);
        if (quizzes.isEmpty()) throw new BusinessException(ErrorCode.QUIZ_NOT_FOUND);

        // 퀴즈가 속한 training의 store ID 가져오기
        Long trainingStoreId = quizzes.get(0).getTraining().getStore().getId();
        if (!employeeStoreId.equals(trainingStoreId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // isCompleted 기준으로 필드 노출 제어
        return quizzes.stream().map(quiz -> {
            QuizAttempt attempt = quizAttemptRepository
                    .findFirstByQuizAndEmployee(quiz, employee)
                    .orElse(null);

            if (attempt != null) {
                // 푼 경우: 정답/오답 포함
                return QuizDetailResponse.fromAttempt(quiz, attempt.getIsCorrect());
            } else {
                // 안 푼 경우: 정답/해설 숨김
                return QuizDetailResponse.forEmployee(quiz);
            }
        }).collect(Collectors.toList());
    }

    // 퀴즈 제출
    @Transactional
    public QuizSubmitResponse submitQuiz(String token, Long trainingId, Long quizId, QuizSubmitRequest request) {
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
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));

        // 퀴즈 조회
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        // trainingId 불일치 체크
        if (!quiz.getTraining().getId().equals(trainingId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 가게 일치 검증
        Long employeeStoreId = employee.getStore().getId();
        Long trainingStoreId = quiz.getTraining().getStore().getId();

        if (!employeeStoreId.equals(trainingStoreId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 이미 완료된 퀴즈 재제출 시 차단
        if (quizAttemptRepository.existsByQuizAndEmployee(quiz, employee)) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_COMPLETED);
        }

        // 선택지 검증
        if (request.getSelectedAnswer() == null || request.getSelectedAnswer().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_ANSWER);
        }

        try {
            List<String> options = objectMapper.readValue(quiz.getOptions(), new TypeReference<List<String>>() {});
            if (!options.contains(request.getSelectedAnswer().trim())) {
                throw new BusinessException(ErrorCode.INVALID_ANSWER);
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_ANSWER);
        }

        boolean isCorrect = quiz.getAnswer().equalsIgnoreCase(request.getSelectedAnswer().trim());

        // 퀴즈 풀이 기록 저장
        quizAttemptRepository.save(QuizAttempt.of(quiz, employee, isCorrect, request.getSelectedAnswer()));

        return QuizSubmitResponse.builder()
                .quizId(quiz.getId())
                .isCorrect(isCorrect)
                .correctAnswer(quiz.getAnswer())
                .explanation(quiz.getExplanation())
                .build();
    }
}