package com.alibaba.cloud.ai.service.impl;

import com.alibaba.cloud.ai.dto.image.ImageCandidateDTO;
import com.alibaba.cloud.ai.service.ImageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<ImageCandidateDTO> getCandidatesFromImage(MultipartFile image) throws IOException {
        byte[] imageBytes = image.getBytes();
        return callPythonService(imageBytes, 5, true);
    }

    private List<ImageCandidateDTO> callPythonService(byte[] imageBytes, int topK, boolean useRembg) throws IOException {
        // 构建 URL 并添加查询参数
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("192.168.100.218")
                .port(8000)
                .addPathSegment("match")
                .addQueryParameter("top_k", String.valueOf(topK))
                .addQueryParameter("use_rembg", String.valueOf(useRembg))
                .build();

        // 创建 multipart 请求体
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "image.jpg",
                        RequestBody.create(imageBytes, MediaType.parse("application/octet-stream")))
                .build();

        // 构建请求
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        // 执行请求并解析响应
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String jsonData = response.body().string();

            // 解析 JSON 响应，提取 candidates 字段
            Map<String, List<ImageCandidateDTO>> resultMap = objectMapper.readValue(jsonData,
                new TypeReference<Map<String, List<ImageCandidateDTO>>>() {});

            return resultMap.getOrDefault("candidates", Collections.emptyList());
        }
    }
}
