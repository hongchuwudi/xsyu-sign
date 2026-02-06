package com.hongchu.qqrobotsign.controller;

import com.hongchu.qqrobotsign.pojo.entity.Announcement;
import com.hongchu.qqrobotsign.result.Result;
import com.hongchu.qqrobotsign.service.IAnnouncementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AnnouncementController {

    @Autowired private IAnnouncementService announcementService;

    // ========== 用户端 ==========

    /** 获取最新公告 */
    @GetMapping("/user/announcement/latest")
    public Result<Announcement> getLatest() {
        Announcement latest = announcementService.getLatest();
        return Result.success(latest);
    }

    // ========== 管理端 ==========

    /** 获取所有公告 */
    @GetMapping("/admin/announcements")
    public Result<List<Announcement>> listAll() {
        return Result.success(announcementService.listAll());
    }

    /** 获取单个公告 */
    @GetMapping("/admin/announcements/{id}")
    public Result<Announcement> getById(@PathVariable Long id) {
        return Result.success(announcementService.getById(id));
    }

    /** 新增公告 */
    @PostMapping("/admin/announcements")
    public Result<Void> add(@RequestBody Announcement announcement) {
        announcementService.add(announcement);
        return Result.success();
    }

    /** 更新公告 */
    @PutMapping("/admin/announcements/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Announcement announcement) {
        announcement.setId(id);
        announcementService.update(announcement);
        return Result.success();
    }

    /** 删除公告 */
    @DeleteMapping("/admin/announcements/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return Result.success();
    }
}
