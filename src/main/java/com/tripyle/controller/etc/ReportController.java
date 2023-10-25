package com.tripyle.controller.etc;

import com.tripyle.common.model.dto.HttpRes;
import com.tripyle.model.dto.etc.ReportReq;
import com.tripyle.model.dto.etc.ReportRes;
import com.tripyle.service.etc.ReportService;
import com.tripyle.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags={"10.Report"})
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/report", produces = MediaType.APPLICATION_JSON_VALUE)
public class ReportController {
    private final ReportService reportService;
    private final UserService userService;

    @ApiOperation(value = "신고 이유 목록 조회", notes = "신고 이유 목록을 조회합니다.")
    @GetMapping("/reason")
    public HttpRes<List<ReportRes.ReasonListDto>> getReportReasonList(HttpServletRequest httpServletRequest) {
        return new HttpRes<>(reportService.getReportReasonList());
    }

    @ApiOperation(value = "신고하기", notes = "신고 게시물을 작성합니다.")
    @PostMapping("")
    public HttpRes<String> createReport(@RequestBody ReportReq.ReportCreateDto reportCreateDto, HttpServletRequest httpServletRequest) {
        Long reporterId = userService.getUserId(httpServletRequest);
        reportService.createReport(reportCreateDto, reporterId);
        return new HttpRes<>("신고가 접수되었습니다.");
    }

    @ApiOperation(value = "신고 목록 조회", notes = "신고 게시물 목록을 조회합니다.\n" +
            "로그인 하지 않고 조회 시 401 에러\n로그인 했지만 관리자가 아닐 시에 403 에러")
    @GetMapping("/list")
    public HttpRes<List<ReportRes.ReportDetailDto>> getReportList(HttpServletRequest httpServletRequest) {
        return new HttpRes<>(reportService.getReportList());
    }

    @ApiOperation(value = "신고 상세 조회", notes = "신고 게시물을 상세 조회합니다.\n" +
            "로그인 하지 않고 조회 시 401 에러\n로그인 했지만 관리자가 아닐 시에 403 에러")
    @GetMapping("/{reportId}")
    public HttpRes<ReportRes.ReportDetailDto> getReport(@PathVariable Long reportId, HttpServletRequest httpServletRequest) {
        return new HttpRes<>(reportService.getReport(reportId));
    }
}
