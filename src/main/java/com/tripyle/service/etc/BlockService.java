package com.tripyle.service.etc;

import com.tripyle.model.dto.etc.BlockRes;
import com.tripyle.model.dto.etc.ReportRes;
import com.tripyle.model.entity.etc.Block;
import com.tripyle.model.entity.user.User;
import com.tripyle.repository.etc.BlockRepository;
import com.tripyle.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BlockService {
    private final BlockRepository blockRepository;
    private final UserService userService;

    public void createBlock(Long blockerId, Long blockeeId) {
        User blocker = userService.getUserByUserId(blockerId);
        User blockee = userService.getUserByUserId(blockeeId);
        Block block = Block.builder()
                .blocker(blocker)
                .blockee(blockee)
                .build();
        blockRepository.save(block);
    }

    public List<BlockRes.BlockDetailDto> getBlockList() {
        List<Block> blocks = blockRepository.findAll();
        List<BlockRes.BlockDetailDto> blockDetailDtos = new ArrayList<>();
        for(Block block : blocks) {
            blockDetailDtos.add(BlockRes.BlockDetailDto.builder()
                    .id(block.getId())
                    .blockerUsername(block.getBlocker().getUsername())
                    .blockeeUsername(block.getBlockee().getUsername())
                    .regDateTime(block.getRegDateTime())
                    .build());
        }
        return blockDetailDtos;
    }
}
