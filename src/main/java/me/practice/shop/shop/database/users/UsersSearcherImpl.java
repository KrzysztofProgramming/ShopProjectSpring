package me.practice.shop.shop.database.users;

import com.mongodb.client.result.UpdateResult;
import me.practice.shop.shop.controllers.users.models.users.GetUsersParams;
import me.practice.shop.shop.database.Searcher;
import me.practice.shop.shop.models.ShopUser;
import me.practice.shop.shop.models.UserInfo;
import me.practice.shop.shop.utils.ProductsSortUtils;
import me.practice.shop.shop.utils.UsersSortUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.support.PageableExecutionUtils;

public class UsersSearcherImpl extends Searcher implements UsersSearcher  {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<ShopUser> findByGetParams(GetUsersParams params) {
        Query query;
        if(Strings.isNotEmpty(params.getSearchPhrase())) {
            query = new TextQuery(params.getSearchPhrase());
            if(ProductsSortUtils.isEmpty(params.getSort()))
                ((TextQuery)query).sortByScore();
        }
        else{
            query = new Query();
        }
        applyCriteria(query, this.generateRolesCriteria(params));
        Pageable pageable = PageRequest.of(params.getPageNumber() - 1, params.getPageSize());
        query.with(pageable);
        query.with(UsersSortUtils.getSort(params.getSort()));
        return PageableExecutionUtils.getPage(mongoTemplate.find(query, ShopUser.class), pageable,
                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), ShopUser.class));
    }

    @Override
    public UpdateResult saveUserInfo(String id, UserInfo info) {
        return this.mongoTemplate.updateFirst(Query.query(Criteria.where("username").is(id)),
                Update.update("userInfo", info), ShopUser.class);
    }

    private Criteria generateRolesCriteria(GetUsersParams params){
        return params.getRolesNames().isEmpty() ? null :
                Criteria.where("roles").elemMatch(Criteria.where("name").in(params.getRolesNames()));
    }
}
