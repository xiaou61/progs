package com.campus.competition.modules.submission;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "demo-data"})
@TestPropertySource(properties = "campus.storage.submissions-dir=target/test-uploads/submissions")
@Transactional
class SubmissionFileUploadApiTest {

  private static final Path STORAGE_DIRECTORY = Path.of("target", "test-uploads", "submissions");

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() throws IOException {
    if (Files.exists(STORAGE_DIRECTORY)) {
      try (Stream<Path> stream = Files.walk(STORAGE_DIRECTORY)) {
        stream.sorted(Comparator.reverseOrder())
          .forEach(path -> {
            try {
              Files.deleteIfExists(path);
            } catch (IOException exception) {
              throw new RuntimeException(exception);
            }
          });
      }
    }
    Files.createDirectories(STORAGE_DIRECTORY);
  }

  @Test
  void shouldUploadSubmissionFileIntoLocalStorage() throws Exception {
    String token = loginAndGetToken("S20260001", "Abcd1234", "STUDENT");
    MockMultipartFile file = new MockMultipartFile(
      "file",
      "innovation-work.pdf",
      "application/pdf",
      "mock-pdf-content".getBytes()
    );

    MvcResult result = mockMvc.perform(multipart("/api/app/submission-files")
        .file(file)
        .header("Authorization", "Bearer " + token))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.data.fileName").value("innovation-work.pdf"))
      .andExpect(jsonPath("$.data.fileUrl", startsWith("/uploads/submissions/")))
      .andReturn();

    String fileUrl = objectMapper.readTree(result.getResponse().getContentAsString())
      .path("data")
      .path("fileUrl")
      .asText();
    String storedFileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);

    org.junit.jupiter.api.Assertions.assertTrue(Files.exists(STORAGE_DIRECTORY.resolve(storedFileName)));
  }

  private String loginAndGetToken(String studentNo, String password, String roleCode) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/app/auth/login")
        .contentType(APPLICATION_JSON)
        .content("""
          {
            "studentNo": "%s",
            "password": "%s",
            "roleCode": "%s"
          }
          """.formatted(studentNo, password, roleCode)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andReturn();

    JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
    return root.path("data").path("token").asText();
  }
}
