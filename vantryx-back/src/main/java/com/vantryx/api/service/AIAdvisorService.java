package com.vantryx.api.service;

import com.vantryx.api.model.Product;
import com.vantryx.api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AIAdvisorService {

    private final ProductRepository productRepository;
    private final ChatClient chatClient;

    // Constructor para inyectar el ChatClient.Builder (estilo Spring AI moderno)
    public AIAdvisorService(ProductRepository productRepository, ChatClient.Builder chatClientBuilder) {
        this.productRepository = productRepository;
        this.chatClient = chatClientBuilder.build();
    }

    public String getProductAnalysis(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // El "Contexto" es lo que hace a esta IA inteligente
        String userPrompt = String.format(
                "Analiza este producto de mi inventario Vantryx:\n" +
                        "- Nombre: %s\n" +
                        "- Stock actual: %d\n" +
                        "- Precio de compra: %s\n" +
                        "- Precio de venta: %s\n" +
                        "- Tiempo de reposición: %d días\n\n" +
                        "Responde de forma profesional y concisa: \n" +
                        "1. ¿Cuál es el margen de beneficio porcentual?\n" +
                        "2. ¿Qué riesgo ves con ese stock y tiempo de entrega?\n" +
                        "3. Una recomendación de acción inmediata.",
                product.getName(), product.getCurrentStock(),
                product.getPurchasePrice(), product.getSalePrice(), product.getLeadTime()
        );

        return chatClient.prompt()
                .user(userPrompt)
                .call()
                .content();
    }
}
