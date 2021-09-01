package me.practice.shop.shop.controllers.users.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.practice.shop.shop.models.PageableParams;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
public class GetUsersParams extends PageableParams {
    
}
