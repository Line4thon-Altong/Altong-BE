package com.altong.altong_backend.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmployeeUnlinkStoreResponse {

    @Schema(description = "알바 ID", example = "5")
    private Long employeeId;

    @Schema(description = "연동 해제 후 storeId (무조건 null)", example = "null")
    private Long storeId;

    @Schema(description = "처리 메시지", example = "가게 연동이 해제되었습니다.")
    private String message;
}