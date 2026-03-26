package com.vantryx.api.controller;

import com.vantryx.api.service.AIAdvisorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AI Advisor", description = "Consultoría inteligente de inventario")
public class AIAdvisorController {

    private final AIAdvisorService aiAdvisorService;

    public AIAdvisorController(AIAdvisorService aiAdvisorService) {
        this.aiAdvisorService = aiAdvisorService;
    }

    @GetMapping("/analyze/{productId}")
    public ResponseEntity<String> analyzeProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(aiAdvisorService.getProductAnalysis(productId));
    }
}
