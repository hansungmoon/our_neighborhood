package ywphsm.ourneighbor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import ywphsm.ourneighbor.domain.dto.CategoryDTO;

import java.awt.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest
        .WebEnvironment.RANDOM_PORT)
@Transactional
@Rollback(value = true)
class CategoryServiceTest {

    @Autowired
    WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @LocalServerPort
    private int port;

    @Autowired
    CategoryService categoryService;

    @Test
    void 몰라() {
        List<CategoryDTO> all = categoryService.findAll();
        assertThat(all.size()).isEqualTo(24);
    }

    @Test
    @DisplayName("카테고리 등록")
    void save() throws Exception {
        String name = "test";
        Long parentId = 5L;

        CategoryDTO dto = CategoryDTO.builder()
                .name(name)
                .parent_id(parentId)
                .build();

        System.out.println("dto = " + dto);
        String s = new ObjectMapper().writeValueAsString(dto);
        System.out.println("s = " + s);

        String url = "http://localhost:" + port + "/category/add";

        mvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk());

        CategoryDTO findCategory = categoryService.findByName(name);

        assertThat(findCategory.getDepth()).isEqualTo(3L);
        assertThat(findCategory.getParentId()).isEqualTo(5L);

    }

}