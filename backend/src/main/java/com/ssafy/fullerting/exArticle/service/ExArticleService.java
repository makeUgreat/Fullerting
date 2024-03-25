package com.ssafy.fullerting.exArticle.service;

import com.ssafy.fullerting.deal.exception.DealErrorCode;
import com.ssafy.fullerting.deal.exception.DealException;
import com.ssafy.fullerting.deal.model.entity.Deal;
import com.ssafy.fullerting.deal.repository.DealRepository;
import com.ssafy.fullerting.deal.service.DealService;
import com.ssafy.fullerting.exArticle.exception.ExArticleErrorCode;
import com.ssafy.fullerting.exArticle.exception.ExArticleException;
import com.ssafy.fullerting.exArticle.model.dto.request.ExArticleDoneRequest;
import com.ssafy.fullerting.exArticle.model.dto.request.ExArticleRegisterImageRequest;
import com.ssafy.fullerting.exArticle.model.dto.request.ExArticleRegisterRequest;
import com.ssafy.fullerting.exArticle.model.dto.response.ExArticleAllResponse;
import com.ssafy.fullerting.exArticle.model.dto.response.ExArticleDetailResponse;
import com.ssafy.fullerting.exArticle.model.dto.response.ExArticleKeywordResponse;
import com.ssafy.fullerting.exArticle.model.dto.response.ExArticleResponse;
import com.ssafy.fullerting.exArticle.model.entity.ExArticle;
import com.ssafy.fullerting.exArticle.model.entity.enums.ExArticleType;
import com.ssafy.fullerting.exArticle.repository.ExArticleRepository;
import com.ssafy.fullerting.favorite.exception.FavoriteErrorCode;
import com.ssafy.fullerting.favorite.exception.FavoriteException;
import com.ssafy.fullerting.favorite.model.entity.Favorite;
import com.ssafy.fullerting.favorite.repository.favoriteRepository;

import com.ssafy.fullerting.global.s3.model.entity.response.S3ManyFilesResponse;
import com.ssafy.fullerting.global.s3.servcie.AmazonS3Service;
import com.ssafy.fullerting.image.model.entity.Image;
import com.ssafy.fullerting.image.repository.ImageRepository;
import com.ssafy.fullerting.record.packdiary.model.entity.PackDiary;
import com.ssafy.fullerting.record.packdiary.repository.PackDiaryRepository;
import com.ssafy.fullerting.trans.exception.TransErrorCode;
import com.ssafy.fullerting.trans.exception.TransException;
import com.ssafy.fullerting.trans.model.entity.Trans;
import com.ssafy.fullerting.trans.repository.TransRepository;
import com.ssafy.fullerting.user.exception.UserErrorCode;
import com.ssafy.fullerting.user.exception.UserException;
import com.ssafy.fullerting.user.model.dto.response.UserResponse;
import com.ssafy.fullerting.user.model.entity.CustomUser;
import com.ssafy.fullerting.user.repository.UserRepository;
import com.ssafy.fullerting.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExArticleService {
    private final ExArticleRepository exArticleRepository;
    private final DealRepository dealRepository;
    private final UserRepository userRepository;
    private final favoriteRepository favoriteRepository;
    private final TransRepository transRepository;
    private final PackDiaryRepository packDiaryRepository;
    private final DealService dealService;
    private final UserService userService;
    private final AmazonS3Service amazonS3Service;
    private final ImageRepository imageRepository;


    public void registeriamges(List<MultipartFile> files, Long ex_article_id) {

        ExArticle article = exArticleRepository.findById(ex_article_id).orElseThrow(() -> new ExArticleException(ExArticleErrorCode.NOT_EXISTS));


        S3ManyFilesResponse response =
                amazonS3Service.uploadFiles(files);

        List<Image> images = response.getUrls().entrySet().stream().map(stringStringEntry -> {
            Image image = new Image();
            image.setImg_store_url(stringStringEntry.getValue());
            image.setExArticle(exArticleRepository.findById(article.getId()).
                    orElseThrow(() -> new ExArticleException(ExArticleErrorCode.NOT_EXISTS)));
            imageRepository.save(image);
            return image;
        }).collect(Collectors.toList());

//        article.setImage(file);
    }


    public Long register(ExArticleRegisterRequest exArticleRegisterRequest, String email1, List<MultipartFile> files) {
//        public Long register(ExArticleRegisterRequest exArticleRegisterRequest, String email1) {

        if(exArticleRegisterRequest.getExArticleType().equals(null)){
            throw new ExArticleException(ExArticleErrorCode.NOT_EXISTS);
        }

        CustomUser customUser = userRepository.findByEmail(email1).orElseThrow(() -> new UserException(UserErrorCode.NOT_EXISTS_USER));
//        log.info("ussssss"+customUser.getEmail());
        log.info("ussssss" + email1);

//        List<MultipartFile> files = exArticleRegisterImageRequest.getMultipartFiles();

        S3ManyFilesResponse response =
                amazonS3Service.uploadFiles(files);

        Optional<PackDiary> packDiary = null;

        if (exArticleRegisterRequest.getPackdiaryid() != null)
            packDiary = packDiaryRepository.findById(exArticleRegisterRequest.getPackdiaryid());


        LocalDateTime createdAt = LocalDateTime.now(); // 현재 시각 설정


        ExArticle exArticle = ExArticle.builder()
                .id(exArticleRegisterRequest.getId())
                .title(exArticleRegisterRequest.getExArticleTitle())
                .content(exArticleRegisterRequest.getExArticleContent())
//                .place(exArticleRegisterRequest.getExArticlePlace())
                .type(exArticleRegisterRequest.getExArticleType())
                .created_at(createdAt)
                .location(exArticleRegisterRequest.getEx_article_location())
                .user(customUser)
                .favorite(exArticleRegisterRequest.getFavorite())
                .packDiary(packDiary != null ? packDiary.orElse(null) : null)
                .build();

        log.info("exxxxx" + exArticle.toString());
//        exArticleRepository.saveAndFlush(exArticle);
        ExArticle exArticle1 = exArticleRepository.save(exArticle);

        ExArticle article2 = null;

        List<Image> images = response.getUrls().entrySet().stream().map(stringStringEntry -> {
            Image image = new Image();
            image.setImg_store_url(stringStringEntry.getValue());
            image.setExArticle(exArticleRepository.findById(exArticle1.getId()).
                    orElseThrow(() -> new ExArticleException(ExArticleErrorCode.NOT_EXISTS)));
            imageRepository.save(image);
            return image;
        }).collect(Collectors.toList());

//        exArticle1.setImage(images);
        ExArticle article = exArticleRepository.save(exArticle1);

        if (exArticleRegisterRequest.getExArticleType().equals(ExArticleType.DEAL)) {
            Deal deal = Deal.builder()
                    .deal_cur_price(exArticleRegisterRequest.getDeal_cur_price())
                    .build();

            deal.setexarticle(article);

            System.out.println("exarttttt   " + exArticle.toString());

            dealRepository.save(deal);

            article.setdeal(deal);
            article2 = exArticleRepository.save(article);

            System.out.println("exarttttt22222      " + article.getDeal());
        } else {
            //sharing,generaltransaction
            int price = 0;

            if (exArticleRegisterRequest.getExArticleType().equals(ExArticleType.GENERAL_TRANSACTION)) {
                price = exArticleRegisterRequest.getDeal_cur_price();
            }

            Trans trans = Trans.builder()
                    .trans_sell_price(price)
                    .build();

            trans.setexarticle(article);

            System.out.println("exarttttt   " + exArticle.toString());

            transRepository.save(trans);

            article.setTrans(trans);
            article2 = exArticleRepository.save(article);

        }
        log.info("iiiiiii" + article.getImage());

        return article2.getId();
    }

    public List<ExArticleAllResponse> allArticle() {

        List<ExArticle> exArticle = exArticleRepository.findAll();
        CustomUser user = UserResponse.toEntity(userService.getUserInfo());
//        log.info("eeeeeeeeeeeee" + exArticle.stream().
//                map(exArticle1 -> exArticle1.toResponse(exArticle1, user)).filter( exArticleResponse -> exArticleResponse.getExArticleId()==28).collect(Collectors.toList()));


        List<ExArticleAllResponse> exArticleResponses =
                exArticle.stream().map(exArticle1 -> exArticle1.toAllResponse(exArticle1, user)).
                        collect(Collectors.toList());


        return exArticleResponses;
    }


    public void like(Long ex_article_id) {
        //좋아요 로직
        ExArticle article = exArticleRepository.findById(ex_article_id).orElseThrow(() ->
                new ExArticleException(ExArticleErrorCode.NOT_EXISTS));


        UserResponse userResponse = userService.getUserInfo();
        Long userid = userResponse.getId();
        Long articleid = article.getId();

        Optional<Favorite> favorite1 = favoriteRepository.findByUserIdAndExArticleId(userid, articleid);

        if (favorite1.isPresent()) {
            throw new ExArticleException(ExArticleErrorCode.ALREADY_LIKED);
        }

        Favorite favorite = new Favorite();
        favorite.setExArticle(article);
        favorite.setUser(userResponse.toEntity(userResponse));


        article.addfavorite(favorite);

        favoriteRepository.save(favorite);
    }

    public List<ExArticleKeywordResponse> keyword(String keyword) { //keyword 검색.
        List<ExArticle> exArticles = exArticleRepository.findAllByTitleContaining(keyword).orElseThrow(() ->
                new ExArticleException(ExArticleErrorCode.NOT_EXISTS));
        CustomUser user = UserResponse.toEntity(userService.getUserInfo());
        return exArticles.stream().map(exArticle -> exArticle.tokeyResponse(exArticle, user)).collect(Collectors.toList());

    }

    public ExArticleDetailResponse detail(Long ex_article_id) {
        ExArticle article = exArticleRepository.findById(ex_article_id).orElseThrow(() -> new ExArticleException(ExArticleErrorCode.NOT_EXISTS));
        UserResponse userResponse = userService.getUserInfo();
        CustomUser user = userResponse.toEntity(userResponse);

        return article.toDetailResponse(article, user);

    }

    public void done(Long exArticleId, ExArticleDoneRequest exArticleDoneRequest) {
        //구매자 누군지 설정, 거래완료 true

        CustomUser customUser = UserResponse.toEntity(userService.getUserInfo());
        ExArticle exArticle = exArticleRepository.findById(exArticleId).orElseThrow(() ->
                new ExArticleException(ExArticleErrorCode.NOT_EXISTS));

        log.info("exxxx" + exArticle.getPurchaserId());
        exArticle.setdone();
        exArticle.setpurchaserid(exArticleDoneRequest.getExArticlePurchaserId());

        exArticleRepository.save(exArticle);
        log.info("exxxx" + exArticle.getPurchaserId());
    }

    public void deletelike(Long ex_article_id) {
        //좋아요 삭제
        ExArticle article = exArticleRepository.findById(ex_article_id).orElseThrow(() ->
                new ExArticleException(ExArticleErrorCode.NOT_EXISTS));

        UserResponse userResponse = userService.getUserInfo();
        Long userid = userResponse.getId();

        Long articleid = article.getId();

        Favorite favorite1 = favoriteRepository.findByUserIdAndExArticleId(userid, articleid).
                orElseThrow(() -> new FavoriteException(FavoriteErrorCode.NOT_EXISTS));

//
//        Favorite favorite = new Favorite();
//        favorite.setExArticle(article);
//        favorite.setUser(userResponse.toEntity(userResponse));

        article.deletefavorite(favorite1);

        favoriteRepository.delete(favorite1);
    }

    public List<ExArticleResponse> selectFavorite() {  //나의 관심 article 추출.
        UserResponse userResponse = userService.getUserInfo();
        CustomUser user = UserResponse.toEntity(userResponse);

        List<ExArticle> exArticles = exArticleRepository.findAllByUserIdAndFavoriteIsNotEmpty(user.getId()); //내 article 중
        List<ExArticleResponse> exArticleResponses = exArticles.stream().map(exArticle -> exArticle.toResponse(exArticle, user)).collect(Collectors.toList());
        return exArticleResponses;
    }

    public List<ExArticleResponse> finishedarticles() {
        UserResponse userResponse = userService.getUserInfo();
        CustomUser user = UserResponse.toEntity(userResponse);

        List<ExArticle> exArticles = exArticleRepository.findAllByUserIDAndDone(user.getId()); //내 article 중
        List<ExArticleResponse> exArticleResponses = exArticles.stream().map(exArticle -> exArticle.toResponse(exArticle, user)).collect(Collectors.toList());
        return exArticleResponses;
    }

    public void deletearticle(Long ex_article_id) {
        ExArticle article = exArticleRepository.findById(ex_article_id).orElseThrow(()
                -> new ExArticleException(ExArticleErrorCode.NOT_EXISTS));

        List<Image> image = imageRepository.findAllByExArticleId(ex_article_id);

        for (Image image1 : image) {
            imageRepository.delete(image1);
            amazonS3Service.deleteFile(image1.getImg_store_url()); //s3
        }

        if (article.getType().equals(ExArticleType.DEAL)) {
            Deal deal = dealRepository.findById(article.getDeal().getId()).orElseThrow(() -> new DealException(DealErrorCode.NOT_EXISTS));
            dealRepository.delete(deal);
        } else {
            Trans trans = transRepository.findById(article.getTrans().getId()).orElseThrow(()
                    -> new TransException(TransErrorCode.NOT_EXISTS));
            transRepository.delete(trans);
        }

        exArticleRepository.delete(article);

    }

    public void convert_like(Long ex_article_id) {

        ExArticle article = exArticleRepository.findById(ex_article_id).orElseThrow(() ->
                new ExArticleException(ExArticleErrorCode.NOT_EXISTS));


        UserResponse userResponse = userService.getUserInfo();
        Long userid = userResponse.getId();
        Long articleid = article.getId();

        Optional<Favorite> favorite1 = favoriteRepository.findByUserIdAndExArticleId(userid, articleid);

        if (favorite1.isPresent()) { //내가 이미 좋아요를 한경우 삭제를 해야한다.

            article.deletefavorite(favorite1.orElse(null));

            favoriteRepository.delete(favorite1.orElse(null));

            return;
        }
        // 좋아요를 안한경우

        Favorite favorite = new Favorite();
        favorite.setExArticle(article);
        favorite.setUser(userResponse.toEntity(userResponse));


        article.addfavorite(favorite);

        favoriteRepository.save(favorite);

    }
}
