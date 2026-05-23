package com.calt.buroxz.service.mapper;

import com.calt.buroxz.domain.Category;
import com.calt.buroxz.domain.Product;
import com.calt.buroxz.service.dto.CategoryDTO;
import com.calt.buroxz.service.dto.ProductDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Product} and its DTO {@link ProductDTO}.
 */
@Mapper(componentModel = "spring")
public interface ProductMapper extends EntityMapper<ProductDTO, Product> {
    @Mapping(target = "category", source = "category", qualifiedByName = "categoryName")
    ProductDTO toDto(Product s);

    @Named("categoryName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    CategoryDTO toDtoCategoryName(Category category);
}
