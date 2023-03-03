package com.filmdoms.community.board.notice.data.dto.response;

import com.filmdoms.community.board.comment.data.dto.ParentCommentResponseDto;
import com.filmdoms.community.board.comment.data.entity.Comment;
import com.filmdoms.community.board.comment.data.utility.CommentUtils;
import com.filmdoms.community.board.data.dto.BoardHeadCoreDetailResponseDto;
import com.filmdoms.community.board.notice.data.entity.NoticeHeader;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class NoticeDetailResponseDto extends BoardHeadCoreDetailResponseDto {

    //메인 페이지 정보는 상세 조회시 필요 없음
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<ParentCommentResponseDto> comments;
    private LocalDateTime dateCreated;
    private LocalDateTime dateLastModified;

    public NoticeDetailResponseDto(NoticeHeader noticeHeader, List<Comment> comments) {
        super(noticeHeader);
        this.startDate = noticeHeader.getStartDate();
        this.endDate = noticeHeader.getEndDate();
        this.comments = CommentUtils.convert(comments);
        this.dateCreated = noticeHeader.getDateCreated();
        this.dateLastModified = noticeHeader.getDateLastModified();
    }
}
