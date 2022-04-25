package me.practice.shop.shop.database.users;

import me.practice.shop.shop.database.Searcher;

public class UsersSearcherImpl extends Searcher  {

//    @Autowired
//    private MongoTemplate mongoTemplate;
//
//    @Override
//    public Page<ShopUser> findByGetParams(GetUsersParams params) {
//        Query query;
//        if(Strings.isNotEmpty(params.getSearchPhrase())) {
//            query = new TextQuery(params.getSearchPhrase());
//            if(ProductsSortUtils.isEmpty(params.getSort()))
//                ((TextQuery)query).sortByScore();
//        }
//        else{
//            query = new Query();
//        }
//        applyCriteria(query, this.generateRolesCriteria(params));
//        Pageable pageable = PageRequest.of(params.getPageNumber() - 1, params.getPageSize());
//        query.with(pageable);
//        query.with(UsersSortUtils.getSort(params.getSort()));
//        return PageableExecutionUtils.getPage(mongoTemplate.find(query, ShopUser.class), pageable,
//                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), ShopUser.class));
//    }
//
//    @Override
//    public UpdateResult saveUserInfo(String id, UserInfo info) {
//        return this.mongoTemplate.updateFirst(Query.query(Criteria.where("username").is(id)),
//                Update.update("userInfo", info), ShopUser.class);
//    }
//
//    private Criteria generateRolesCriteria(GetUsersParams params){
//        return params.getRolesNames().isEmpty() ? null :
//                Criteria.where("roles").elemMatch(Criteria.where("name").in(params.getRolesNames()));
//    }
}
