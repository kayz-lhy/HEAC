package com.kayz.heac.opinion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kayz.heac.opinion.domain.dto.OpinionPostDTO;
import com.kayz.heac.opinion.domain.vo.OpinionVO;
import com.kayz.heac.opinion.entity.Opinion;

public interface OpinionService extends IService<Opinion> {
    OpinionVO postOpinion(OpinionPostDTO dto, String clientIp, String userAgent);
}
