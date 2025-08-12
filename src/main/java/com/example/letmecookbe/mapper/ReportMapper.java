// src/main/java/com/example/letmecookbe/mapper/ReportMapper.java
package com.example.letmecookbe.mapper;

import com.example.letmecookbe.dto.request.ReportRequest;
import com.example.letmecookbe.dto.request.ReportStatusUpdateRequest;
import com.example.letmecookbe.dto.response.ReportResponse;
import com.example.letmecookbe.entity.Account;
import com.example.letmecookbe.entity.Report;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ReportMapper {

    // --- Ánh xạ từ ReportRequest (DTO) sang Report (Entity) ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reporterAccount", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "severity", ignore = true)
    @Mapping(target = "adminResponse", ignore = true)
    @Mapping(target = "resolvedByAdmin", ignore = true)
    @Mapping(target = "resolvedAt", ignore = true)
    @Mapping(target = "reportedAccount", ignore = true)
    @Mapping(target = "evidenceImageUrl", ignore = true) // <-- BỎ QUA NÀY VÌ LÀ MULTIPARTFILE
    Report toEntity(ReportRequest request);

    // --- Ánh xạ từ Report (Entity) sang ReportResponse (DTO) ---
    @Mapping(source = "reporterAccount.username", target = "reporterAccountUsername")
    @Mapping(source = "reporterAccount.email", target = "reporterAccountEmail")
    @Mapping(source = "reportedAccount.username", target = "reportedAccountUsername")
    @Mapping(source = "reportedAccount.email", target = "reportedAccountEmail")
    @Mapping(source = "resolvedByAdmin.username", target = "resolvedByAdminUsername")
    @Mapping(source = "resolvedByAdmin.email", target = "resolvedByAdminEmail")
    @Mapping(source = "severity", target = "severity")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "evidenceImageUrl", target = "evidenceImageUrl") // <-- ÁNH XẠ URL ẢNH
    ReportResponse toDto(Report report);

    // --- Cập nhật Report (Entity) từ ReportStatusUpdateRequest (DTO) ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reporterAccount", ignore = true)
    @Mapping(target = "reportType", ignore = true)
    @Mapping(target = "reportedItemId", ignore = true)
    @Mapping(target = "reason", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "evidenceImageUrl", ignore = true) // <-- BỎ QUA KHI UPDATE STATUS
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "severity", ignore = true)
    @Mapping(target = "resolvedByAdmin", ignore = true)
    @Mapping(target = "resolvedAt", ignore = true)
    @Mapping(target = "reportedAccount", ignore = true)
    @Mapping(source = "newStatus", target = "status")
    void updateReportFromStatusRequest(ReportStatusUpdateRequest request, @MappingTarget Report report);
}