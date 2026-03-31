package com.vantryx.api.mapper;

import com.vantryx.api.dto.SaleDTO;
import com.vantryx.api.model.Sale;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface SaleMapper {
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "user.id", target = "userId")
    SaleDTO toDTO(Sale sale);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "user", ignore = true)
    Sale toEntity(SaleDTO dto);
}
