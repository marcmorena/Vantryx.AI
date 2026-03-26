package com.vantryx.api.service;

import com.vantryx.api.model.Product;
import com.vantryx.api.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIAdvisorServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    // Mocks para la interfaz fluida de Spring AI
    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;
    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    private AIAdvisorService aiAdvisorService;

    @BeforeEach
    void setUp() {
        // Configuramos el Builder para que devuelva nuestro ChatClient mockeado
        when(chatClientBuilder.build()).thenReturn(chatClient);
        aiAdvisorService = new AIAdvisorService(productRepository, chatClientBuilder);
    }

    @Test
    @DisplayName("Debería generar un análisis de IA basado en los datos del producto")
    void shouldGenerateProductAnalysis() {
        // 1. Datos de prueba
        Product product = new Product();
        product.setId(1L);
        product.setName("Batería de Litio X1");
        product.setCurrentStock(5);
        product.setPurchasePrice(new BigDecimal("100.00"));
        product.setSalePrice(new BigDecimal("150.00"));
        product.setLeadTime(10);

        String mockAIResponse = "Análisis: El margen es del 50%. Riesgo alto por bajo stock.";

        // 2. Configuración de Mocks (Simulando la cadena de Spring AI)
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Mockeamos la cadena: chatClient.prompt().user(...).call().content()
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(mockAIResponse);

        // 3. Ejecución
        String result = aiAdvisorService.getProductAnalysis(1L);

        // 4. Verificaciones
        assertNotNull(result);
        assertEquals(mockAIResponse, result);

        // Verificamos que se llamó a la IA (opcional pero recomendado)
        verify(chatClient).prompt();
        verify(responseSpec).content();
    }

    @Test
    @DisplayName("Debería lanzar excepción si el producto no existe para analizar")
    void shouldThrowExceptionWhenProductNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            aiAdvisorService.getProductAnalysis(99L);
        });
    }
}