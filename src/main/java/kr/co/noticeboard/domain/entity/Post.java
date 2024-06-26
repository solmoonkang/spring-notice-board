package kr.co.noticeboard.domain.entity;

import jakarta.persistence.*;
import kr.co.noticeboard.service.dto.request.PostReqDTO;
import kr.co.noticeboard.service.dto.response.CommentResDTO;
import kr.co.noticeboard.service.dto.response.PostResDTO;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "tbl_posts", indexes = {
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@AttributeOverride(
        name = "id",
        column = @Column(name = "post_id", length = 4)
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Column(name = "title", length = 50)
    private String title;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "content", length = 500)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DeleteStatus status = DeleteStatus.NOT_DELETED;

    @OneToMany(
            mappedBy = "post",
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY
    )
    private List<Comment> comments = new ArrayList<>();

    @Builder
    public Post(String title,
                Member member,
                String content,
                List<Comment> comments) {

        this.title = title;
        this.member = member;
        this.content = content;
        this.comments = comments;
    }

    public static Post toPostEntity(Member member, PostReqDTO.CREATE create) {

        return Post.builder()
                .title(create.getTitle())
                .member(member)
                .content(create.getContent())
                .build();
    }

    public PostResDTO.READ toReadDto() {

        return PostResDTO.READ.builder()
                .title(title)
                .memberName(member.getName())
                .status(status)
                .build();
    }

    public PostResDTO.DETAIL toReadDetailDto() {

        return PostResDTO.DETAIL.builder()
                .title(title)
                .memberName(member.getName())
                .content(content)
                .status(status)
                .comments(toReadCommentDto())
                .build();
    }

    public void updatePost(PostReqDTO.UPDATE update) {

        this.title = update.getTitle();
        this.content = update.getContent();
    }

    public void markAsDeleted() {

        this.status = DeleteStatus.DELETED;
    }

    private List<CommentResDTO.READ> toReadCommentDto() {

        return comments.stream()
                .filter(comment -> comment.getStatus() == DeleteStatus.NOT_DELETED)
                .map(comment -> CommentResDTO.READ.builder()
                        .username(comment.getMember().getName())
                        .status(comment.getStatus())
                        .comment(comment.getComment())
                        .build()).toList();
    }
}
