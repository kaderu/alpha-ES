package com.jd.alpha.skill.handler;


import com.jd.alpha.skill.builder.SkillResponseBuilder;
import com.jd.alpha.skill.client.RequestHandler;
import com.jd.alpha.skill.client.constant.RequestTypeConstants;
import com.jd.alpha.skill.client.entity.request.SkillData;
import com.jd.alpha.skill.client.entity.request.SkillRequest;
import com.jd.alpha.skill.client.entity.request.SkillRequestIntent;
import com.jd.alpha.skill.client.entity.response.SkillResponse;
import com.jd.alpha.skill.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by zhangshangzhi on 2018/3/16.
 */

@Service
public class ChuuNiByouHandler extends RequestHandler {

    @Autowired
    private SkillResponseBuilder skillResponseBuilder;

    private final static Logger LOG = LoggerFactory.getLogger(ChuuNiByouHandler.class);

    @Value("#{configProperties['chuunibyou_skill_id']}")
    private String skillApplicationId;

    public String setSkillApplicationId(String skillApplicationId) {
        return this.skillApplicationId = skillApplicationId;
    }

    public ChuuNiByouHandler() {
        System.out.println("###");
    }

    /**
     * 请求合法性校验
     *
     * @param skillData Skill请求数据
     * @return boolean
     */
    @Override
    public boolean validate(SkillData skillData) {
        return skillApplicationId.equals(skillData.getSession().getApplication().getApplicationId());
    }

    /**
     * Session开始时的响应
     *
     * @param skillData Skill请求数据
     */
    @Override
    public void onSessionStarted(SkillData skillData)
    {
        LOG.info("Session Started");
    }

    /**
     * 技能启动时的响应（一般返回欢迎语句）
     *
     * @param skillData Skill请求数据
     * @return SkillResponse
     */
    @Override
    public SkillResponse onLaunchRequest(SkillData skillData) {
        LOG.info("Skill Launched");
        return skillResponseBuilder.buildSimpleResponse(skillApplicationId, false, "ようこそ!");
    }

    /**
     * 自定义意图时的响应（用户在Alpha平台自行定义的意图）
     *
     * @param skillData Skill请求数据
     * @return SkillResponse
     */
    @Override
    public SkillResponse onIntentRequest(SkillData skillData) {
        try {
            SkillRequest intentRequestData = skillData.getRequest();
            SkillRequestIntent skillRequestIntent = intentRequestData.getIntent();
            if (Constant.INTENT_BEHAVIOR_REQUEST.equals(skillRequestIntent.getName())) {
                // TODO
            }
            String uCode = skillRequestIntent.getName() + " # " + skillRequestIntent.getSlots().get(Constant.SLOT_BEHAVIOR).getValue();
            return skillResponseBuilder.buildSimpleResponse(skillApplicationId, true, "ようこそ! " + uCode);
        } catch (Exception e) {
            LOG.error("onIntentRequest error", e);
        }

        return skillResponseBuilder.buildSimpleResponse(skillApplicationId, true, "ようこそ!");
    }

    /**
     * Session超时退出
     *
     * @param skillData Skill请求数据
     */
    @Override
    public void onSessionEndedRequest(SkillData skillData) {
        LOG.info("Session Ended");
    }

    /**
     * 取消意图时的响应
     *
     * @param skillData Skill请求数据
     * @return SkillResponse
     */
    @Override
    public SkillResponse onCancelIntent(SkillData skillData) {
        return skillResponseBuilder.buildSimpleResponse(skillApplicationId, false, "待っている, 怒る");
    }

    /**
     * 帮助意图时的响应
     *
     * @param skillData Skill请求数据
     * @return SkillResponse
     */
    @Override
    public SkillResponse onHelpIntent(SkillData skillData) {
        return skillResponseBuilder.buildSimpleResponse(skillApplicationId, false, "あなたは怒っていますか？怒っていない");
    }

    /**
     * 下一个意图时的响应
     *
     * @param skillData Skill请求数据
     * @return SkillResponse
     */
    @Override
    public SkillResponse onNextIntent(SkillData skillData) {
        return skillResponseBuilder.buildSimpleResponse(skillApplicationId, true, "怒っていない");
    }

    /**
     4/6
     * 重复播报意图时的响应（暂未支持）
     *
     * @param skillData Skill请求数据
     * @return SkillResponse
     */
    @Override
    public SkillResponse onRepeatIntent(SkillData skillData) {
        return null;
    }

    /**
     * 其他内置意图时的响应
     *
     * @param skillData Skill请求数据
     * @return SkillResponse
     */
    @Override
    public SkillResponse onOtherBuildInIntent(SkillData skillData) {
        return null;
    }

    /**
     * 默认响应
     *
     * @param skillData Skill请求数据
     * @return SkillResponse
     */
    @Override
    public SkillResponse defaultResponse(SkillData skillData) {
        String input = skillData.getRequest().getIntent().getSlots().get(Constant.SLOT_BEHAVIOR).getValue();
        if (input == null ||
                input.isEmpty()) {
            input = "意味がわからない";
        }
        return skillResponseBuilder.buildSimpleResponse(skillApplicationId, true, input);
    }

    @Override
    public SkillResponse handle(SkillData skillData) {
        try {
            if (!validate(skillData)) {
                return SkillResponse.builder().shouldEndSession(true).build();
            }

            if (skillData.getSession().isNew()) {
                onSessionStarted(skillData);
            }

            String requestType = skillData.getRequest().getType();

            switch (requestType) {
                case RequestTypeConstants.LAUNCH_REQUEST:
                    return onLaunchRequest(skillData);
                case RequestTypeConstants.INTENT_REQUEST:
                    return onIntentRequest(skillData);
                case RequestTypeConstants.SESSION_END_REQUEST:
                    onSessionEndedRequest(skillData);
                    break;
                default:
                    return defaultResponse(skillData);
            }
            return SkillResponse.builder().shouldEndSession(true).build();
        } catch (Exception e) {
            return SkillResponse.builder().shouldEndSession(true).build();
        }
    }

}