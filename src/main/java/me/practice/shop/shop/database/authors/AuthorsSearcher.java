package me.practice.shop.shop.database.authors;

import me.practice.shop.shop.controllers.authors.models.GetAuthorsParams;
import me.practice.shop.shop.models.Author;
import org.springframework.data.domain.Page;

public interface AuthorsSearcher {
    Page<Author> findByParams(GetAuthorsParams params);
}
