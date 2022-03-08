package me.practice.shop.shop.controllers.users.models.users;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.practice.shop.shop.models.PageableParams;

import java.util.Collection;
import java.util.Collections;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
public class GetUsersParams extends PageableParams {

    private String searchPhrase = "";
    private Collection<String> rolesNames = Collections.emptyList();
    private String sort = "";
}
