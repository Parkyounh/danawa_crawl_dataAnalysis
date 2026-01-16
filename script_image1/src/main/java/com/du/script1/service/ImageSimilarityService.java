package com.du.script1.service;

import com.du.script1.domain.danawaNewDb.NewProducts;
import com.du.script1.repository.danawaNewDb.NewProductsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageSimilarityService {

    @Value("${python.path:python}")
    private String pythonPath;

    @Value("${similarity.script.path:script_image1/scripts/similarity_search.py}")
    private String scriptPath;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final NewProductsRepository newProductsRepository; // 신규 리포지토리로 변경

    // 파라미터 타입을 Map으로 변경하여 Controller와 일치시킴
    public Map<String, Object> searchSimilarImages(String productId, int topN, List<String> filters) {
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            List<String> command = new ArrayList<>();
            command.add(pythonPath);
            command.add(scriptPath);
            command.add("--id");
            command.add(productId);
            command.add("--top");
            command.add(String.valueOf(topN));

            if (filters != null && !filters.isEmpty()) {
                command.add("--filters");

                // [기존 코드]
                // String filterJson = objectMapper.writeValueAsString(filters); // ["123", "456"] 형태로 전달됨 -> Python에서 에러 유발 가능성 높음

                // [수정 코드]
                // 리스트를 "76332956,78848744,20017292" 처럼 콤마로 연결된 문자열로 변환
                String filterString = String.join(",", filters);
                command.add(filterString);

                log.info("Python으로 보낼 필터 문자열: {}", filterString);
            }

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(false);
            Process process = pb.start();

            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    log.error("실제 Python 에러 내용: " + errorLine);
                }
            } catch (Exception e) {
                log.warn("에러 스트림 읽기 중 오류 발생: " + e.getMessage());
            }

            // Python 출력 읽기
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitCode = process.waitFor();
            String jsonOutput = output.toString().trim();

            // JSON 결과 파싱 (디버그 메시지 방어 로직)
            int jsonStart = jsonOutput.indexOf("{");
            if (jsonStart < 0) {
                result.put("success", false);
                result.put("error", "Python 출력 형식이 올바르지 않습니다: " + jsonOutput);
                return result;
            }

            jsonOutput = jsonOutput.substring(jsonStart);
            JsonNode jsonNode = objectMapper.readTree(jsonOutput);

            if (jsonNode.has("success") && jsonNode.get("success").asBoolean()) {
                result.put("success", true);
                List<Map<String, Object>> similarImages = new ArrayList<>();
                JsonNode imagesNode = jsonNode.get("similar_images");

                if (imagesNode != null && imagesNode.isArray()) {
                    for (JsonNode img : imagesNode) {
                        if (img.get("similarity").asDouble() >= 0.999) continue; // 자기 자신 제외

                        Map<String, Object> imageInfo = new LinkedHashMap<>();
                        String pId = img.get("product_id").asText();
                        String cleanPcode = pId.replaceAll("[^0-9]", "");

                        // NewProductsRepository를 사용하여 신규 DB 정보 조회
                        Optional<NewProducts> productOpt = newProductsRepository.findByProductCode(cleanPcode);
                        if (productOpt.isPresent()) {
                            NewProducts p = productOpt.get();
                            imageInfo.put("productCode", p.getProductCode());
                            imageInfo.put("productName", p.getName());
                            imageInfo.put("specifications", p.getSpecifications());
                            imageInfo.put("imageUrl", "http://localhost:8083/danawa_new_db_image/" + p.getCategoryId() + "/" + p.getProductCode() + ".jpg");
                        }
                        imageInfo.put("similarity", img.get("similarity").asDouble());
                        similarImages.add(imageInfo);
                    }
                }
                result.put("similarImages", similarImages);
            }
        } catch (Exception e) {
            log.error("유사 검색 중 예외 발생", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }
}