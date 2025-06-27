package link.crychic.smarthome.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OllamaService {
    private static final Logger logger = LoggerFactory.getLogger(OllamaService.class);

    @Value("${ollama.url}")
    private String ollamaUrl;

    @Value("${ollama.model}")
    private String ollamaModel;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public JsonNode generateResponse(String prompt) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", ollamaModel);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                    ollamaUrl,
                    requestEntity,
                    String.class
            );

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                JsonNode responseJson = objectMapper.readTree(responseEntity.getBody());
                String response = responseJson.get("response").asText();
                // 移除回答开头的<think></think>标记, </think>\n\n共计长度需 + 10
                String cleanedResponse = response.substring(response.indexOf("</think>\n\n") + 10).trim();
                // 将清理后的响应解析为JsonNode对象
                try {
                    return objectMapper.readTree(cleanedResponse);
                } catch (Exception e) {
                    // 如果无法解析为JSON，则创建一个包含文本响应的JsonNode
                    ObjectNode resultNode = objectMapper.createObjectNode();
                    resultNode.put("text", cleanedResponse);
                    return resultNode;
                }
            } else {
                logger.error("Failed to get response from OLLAMA model: {}", responseEntity);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error calling OLLAMA model", e);
            return null;
        }
    }
}
