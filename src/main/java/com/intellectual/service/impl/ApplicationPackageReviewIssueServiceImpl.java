package com.intellectual.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.intellectual.mapper.ApplicationPackageReviewIssueMapper;
import com.intellectual.model.entity.ApplicationPackageReviewIssue;
import com.intellectual.service.ApplicationPackageReviewIssueService;
import org.springframework.stereotype.Service;

@Service
public class ApplicationPackageReviewIssueServiceImpl
        extends ServiceImpl<ApplicationPackageReviewIssueMapper, ApplicationPackageReviewIssue>
        implements ApplicationPackageReviewIssueService {
}
