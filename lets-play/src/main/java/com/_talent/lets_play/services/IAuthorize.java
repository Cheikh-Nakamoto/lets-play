package com._talent.lets_play.services;

import com._talent.lets_play.models.Product;
import com._talent.lets_play.models.UserPrincipal;

public interface IAuthorize {
    boolean canModifyProductById(String productId, UserPrincipal user);

    boolean canModifyProduct(Product product, UserPrincipal user);
}
