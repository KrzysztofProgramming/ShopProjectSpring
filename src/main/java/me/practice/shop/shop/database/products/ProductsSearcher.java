package me.practice.shop.shop.database.products;

import me.practice.shop.shop.controllers.products.models.GetProductsParams;
import me.practice.shop.shop.database.ParamsApplicator;
import me.practice.shop.shop.models.BookProduct;
import me.practice.shop.shop.utils.ProductsSortUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        StringBuilder mainBuilder = new StringBuilder("SELECT b.book_id FROM ");
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
        mainBuilder.append(" GROUP BY b.book_id");

        Query mainQuery = this.entityManager.createNativeQuery(mainBuilder.toString());
        this.applyParams(mainQuery, params);
        mainQuery.setFirstResult((params.getPageNumber() - 1) * params.getPageSize());
        mainQuery.setMaxResults((params.getPageSize()));
        List<Long> result = ((Stream<BigInteger>) mainQuery.getResultList().stream()).map(BigInteger::longValue)
                .collect(Collectors.toList());
        return this.productsRepository.findAllByIds(result,
                        PageRequest.of(params.getPageNumber() - 1, params.getPageSize())
                                .withSort(ProductsSortUtils.getSort(params.getSort())));
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
                .applyParam("archived", params.getIsArchived())
                .getQuery();
    }

//
//    @Override
//    public boolean allExistByIds(Collection<String> ids) {
//        return this.mongoTemplate.count(Query.query(Criteria.where("id").in(ids)), BookProduct.class) == ids.size();
//    }
//
//    @Override
//    public BulkWriteResult decreaseProductsCounts(Map<String, Integer> productsCounts) {
//        BulkOperations operation = this.mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, BookProduct.class);
//        productsCounts.forEach((key, value) -> operation.updateOne(Query.query(Criteria.where("id").is(key)),
//                new Update().inc("inStock", -value)));
//        return operation.execute();
//    }
//
//    @Override
//    public boolean allProductsAvailable(Map<String, Integer> amounts) {
//        if(amounts.isEmpty()) return false;
//        List<BookProduct> products= this.mongoTemplate.find(Query.query(Criteria.where("id").in(amounts.keySet())),
//                BookProduct.class);
//        return products.stream().allMatch(product->product.getInStock() >= amounts.get(product.getId()));
//    }
//
//
//    private Criteria generatePriceCriteria(GetProductsParams params){
//        return maxMinCriteria("price", params.getMaxPrice(), params.getMinPrice());
//    }
//
//    private Criteria generateAuthorsCriteria(GetProductsParams params){
//        return params.getAuthorsNames().size() == 0 ? null :
//                Criteria.where("authorsNames").elemMatch(new Criteria().in(params.getAuthorsNames()));
//    }
//
//    private Criteria generateStockCriteria(GetProductsParams params){
//        return maxMinCriteria("inStock", params.getMaxInStock(), params.getMinInStock());
//    }
//
//    private Criteria generateTypesCriteria(GetProductsParams params){
//        return params.getTypes().isEmpty() ? null :
//                Criteria.where("types").elemMatch(new Criteria().in(params.getTypes()));
//    }
}
