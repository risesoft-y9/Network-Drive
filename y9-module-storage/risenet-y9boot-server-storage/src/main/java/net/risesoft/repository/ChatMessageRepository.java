package net.risesoft.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.risesoft.entity.ChatMessage;

/**
 * AI 对话消息 Repository
 */
public interface ChatMessageRepository
    extends JpaRepository<ChatMessage, String>, JpaSpecificationExecutor<ChatMessage> {

    /**
     * 根据会话ID查询所有消息，按创建时间升序
     */
    List<ChatMessage> findBySessionIdOrderByCreateTimeAsc(String sessionId);

    /**
     * 根据会话ID删除所有消息
     */
    void deleteBySessionId(String sessionId);
}
