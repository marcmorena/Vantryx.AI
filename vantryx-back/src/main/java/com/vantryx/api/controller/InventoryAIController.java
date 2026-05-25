package com.vantryx.api.controller;

import com.vantryx.api.dto.StockPredictionDTO;
import com.vantryx.api.service.InventoryAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class InventoryAIController {

    private final InventoryAIService aiService;

    @GetMapping("/predict/{productId}")
    public StockPredictionDTO getPrediction(@PathVariable Long productId) {
        return aiService.predictStockOut(productId);
    }
}
