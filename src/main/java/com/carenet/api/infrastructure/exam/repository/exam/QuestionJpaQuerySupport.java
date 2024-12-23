package com.carenet.api.infrastructure.exam.repository.exam;

import com.carenet.api.domain.exception.ApplicationException;
import com.carenet.api.domain.exception.ErrorCode;
import com.carenet.api.infrastructure.Utils;
import com.carenet.api.infrastructure.exam.dto.payload.*;
import com.carenet.api.infrastructure.exam.dto.statement.QuestionStatement;
import com.carenet.api.infrastructure.exam.entity.QuestionEntity;
import com.carenet.api.infrastructure.useraccount.QUserAccountEntity;
import com.carenet.api.interfaces.exam.dto.SearchQuestionDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static com.carenet.api.infrastructure.exam.entity.QQuestionEntity.questionEntity;
import static com.carenet.api.infrastructure.exam.entity.QSelectionEntity.selectionEntity;
import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;

@Repository
public class QuestionJpaQuerySupport extends QuerydslRepositorySupport {

    private final JPAQueryFactory queryFactory;
    private final SelectionJpaRepository selectionJpaRepository;

    public QuestionJpaQuerySupport(JPAQueryFactory queryFactory, SelectionJpaRepository selectionJpaRepository) {
        super(QuestionEntity.class);
        this.queryFactory = queryFactory;
        this.selectionJpaRepository = selectionJpaRepository;
    }

    /** 문제 목록조회 */
    public Slice<QuestionPayload.Get> getQuestionsByExamId(Pageable pageable, QuestionStatement.Get statement) {
        QUserAccountEntity createUser = new QUserAccountEntity("createUser");
        QUserAccountEntity updateUser = new QUserAccountEntity("updateUser");

        List<QuestionPayload.Get> list = queryFactory.select(
                        new QQuestionPayload_Get(
                                questionEntity.id, questionEntity.examId, questionEntity.codeId,
                                questionEntity.name, questionEntity.article,
                                new QUserAccountPayload_Get(createUser.id, createUser.username),
                                new QUserAccountPayload_Get(updateUser.id, updateUser.username)
                        ))
                .from(questionEntity)
                .leftJoin(createUser).on(createUser.id.eq(questionEntity.createdBy))
                .leftJoin(updateUser).on(updateUser.id.eq(questionEntity.updatedBy))
                .where(questionEntity.examId.eq(statement.examId()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .orderBy(Utils.getOrderList(pageable.getSort(), QuestionEntity.class))
                .fetch();

        return new SliceImpl<>(list, pageable, Utils.hasNext(list, pageable.getPageSize()));
    }

    public Long getTotalCountByExamId(SearchQuestionDto.Search search, Long examId) {
        return queryFactory.select(questionEntity.id.count()).from(questionEntity)
                .where(questionEntity.examId.eq(examId).and(searchByConditions(search)))
                .fetchOne();
    }

    /** 문제 단일조회 */
    public QuestionPayload.GetWithSelections getQuestion(Long questionId) {
        QUserAccountEntity createUser = new QUserAccountEntity("createUser");
        QUserAccountEntity updateUser = new QUserAccountEntity("updateUser");

        Map<QuestionEntity, QuestionPayload.GetWithSelections> result = queryFactory.from(questionEntity)
                .leftJoin(selectionEntity).on(selectionEntity.id.questionId.eq(questionId))
                .leftJoin(createUser).on(createUser.id.eq(questionEntity.createdBy))
                .leftJoin(updateUser).on(updateUser.id.eq(questionEntity.updatedBy))
                .where(questionEntity.id.eq(questionId))
                .transform(groupBy(questionEntity).as(new QQuestionPayload_GetWithSelections(
                        questionEntity.id, questionEntity.examId, questionEntity.codeId,
                        questionEntity.name, questionEntity.article,
                        new QUserAccountPayload_Get(createUser.id, createUser.username),
                        new QUserAccountPayload_Get(updateUser.id, updateUser.username),
                        list(new QSelectionPayload_Get(
                                selectionEntity.id.questionId, selectionEntity.id.selectionId,
                                selectionEntity.content, selectionEntity.createdAt, selectionEntity.updatedAt
                        ))
                )));
        return result.values().stream().findFirst().orElseThrow(
                () -> new ApplicationException(ErrorCode.CONTENT_NOT_FOUND, "question not found : id - %d".formatted(questionId)));
    }

    private BooleanBuilder searchByConditions(SearchQuestionDto.Search search) {
        BooleanBuilder builder = new BooleanBuilder();

        return builder;
    }
}
