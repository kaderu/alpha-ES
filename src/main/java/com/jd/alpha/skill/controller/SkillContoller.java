package com.jd.alpha.skill.controller;

import com.jd.alpha.skill.client.entity.request.SkillData;
import com.jd.alpha.skill.client.entity.response.SkillResponse;
import com.jd.alpha.skill.handler.ChuuNiByouHandler;
import com.jd.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * Created by zhangshangzhi on 2018/3/18.
 */

@RestController
//@Resource
public class SkillContoller {

    @Autowired
    private ChuuNiByouHandler handler;

    /**
     *
//     * @param requestBody 请求JSON字符串
     * @return SkillResponse
     */
    @RequestMapping(value = "/skill",
            produces = {"application/json;charset=UTF-8" },
            method = RequestMethod.POST)
    public SkillResponse index(
            @RequestBody String requestBody
    ) {
// 将得到的JSON数据转换为 SkillData 对象，并交由Handler 进行处理
        SkillData data =
                JSON.parseObject(requestBody, SkillData.class);
        SkillResponse response =
                handler.handle(data);
        System.out.println("skill ### this method called");
        return response;
    }

    // test
    @RequestMapping(value = "/skill2",
            produces = {"application/json;charset=UTF-8" },
            method = RequestMethod.GET)
    public SkillResponse index(
//            @RequestBody String requestBody
    ) {
        SkillData data =
                JSON.parseObject(null, SkillData.class);
        SkillResponse response =
                handler.handle(data);
        System.out.println("skill2 ### this method called");
        return response;
    }

}
