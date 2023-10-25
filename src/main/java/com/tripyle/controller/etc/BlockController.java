package com.tripyle.controller.etc;

import com.tripyle.common.model.dto.HttpRes;
import com.tripyle.model.dto.etc.BlockReq;
import com.tripyle.model.dto.etc.BlockRes;
import com.tripyle.service.etc.BlockService;
import com.tripyle.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags={"11.Block"})
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/block", produces = MediaType.APPLICATION_JSON_VALUE)
public class BlockController {
    private final BlockService blockService;
    private final UserService userService;

    @ApiOperation(value = "차단하기", notes = "입력받은 유저를 차단합니다.")
    @PostMapping("")
    public HttpRes<String> createBlock(@RequestBody BlockReq.BlockeeIdDto blockeeIdDto, HttpServletRequest httpServletRequest) {
        Long blockerId = userService.getUserId(httpServletRequest);
        blockService.createBlock(blockerId, blockeeIdDto.getBlockeeId());
        return new HttpRes<>("차단이 완료되었습니다.");
    }

    @ApiOperation(value = "차단 목록 조회", notes = "차단 목록을 조회합니다.\n" +
            "로그인 하지 않고 조회 시 401 에러\n로그인 했지만 관리자가 아닐 시에 403 에러")
    @GetMapping("/list")
    public HttpRes<List<BlockRes.BlockDetailDto>> getBlockList(HttpServletRequest httpServletRequest) {
        return new HttpRes<>(blockService.getBlockList());
    }
}
