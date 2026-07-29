package com.intellectual.service;

import com.intellectual.model.vo.SponsorOptionVo;

import java.util.List;

/** 提供可被分配交底的启用主办人，并校验主办人归属。 */
public interface SponsorDirectoryService {

    List<SponsorOptionVo> listActiveSponsors();

    SponsorOptionVo requireActiveSponsor(Long userId);
}
