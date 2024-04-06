package com.ssafy.fullerting.community.comment.service;

import com.ssafy.fullerting.alarm.service.EventAlarmService;
import com.ssafy.fullerting.community.article.exception.ArticleErrorCode;
import com.ssafy.fullerting.community.article.exception.ArticleException;
import com.ssafy.fullerting.community.article.model.entity.Article;
import com.ssafy.fullerting.community.article.repository.ArticleRepository;
import com.ssafy.fullerting.community.comment.exception.CommentErrorCode;
import com.ssafy.fullerting.community.comment.exception.CommentException;
import com.ssafy.fullerting.community.comment.model.dto.request.RegisterCommentRequest;
import com.ssafy.fullerting.community.comment.model.dto.response.CommentResonse;
import com.ssafy.fullerting.community.comment.model.entity.Comment;
import com.ssafy.fullerting.community.comment.repository.CommentRepository;
import com.ssafy.fullerting.user.model.dto.response.UserResponse;
import com.ssafy.fullerting.user.model.entity.CustomUser;
import com.ssafy.fullerting.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.hibernate.dialect.unique.CreateTableUniqueDelegate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final EventAlarmService eventAlarmService;


    public void registcomment(Long targetArticleId, RegisterCommentRequest registerCommentRequest) {
        Article targetArticle = articleRepository.findById(targetArticleId).orElseThrow(() -> new
                ArticleException(ArticleErrorCode.NOT_EXISTS));

        CustomUser commenter = userService.getNowUserInfoEntityByToken();

        Comment comment = commentRepository.save(Comment.builder()
                .article(targetArticle)
                .comment_content(registerCommentRequest.getCommentcontent())
                .customUser(commenter)
                .localDateTime(LocalDateTime.now())
                .build());

        eventAlarmService.notifyCommentCreated(commenter, targetArticle, registerCommentRequest.getRedirectURL());

//        article.addcomment(comment);
//        articleRepository.save(article);

    }


    public List<CommentResonse> allcomment(Long article_id) {

        return commentRepository.findAllByArticle_Id(article_id)
                .stream().map(comment -> {

                    CustomUser customUser = comment.getCustomUser();

                    CommentResonse commentResonse = comment.tocommentResonse(customUser);
                    return commentResonse;
                }).collect(Collectors.toList());

    }


    public void deletecommentbyid(Long articleId, Long commentId) {

        Article article = articleRepository.findById(articleId).orElseThrow(() -> new
                ArticleException(ArticleErrorCode.NOT_EXISTS));

        UserResponse userResponse = userService.getUserInfo();
        CustomUser customUser = userResponse.toEntity(userResponse);

        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new CommentException(CommentErrorCode.NOT_EXISTS));

        if (comment.getCustomUser().getId() != customUser.getId()) {
            throw new CommentException(CommentErrorCode.NOT_MINE);
        }



//        article.removecomment(comment);
//        articleRepository.save(article);

        commentRepository.delete(comment);
    }

}
