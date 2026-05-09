package com.cpt202.projectselection.frontend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StaticAssetSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void vendorAssetsAreAvailableWithoutLogin() throws Exception {
        mockMvc.perform(get("/vendor/echarts/echarts.min.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("echarts")));
    }
}
