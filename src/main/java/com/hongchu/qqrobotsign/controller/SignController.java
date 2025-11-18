package com.hongchu.qqrobotsign.controller;

import com.hongchu.qqrobotsign.pojo.DTO.SignDTO;
import com.hongchu.qqrobotsign.pojo.entity.SignItem;
import com.hongchu.qqrobotsign.service.SignService;
import com.hongchu.qqrobotsign.webClient.BaseSignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 签到表 前端控制器
 * </p>
 *
 * @author hongchu
 * @since 2025-11-18
 */
@RestController
@RequestMapping("/sign")
@RequiredArgsConstructor
@Slf4j
public class SignController {
    @Autowired private SignService signService;
    @Autowired private BaseSignService baseSignService;

    /**
     *  获取所有签到
     * @param username 用户名
     * @param page 页码
     * @param size 每页数量
     * @return 签到结果
     */
    @RequestMapping("/allSign")
    public String getAllSign(@RequestParam("username") String username,
                             @RequestParam(value = "page",defaultValue = "1") Integer page,
                             @RequestParam(value = "size",defaultValue = "10") Integer size){
        log.info("controller层-获取所有签到-username: {}", username);
        return String.valueOf(baseSignService.getAllSign(username, page, size));
    }

    /**
     * 获取单个签到
     * @param username 用户名
     * @param signId 签到id
     * @param schoolId 学校id
     * @return 签到结果
     */
    @RequestMapping("/oneSign/{signId}/{schoolId}")
    public String getOneSign(@RequestParam("username") String username,
                             @PathVariable("signId") String signId,
                             @PathVariable("schoolId") String schoolId){
        log.info("controller层-获取单个签到-username: {}, signId: {}, schoolId: {}",
                username, signId, schoolId);
        return String.valueOf(baseSignService.getOneSign(username, signId, schoolId));
    }

    /**
     * 一键签到
     * @param username 用户名
     * @return 签到结果
     */
    @RequestMapping("/all")
    public String sign(@RequestParam("username") String username) {
        log.info("controller层-签到-username: {}", username);
        return signService.signAll(username);
    }

    /**
     * 处理单个签到
     * @param username 用户名
     * @return 签到结果
     */
    @RequestMapping("/one")
    public String signOne(@RequestParam("username") String username,
                          @RequestParam(name = "id")String id,
                          @RequestParam(name = "signId") String signId,
                          @RequestParam(name = "schoolId") String schoolId,
                          @RequestParam(name = "signDTO", required = false) SignDTO signDTO) {
        log.info("controller层-处理单个签到-username: {}, id: {}, signId: {}, schoolId: {}",
                username, id, signId, schoolId);
                SignItem signItem = SignItem.builder()
                        .id(id)
                        .signId(signId)
                        .schoolId(schoolId)
                        .build();
        return signService.processSingleSign(username, signItem, signDTO);
    }

}
