package net.risesoft.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.risesoft.entity.ChatSession;

/**
 * AI 对话会话 Repository
 */
public interface ChatSessionRepository
    extends JpaRepository<ChatSession, String>, JpaSpecificationExecutor<ChatSession> {

    /**
     * 根据用户ID查询会话列表，按更新时间倒序
     */
    List<ChatSession> findByPersonIdAndTenantIdOrderByUpdateTimeDesc(String personId, String tenantId);

    /**
     * 分页查询用户会话列表
     */
    Page<ChatSession> findByPersonIdAndTenantId(String personId, String tenantId, Pageable pageable);

    /**
     * 更新会话标题和更新时间
     */
    @Modifying
    @Query("update ChatSession c set c.title = :title, c.updateTime = current_timestamp where c.id = :sessionId")
    void updateTitle(@Param("sessionId") String sessionId, @Param("title") String title);

    /**
     * 删除指定会话
     */
    void deleteByIdAndPersonId(String id, String personId);
}
