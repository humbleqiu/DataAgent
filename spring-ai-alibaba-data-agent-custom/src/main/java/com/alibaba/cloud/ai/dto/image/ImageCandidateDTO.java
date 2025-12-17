package com.alibaba.cloud.ai.dto.image;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageCandidateDTO {
    /**
     * 物料id
     */
    private String material_id;

    /**
     * 分数
     */
    private Double score;

    /**
     * 图片名称
     */
    private String img_name;

    /**
     * id
     */
    private Long doc_id;

    /**
     * 图片地址
     */
    private String url;
}
