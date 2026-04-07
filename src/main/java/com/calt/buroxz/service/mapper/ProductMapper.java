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
    @Mapping(target = "category", source = "category", qualifiedByName = "categoryId")
    ProductDTO toDto(Product s);

    @Named("categoryId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    CategoryDTO toDtoCategoryId(Category category);
}
