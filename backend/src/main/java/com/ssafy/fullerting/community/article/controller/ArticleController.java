package com.ssafy.fullerting.community.article.controller;

import com.ssafy.fullerting.community.article.model.dto.request.RegistArticleRequest;
import com.ssafy.fullerting.community.article.model.dto.request.UpdateArticleRequest;
import com.ssafy.fullerting.community.article.model.dto.response.ArticleAllResponse;
import com.ssafy.fullerting.community.article.model.dto.response.ArticleResponse;
import com.ssafy.fullerting.community.article.model.entity.Article;
import com.ssafy.fullerting.community.article.service.ArticleService;
import com.ssafy.fullerting.global.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1/articles")
public class ArticleController {
    private final ArticleService articleService;

    @PostMapping("")
    public ResponseEntity<MessageUtils> registarticle(@RequestPart("RegistArticleRequest") RegistArticleRequest registArticleRequest,
                                                      @RequestPart("images") List<MultipartFile> files
    ) {
        articleService.registerticle(registArticleRequest, files);

        return ResponseEntity.ok(MessageUtils.success());
    }

    @PatchMapping("{article_id}")
    public ResponseEntity<MessageUtils> updatearticle(@PathVariable Long article_id,
                                                      @RequestPart("RegistArticleRequest") UpdateArticleRequest updateArticleRequest,
                                                      @RequestPart("images") List<MultipartFile> files) {
        ArticleResponse article = articleService.update(updateArticleRequest, article_id, files);
        return ResponseEntity.ok(MessageUtils.success(article));

    }

    @GetMapping("{article_id}")
    public ResponseEntity<MessageUtils> findarticlebyid(@PathVariable Long article_id
    ) {
        ArticleResponse articleResponse = articleService.findarticlebyid(article_id);

        return ResponseEntity.ok(MessageUtils.success(articleResponse));
    }

    @GetMapping("/all")
    public ResponseEntity<MessageUtils> findAllArticle(
    ) {
        List<ArticleAllResponse> articleResponse = articleService.findAllArticle();
        log.info("allarticle" + articleResponse);
        return ResponseEntity.ok(MessageUtils.success(articleResponse));
    }

    @GetMapping("/category/{keyword}")
    public ResponseEntity<MessageUtils> findAllArticlebyCategory(
            @PathVariable String keyword) {
        List<ArticleResponse> articleResponse = articleService.findAllArticlebyCategory(keyword);
        return ResponseEntity.ok(MessageUtils.success(articleResponse));
    }

    @GetMapping("/search/{keyword}")
    public ResponseEntity<MessageUtils> search(
            @PathVariable String keyword) {
        List<ArticleResponse> articleResponse = articleService.search(keyword);
        return ResponseEntity.ok(MessageUtils.success(articleResponse));
    }


    @DeleteMapping("{article_id}")
    public ResponseEntity<MessageUtils> deletearticlebyid(@PathVariable Long article_id
    ) {
        articleService.deletearticlebyid(article_id);

        return ResponseEntity.ok(MessageUtils.success());
    }

}
