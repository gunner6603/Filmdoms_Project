package com.filmdoms.community.newcomment.service;

import com.filmdoms.community.account.data.dto.AccountDto;
import com.filmdoms.community.account.data.entity.Account;
import com.filmdoms.community.account.exception.ApplicationException;
import com.filmdoms.community.account.exception.ErrorCode;
import com.filmdoms.community.account.repository.AccountRepository;
import com.filmdoms.community.article.data.constant.Category;
import com.filmdoms.community.article.data.entity.Article;
import com.filmdoms.community.article.repository.ArticleRepository;
import com.filmdoms.community.board.data.constant.CommentStatus;
import com.filmdoms.community.newcomment.data.dto.request.NewCommentCreateRequestDto;
import com.filmdoms.community.newcomment.data.dto.request.NewCommentUpdateRequestDto;
import com.filmdoms.community.newcomment.data.dto.response.DetailPageCommentResponseDto;
import com.filmdoms.community.newcomment.data.dto.response.NewCommentCreateResponseDto;
import com.filmdoms.community.newcomment.data.entity.NewComment;
import com.filmdoms.community.newcomment.repository.NewCommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NewCommentService {

    private final NewCommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final AccountRepository accountRepository;

    public DetailPageCommentResponseDto getDetailPageCommentList(Long articleId) {
        List<NewComment> comments = commentRepository.findByArticleIdWithAuthorProfileImage(articleId);
        return DetailPageCommentResponseDto.from(comments);
    }

    public NewCommentCreateResponseDto createComment(NewCommentCreateRequestDto requestDto, AccountDto accountDto) {
        Article article = articleRepository.findById(requestDto.getArticleId()).orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_ARTICLE_ID));

        Account author = accountRepository.getReferenceById(accountDto.getId());

        NewComment parentComment = null;
        if (requestDto.getParentCommentId() != null) {
            parentComment = commentRepository.findById(requestDto.getParentCommentId()).orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_PARENT_COMMENT_ID));

            //위에서 조회한 Article 엔티티가 부모 댓글에 매핑된 Article 엔티티와 동일한지 확인
            if (!Objects.equals(parentComment.getArticle().getId(), article.getId())) {
                throw new ApplicationException(ErrorCode.INVALID_PARENT_COMMENT_ID);
            }

            //부모 댓글이 INACTIVE, DELETED인 경우 댓글 생성 불가능하게 할지 결정 필요
        }

        //일단 관리자 댓글은 공지 게시판에서 공지 작성자에 의해서만 생성되도록 함
        if (requestDto.isManagerComment()) {
            if (article.getCategory() != Category.FILM_UNIVERSE || !Objects.equals(article.getAuthor().getId(), accountDto.getId())) {
                throw new ApplicationException(ErrorCode.MANAGER_COMMENT_CANNOT_BE_CREATED);
            }
        }

        NewComment comment = NewComment.builder()
                .article(article)
                .parentComment(parentComment)
                .author(author)
                .content(requestDto.getContent())
                .isManagerComment(requestDto.isManagerComment())
                .build();

        NewComment savedComment = commentRepository.save(comment);
        return NewCommentCreateResponseDto.from(savedComment);
    }

    public void updateComment(NewCommentUpdateRequestDto requestDto, Long commentId, AccountDto accountDto) {
        NewComment comment = commentRepository.findById(commentId).orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_COMMENT_ID));

        //ACTIVE 상태인 댓글만 수정 가능
        checkCommentStatus(comment);

        //수정 권한 확인
        checkPermission(accountDto, comment);

        comment.update(requestDto.getContent());
    }

    public void deleteComment(Long commentId, AccountDto accountDto) {
        NewComment comment = commentRepository.findById(commentId).orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_COMMENT_ID));

        //ACTIVE 상태인 댓글만 삭제 가능
        checkCommentStatus(comment);

        //삭제 권한 확인
        checkPermission(accountDto, comment);

        //자식 댓글의 경우 별도 확인 없이 삭제
        if (comment.getParentComment() != null) {
            commentRepository.delete(comment);
            return;
        } //DELETED 상태인 부모 댓글의 하나뿐인 자식 댓글이 삭제된 경우 추가 처리를 해줄지 결정 필요

        //부모 댓글의 경우
        if (commentRepository.existsByParentComment(comment)) {
            //자식 댓글이 있으면 DELETED로 변경
            comment.updateStatus(CommentStatus.DELETED);
        } else {
            //자식 댓글이 없으면 삭제
            commentRepository.delete(comment);
        }
    }

    private void checkPermission(AccountDto accountDto, NewComment comment) {
        if (!Objects.equals(comment.getAuthor().getId(), accountDto.getId())) {
            throw new ApplicationException(ErrorCode.INVALID_PERMISSION);
        }
    }

    private void checkCommentStatus(NewComment comment) {
        if (comment.getStatus() != CommentStatus.ACTIVE) {
            throw new ApplicationException(ErrorCode.COMMENT_NOT_ACTIVE);
        }
    }
}