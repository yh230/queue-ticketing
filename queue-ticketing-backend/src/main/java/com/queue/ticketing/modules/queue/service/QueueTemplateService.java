package com.queue.ticketing.modules.queue.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.queue.ticketing.common.exception.BizException;
import com.queue.ticketing.common.exception.ErrorCodeEnum;
import com.queue.ticketing.modules.queue.dto.QueueRule;
import com.queue.ticketing.modules.queue.entity.QueueTemplate;
import com.queue.ticketing.modules.queue.mapper.QueueTemplateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 队列模板服务
 *
 * @author yh
 * @date 2026-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueueTemplateService extends ServiceImpl<QueueTemplateMapper, QueueTemplate> {

    private final ObjectMapper objectMapper;

    /**
     * 根据ID查询模板
     *
     * @param templateId 模板ID
     * @return 模板
     */
    public QueueTemplate getByIdSafe(Long templateId) {
        QueueTemplate template = getById(templateId);
        if (template == null) {
            throw new BizException(ErrorCodeEnum.PARAM_ERROR, "队列模板不存在");
        }
        return template;
    }

    /**
     * 解析队列规则
     *
     * @param template 队列模板
     * @return 队列规则
     */
    public QueueRule parseRule(QueueTemplate template) {
        if (template.getRulesJson() == null || template.getRulesJson().isBlank()) {
            QueueRule defaultRule = new QueueRule();
            defaultRule.setCapacity(template.getCapacity());
            return defaultRule;
        }
        try {
            QueueRule rule = objectMapper.readValue(template.getRulesJson(), QueueRule.class);
            if (rule.getCapacity() == null) {
                rule.setCapacity(template.getCapacity());
            }
            return rule;
        } catch (JsonProcessingException e) {
            log.error("解析队列规则失败，templateId={}", template.getId(), e);
            QueueRule defaultRule = new QueueRule();
            defaultRule.setCapacity(template.getCapacity());
            return defaultRule;
        }
    }
}
