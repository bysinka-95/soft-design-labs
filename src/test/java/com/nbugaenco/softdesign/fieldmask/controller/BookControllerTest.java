package com.nbugaenco.softdesign.fieldmask.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultHandler;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class BookControllerTest {

  @Autowired
  private MockMvc mvc;

  private static final ResultHandler LOG_RESPONSE = result -> log.info("Response {}: {}",
      result.getResponse().getStatus(), result.getResponse().getContentAsString());

  @Test
  void returnsOnlyRequestedFields() throws Exception {
    mvc.perform(get("/api/books").param("title", "Dune").param("fields", "title,rating"))
        .andDo(LOG_RESPONSE)
        .andExpect(status().isOk())
        .andExpect(content().json("[{\"id\":1,\"title\":\"Dune\",\"rating\":4.5}]", JsonCompareMode.STRICT));
  }

  @Test
  void rejectsUnknownField() throws Exception {
    mvc.perform(get("/api/books").param("fields", "price"))
        .andDo(LOG_RESPONSE)
        .andExpect(status().isBadRequest());
  }
}
