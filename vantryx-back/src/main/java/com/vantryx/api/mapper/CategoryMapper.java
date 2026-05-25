package com.vantryx.api.mapper;

import com.vantryx.api.dto.CategoryDTO;
import com.vantryx.api.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryDTO toDTO(Category entity) {
        if (entity == null) return null;
        return CategoryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }

    public Category toEntity(CategoryDTO dto) {
        if (dto == null) return null;
        return Category.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
    }
}
