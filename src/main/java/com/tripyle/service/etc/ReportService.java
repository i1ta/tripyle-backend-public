package com.tripyle.service.etc;

import com.tripyle.common.exception.NotFoundException;
import com.tripyle.model.dto.etc.ReportReq;
import com.tripyle.model.dto.etc.ReportRes;
import com.tripyle.model.entity.etc.Report;
import com.tripyle.model.entity.etc.ReportReason;
import com.tripyle.model.entity.user.User;
import com.tripyle.repository.etc.ReportReasonRepository;
import com.tripyle.repository.etc.ReportRepository;
import com.tripyle.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {
    private final ReportRepository reportRepository;
    private final ReportReasonRepository reportReasonRepository;
    private final UserService userService;

    public List<ReportRes.ReasonListDto> getReportReasonList() {
        List<ReportReason> reportReasons = reportReasonRepository.findAll();
        List<ReportRes.ReasonListDto> reasonListDtos = new ArrayList<>();
        for(ReportReason reportReason : reportReasons) {
            reasonListDtos.add(ReportRes.ReasonListDto.builder()
                            .id(reportReason.getId())
                            .reason(reportReason.getContent())
                            .build());
        }
        return reasonListDtos;
    }

    public void createReport(ReportReq.ReportCreateDto reportCreateDto, Long reporterId) {
        User reporter = userService.getUserByUserId(reporterId);
        User reportee = userService.getUserByUserId(reportCreateDto.getReporteeId());
        ReportReason reportReason = getReportReasonById(reportCreateDto.getReportReasonId());

        Report report = Report.builder()
                .reporter(reporter)
                .reportee(reportee)
                .reportReason(reportReason)
                .content(reportCreateDto.getContent())
                .build();
        reportRepository.save(report);
    }

    public List<ReportRes.ReportDetailDto> getReportList() {
        List<Report> reports = reportRepository.findAll();
        List<ReportRes.ReportDetailDto> reportDetailDtos = new ArrayList<>();
        for(Report report : reports) {
            reportDetailDtos.add(ReportRes.ReportDetailDto.builder()
                            .id(report.getId())
                            .reporterUsername(report.getReporter().getUsername())
                            .reporteeUsername(report.getReportee().getUsername())
                            .reportReason(report.getReportReason().getContent())
                            .content(report.getContent())
                            .regDateTime(report.getRegDateTime())
                            .build());
        }
        return reportDetailDtos;
    }

    public ReportRes.ReportDetailDto getReport(Long reportId) {
        Report report = getReportById(reportId);
        return ReportRes.ReportDetailDto.builder()
                .id(report.getId())
                .reporterUsername(report.getReporter().getUsername())
                .reporteeUsername(report.getReportee().getUsername())
                .reportReason(report.getReportReason().getContent())
                .content(report.getContent())
                .regDateTime(report.getRegDateTime())
                .build();
    }

    public ReportReason getReportReasonById(Long reportReasonId) {
        Optional<ReportReason> optionalReportReason = reportReasonRepository.findById(reportReasonId);
        if(optionalReportReason.isEmpty()) {
            throw new NotFoundException("존재하지 않는 신고 이유 ID 값입니다.");
        }
        return optionalReportReason.get();
    }

    public Report getReportById(Long reportId) {
        Optional<Report> optionalReport = reportRepository.findById(reportId);
        if(optionalReport.isEmpty()) {
            throw new NotFoundException("존재하지 않는 신고 ID 값입니다.");
        }
        return optionalReport.get();
    }
}
