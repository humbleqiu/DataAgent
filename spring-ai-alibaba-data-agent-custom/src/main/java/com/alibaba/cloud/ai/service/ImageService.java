package com.alibaba.cloud.ai.service;

import com.alibaba.cloud.ai.dto.image.ImageCandidateDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageService {

    /**
     * 从图片中获取候选物料列表
     * @param image 上传的图片文件
     * @return 候选物料列表
     * @throws IOException IO异常
     */
    List<ImageCandidateDTO> getCandidatesFromImage(MultipartFile image) throws IOException;
}
