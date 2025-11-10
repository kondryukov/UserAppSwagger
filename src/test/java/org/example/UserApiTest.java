package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.CreateUserRequest;
import org.example.dto.UpdateUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserApiTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    void createTest() throws Exception {
        var request = new CreateUserRequest("name", "name@mail.ru", 123);

        mvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("name@mail.ru"));
    }

    @Test
    void createUserWithDuplicateEmail() throws Exception {
        var originalRequest = new CreateUserRequest("name", "name@mail.ru", 123);

        mvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(originalRequest)))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("name@mail.ru"));

        var copyRequest = new CreateUserRequest("newName", "name@mail.ru", 321);

        mvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(copyRequest)))
                .andExpect(status().is(409))
                .andExpect(jsonPath("$.detail").value("Email already in use"));


    }

    @Test
    void updateTest() throws Exception {
        var createRequest = new CreateUserRequest("name", "name@mail.ru", 123);
        MvcResult createResult = mvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().is(201))
                .andReturn();

        long id = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        var request = new UpdateUserRequest("newName", "newemail@mail.ru", 1234);
        mvc.perform(put("/users/update/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("newemail@mail.ru"));
    }

    @Test
    void updateNotFound() throws Exception {
        var createRequest = new CreateUserRequest("name", "name@mail.ru", 123);
        mvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().is(201))
                .andReturn();

        var request = new UpdateUserRequest("newName", "newemail@mail.ru", 1234);
        mvc.perform(put("/users/update/{id}", -1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.detail").value("User not found"));
    }

    @Test
    void getListOfUsersTest() throws Exception {
        for (int i = 0; i < 10; i++) {
            var createRequest = new CreateUserRequest("name" + i, i + "name@mail.ru", i + 12);
            mvc.perform(post("/users/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().is(201));
        }

        mvc.perform(get("/users"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$._embedded.userResponseList", hasSize(10)))
                .andExpect(jsonPath("$._embedded.userResponseList[5].email").value("5name@mail.ru"));
    }

    @Test
    void openApiDocsTest() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))

                .andExpect(jsonPath("$.info.title").value("User Service API"))
                .andExpect(jsonPath("$.info.version").value("1.0"))

                .andExpect(jsonPath("$.paths['/users']").exists())
                .andExpect(jsonPath("$.paths['/users/read/{id}']").exists())
                .andExpect(jsonPath("$.paths['/users/create']").exists())
                .andExpect(jsonPath("$.paths['/users/update/{id}']").exists())
                .andExpect(jsonPath("$.paths['/users/delete/{id}']").exists());
    }

    @Test
    void swaggerUiTest() throws Exception {
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
