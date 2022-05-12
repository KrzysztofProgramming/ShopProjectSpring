package me.practice.shop.shop.database.products;

import lombok.Data;
import me.practice.shop.shop.controllers.products.models.GetProductsParams;
import me.practice.shop.shop.database.ParamsApplicator;
import me.practice.shop.shop.models.BookProduct;
import me.practice.shop.shop.utils.ProductsSortUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Component
public class ProductsSearcher {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ProductsRepository productsRepository;

    private final String FULL_TEXT_QUERY = "( " +
            " ( " +
            "  SELECT b.* FROM book_products_table b " +
            "  WHERE textsearchable_index_col @@ plainto_tsquery('english', :search) " +
            " ) " +
            " UNION " +
            " ( " +
            "  SELECT b.* FROM book_products_table b " +
            "  JOIN books_authors b_a ON b_a.book_id = b.book_id " +
            "  JOIN ( " +
            "   SELECT a.author_id FROM authors_table a " +
            "   WHERE textsearchable_index_col @@ plainto_tsquery('english', :search) " +
            "  ) a ON a.author_id = b_a.author_id " +
            "  GROUP BY b.book_id " +
            " ) " +
            " UNION " +
            " ( " +
            "  SELECT b.* FROM book_products_table b " +
            "  JOIN books_types b_t ON b_t.book_id = b.book_id " +
            "  JOIN ( " +
            "   SELECT t.type_id FROM types_table t " +
            "   WHERE textsearchable_index_col @@ plainto_tsquery('english', :search) " +
            "  ) t ON b_t.type_id = t.type_id " +
            "  GROUP BY b.book_id " +
            " ) " +
            ")";

    @Transactional
    @SuppressWarnings("unchecked")
    public Page<BookProduct> findByParams(GetProductsParams params) {
        StringBuilder mainBuilder = new StringBuilder("SELECT b.book_id, COUNT(*) OVER() as total FROM ");
        if(Strings.isNotEmpty(params.getSearchPhrase()))
            mainBuilder.append(FULL_TEXT_QUERY);
        else
            mainBuilder.append("book_products_table");
        mainBuilder.append(" b");

        if(params.getTypes().size() > 0)
            mainBuilder.append(" LEFT JOIN books_types b_t ON b_t.book_id = b.book_id");
        if(params.getAuthors().size() > 0)
            mainBuilder.append(" LEFT JOIN books_authors b_a ON b_a.book_id = b.book_id");
        mainBuilder.append(" WHERE 1=1");

        if(params.getAuthors().size() > 0)
            mainBuilder.append(" AND b_a.author_id IN :authors");
        if(params.getTypes().size() > 0)
            mainBuilder.append(" AND b_t.type_id IN :types");
        if(params.getIsArchived()==GetProductsParams.ALL_PRODUCTS)
            params.setIsArchived(null);
        if(params.getIsArchived()!=null)
            mainBuilder.append(" AND b.is_archived = :archived");
        if(params.getMaxInStock()!=null)
            mainBuilder.append(" AND b.in_stock <= :maxStock");
        if(params.getMinInStock()!=null)
            mainBuilder.append(" AND b.in_stock >= :minStock");
        if(params.getMaxPrice()!=null)
            mainBuilder.append(" AND b.price <= :maxPrice");
        if(params.getMinPrice()!=null)
            mainBuilder.append(" AND b.price >= :minPrice");
        mainBuilder.append(" GROUP BY b.book_id, b.price, b.name");
        mainBuilder.append(ProductsSortUtils.getSortString(params.getSort()));

        Query mainQuery = this.entityManager.createNativeQuery(mainBuilder.toString());
        this.applyParams(mainQuery, params);
        if(params.getIsArchived()!=null)
            mainQuery.setParameter("archived", params.getIsArchived() == GetProductsParams.ARCHIVED_PRODUCTS);
        mainQuery.setFirstResult((params.getPageNumber() - 1) * params.getPageSize());
        mainQuery.setMaxResults((params.getPageSize()));
        List<SearchQueryResult> result = (List<SearchQueryResult>) mainQuery.getResultList().stream()
                .map(SearchQueryResult::new).collect(Collectors.toList());
        long totalCount = result.size() == 0 ? 0 : result.get(0).getTotal();
        return PageableExecutionUtils.getPage(this.productsRepository.findAllByIds(
                result.stream().map(SearchQueryResult::getId).collect(Collectors.toList()),
                ProductsSortUtils.getSort(params.getSort())),
                PageRequest.of(params.getPageNumber() - 1, params.getPageSize()),
                ()->totalCount);
    }

    public Query applyParams(Query query, GetProductsParams params){
        return new ParamsApplicator(query)
                .applyParam("authors", params.getAuthors())
                .applyParam("types", params.getTypes())
                .applyParam("maxPrice", params.getMaxPrice())
                .applyParam("minPrice", params.getMinPrice())
                .applyParam("minStock", params.getMinInStock())
                .applyParam("maxStock", params.getMaxInStock())
                .applyParam("search", params.getSearchPhrase())
                .getQuery();
    }
}

@Data
class SearchQueryResult{
    private long id;
    private long total;
    public SearchQueryResult(Object object){
        Object[] row = (Object[]) object;
        this.id = ((BigInteger) row[0]).longValue();
        this.total = ((BigInteger) row[1]).longValue();
    }
}
