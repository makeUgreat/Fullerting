package com.ssafy.fullerting.community.article.service;

import com.ssafy.fullerting.community.article.exception.ArticleErrorCode;
import com.ssafy.fullerting.community.article.exception.ArticleException;
import com.ssafy.fullerting.community.article.model.dto.request.RegistArticleRequest;
import com.ssafy.fullerting.community.article.model.dto.request.UpdateArticleRequest;
import com.ssafy.fullerting.community.article.model.dto.response.ArticleAllResponse;
import com.ssafy.fullerting.community.article.model.dto.response.ArticleResponse;
import com.ssafy.fullerting.community.article.model.entity.Article;
import com.ssafy.fullerting.community.article.model.enums.ArticleType;
import com.ssafy.fullerting.community.article.repository.ArticleRepository;
import com.ssafy.fullerting.community.love.repository.LoveRepository;
import com.ssafy.fullerting.exArticle.exception.ExArticleErrorCode;
import com.ssafy.fullerting.exArticle.exception.ExArticleException;
import com.ssafy.fullerting.global.s3.model.entity.response.S3ManyFilesResponse;
import com.ssafy.fullerting.global.s3.servcie.AmazonS3Service;
import com.ssafy.fullerting.image.model.entity.Image;
import com.ssafy.fullerting.image.repository.ImageRepository;
import com.ssafy.fullerting.user.model.dto.response.UserResponse;
import com.ssafy.fullerting.user.model.entity.CustomUser;
import com.ssafy.fullerting.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.hibernate.Hibernate;
import org.springframework.data.redis.support.collections.RedisList;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final UserService userService;
    private final LoveRepository loveRepository;
    private final ImageRepository imageRepository;
    private final AmazonS3Service amazonS3Service;

    @Transactional
    public void registerticle(RegistArticleRequest registArticleRequest, List<MultipartFile> files) {

        UserResponse userResponse = userService.getUserInfo();
        CustomUser customUser = userResponse.toEntity(userResponse);
        log.info("type" + registArticleRequest.getType());
//        log.info("getSelectedFiles" + registArticleRequest.getSelectedFiles().size());


        S3ManyFilesResponse response =
                amazonS3Service.uploadFiles(files);


        Article article = articleRepository.save(Article.builder()
                .type(registArticleRequest.getType())
                .content(registArticleRequest.getContent())
                .createdAt(LocalDateTime.now())
                .title(registArticleRequest.getTitle())
                .love(0)
                .userId(customUser.getId())
                .build());

        List<Image> images = response.getUrls().entrySet().stream().map(stringStringEntry -> {
            Image image = new Image();
            image.setImgStoreUrl(stringStringEntry.getValue());
            image.setArticle(articleRepository.findById(article.getId()).
                    orElseThrow(() -> new ExArticleException(ExArticleErrorCode.NOT_EXISTS)));
            imageRepository.save(image);
            log.info("imagessssssss" + image.getImgStoreUrl());
            return image;
        }).collect(Collectors.toList());


        log.info("imagesize" + images.size());
//        images.forEach(article::addimage);

//        images.forEach(image -> {
//            article.addimage(image);
//        });

    }

    @Transactional
    public ArticleResponse update(UpdateArticleRequest updateArticleRequest, Long articleId, List<MultipartFile> files) {

        UserResponse userResponse = userService.getUserInfo();
        CustomUser customUser = userResponse.toEntity(userResponse);

        Article article = articleRepository.findById(articleId).orElseThrow(() -> new
                ArticleException(ArticleErrorCode.NOT_EXISTS));

        if (article.getUserId() != customUser.getId()) {
            throw new ArticleException(ArticleErrorCode.NOT_MINE);
        }

        List<Image> imageList = imageRepository.findAllByArticleId(articleId);

        for (Image image : imageList) {
            //유지할 이미지가 아닌 경우
            log.info("imagesss" + image.getId());
            if (!updateArticleRequest.getImages().contains(image.getId())) {
                //이미지 삭제
                amazonS3Service.deleteFile(image.getImgStoreUrl());
                imageRepository.delete(image);

            }

        }

        if (!files.get(0).isEmpty()) {

            S3ManyFilesResponse response =
                    amazonS3Service.uploadFiles(files);

//        log.info("imaggggggg" + article.getImages().size());
//        log.info("responseresponse" + response.getUrls().size());

            List<Image> images = response.getUrls().entrySet().stream().map(stringStringEntry -> {
                Image image = new Image();
                image.setImgStoreUrl(stringStringEntry.getValue());
                image.setArticle(articleRepository.findById(article.getId()).
                        orElseThrow(() -> new ExArticleException(ExArticleErrorCode.NOT_EXISTS)));
                imageRepository.save(image);
                return image;
            }).collect(Collectors.toList());

            log.info("iiiiiiiii" + images.size());
            article.setImages(images);

        }

        article.setContent(updateArticleRequest.getContent());
        article.setType(updateArticleRequest.getType());
        article.setTitle(updateArticleRequest.getTitle());


        Article article1 = articleRepository.save(article);
        log.info("resssssssss" + article1.getImages().size());

        boolean mylove = false;

        if (loveRepository.findByCustomUserIdAndArticleId(customUser.getId(), article1.getId()) != null) {
            mylove = true;
        }

        return article1.toResponse(article1, mylove, userService.getUserEntityById(article1.getUserId()));
    }

    @Transactional
    public ArticleResponse findarticlebyid(Long articleId) {
        Article article = articleRepository.findById(articleId).orElseThrow(() ->
                new ArticleException(ArticleErrorCode.NOT_EXISTS));

        UserResponse userResponse = userService.getUserInfo();
        CustomUser customUser = userResponse.toEntity(userResponse);
        boolean mylove = false;

        if (loveRepository.findByCustomUserIdAndArticleId(customUser.getId(), articleId) != null) {
            mylove = true;
        }

        UserResponse userResponse1 = userService.getUserInfobyid(article.getUserId());
        CustomUser user = userResponse1.toEntity(userResponse1);

        return article.toResponse(article, mylove, user);

    }

    @Transactional
    public void deletearticlebyid(Long articleId) {

        UserResponse userResponse = userService.getUserInfo();
        CustomUser customUser = userResponse.toEntity(userResponse);

        Article article = articleRepository.findById(articleId).orElseThrow(() ->
                new ArticleException(ArticleErrorCode.NOT_EXISTS));

        if (article.getUserId() != customUser.getId()) {
            throw new ArticleException(ArticleErrorCode.NOT_MINE);
        }

        articleRepository.delete(article);
    }


    @Transactional
    public List<ArticleAllResponse> findAllArticle() {

        UserResponse userResponse = userService.getUserInfo();
        CustomUser customUser = userResponse.toEntity(userResponse); //로그인한 유저..


        return articleRepository.findAll().stream().map(article -> {
                    boolean mylove = false;

                    if (loveRepository.findByCustomUserIdAndArticleId(customUser.getId(), article.getId()) != null) {
                        mylove = true;
                    }

                    String authornickname = userService.getUserInfobyid(article.getUserId()).getNickname();
                    ArticleAllResponse articleResponse = article.toAllResponse(article, mylove, authornickname);
                    return articleResponse;
                })
                .sorted(Comparator.comparing(ArticleAllResponse::getTime))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ArticleResponse> findAllArticlebyCategory(String keyword) {

        UserResponse userResponse = userService.getUserInfo();
        CustomUser customUser = userResponse.toEntity(userResponse);


        if (keyword.equals("자유게시판")) {
            return articleRepository.findAllByType((ArticleType.자유게시판)).stream().map(article -> {

                        boolean mylove = false;
                        if (loveRepository.findByCustomUserIdAndArticleId(customUser.getId(), article.getId()) != null) {
                            mylove = true;
                        }

                        UserResponse userResponse1 = userService.getUserInfobyid(article.getUserId());
                        CustomUser user = userResponse1.toEntity(userResponse1);

                        ArticleResponse articleResponse = article.toResponse(article, mylove, user);
                        return articleResponse;
                    })
                    .collect(Collectors.toList());
        } else if (keyword.equals("작물소개")) {
            return articleRepository.findAllByType((ArticleType.작물소개)).stream().map(article -> {
                        boolean mylove = false;
                        if (loveRepository.findByCustomUserIdAndArticleId(customUser.getId(), article.getId()) != null) {
                            mylove = true;
                        }

                        UserResponse userResponse1 = userService.getUserInfobyid(article.getUserId());
                        CustomUser user = userResponse1.toEntity(userResponse1);

                        ArticleResponse articleResponse = article.toResponse(article, mylove, user);
                        return articleResponse;
                    })
                    .collect(Collectors.toList());
        } else if (keyword.equals("텃밭요리")) {
            return articleRepository.findAllByType((ArticleType.텃밭요리)).stream().map(article -> {
                        boolean mylove = false;
                        if (loveRepository.findByCustomUserIdAndArticleId(customUser.getId(), article.getId()) != null) {
                            mylove = true;
                        }

                        UserResponse userResponse1 = userService.getUserInfobyid(article.getUserId());
                        CustomUser user = userResponse1.toEntity(userResponse1);

                        ArticleResponse articleResponse = article.toResponse(article, mylove, user);
                        return articleResponse;
                    })
                    .collect(Collectors.toList());
        } else if (keyword.equals("전체")) {
            return articleRepository.findAll().stream().map(article -> {
                        boolean mylove = false;
                        if (loveRepository.findByCustomUserIdAndArticleId(customUser.getId(), article.getId()) != null) {
                            mylove = true;
                        }
                        UserResponse userResponse1 = userService.getUserInfobyid(article.getUserId());
                        CustomUser user = userResponse1.toEntity(userResponse1);
                        ArticleResponse articleResponse = article.toResponse(article, mylove, user);
                        return articleResponse;
                    })
                    .collect(Collectors.toList());
        } else {
            return articleRepository.findAllByType((ArticleType.꿀팁공유)).stream().map(article -> {
                        boolean mylove = false;
                        if (loveRepository.findByCustomUserIdAndArticleId(customUser.getId(), article.getId()) != null) {
                            mylove = true;
                        }

                        UserResponse userResponse1 = userService.getUserInfobyid(article.getUserId());
                        CustomUser user = userResponse1.toEntity(userResponse1);

                        ArticleResponse articleResponse = article.toResponse(article, mylove, user);
                        return articleResponse;
                    })
                    .collect(Collectors.toList());
        }


    }

    @Transactional
    public List<ArticleResponse> search(String keyword) {

        UserResponse userResponse = userService.getUserInfo();
        CustomUser customUser = userResponse.toEntity(userResponse);

        return articleRepository.
                findAllByContentandtitle(keyword)
                .stream().map(article -> {
                    boolean mylove = false;
                    if (loveRepository.findByCustomUserIdAndArticleId(customUser.getId(), article.getId()) != null) {
                        mylove = true;
                    }

                    UserResponse userResponse1 = userService.getUserInfobyid(article.getUserId());
                    CustomUser user = userResponse1.toEntity(userResponse1);

                    ArticleResponse articleResponse = article.toResponse(article, mylove, user);
                    return articleResponse;
                })
                .collect(Collectors.toList());
    }
}
