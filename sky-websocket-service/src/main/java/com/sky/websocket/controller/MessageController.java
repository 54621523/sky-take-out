package com.sky.websocket.controller;

import com.sky.context.BaseContext;
import com.sky.result.Result;
import com.sky.websocket.service.OfflineMessageService;
import com.sky.websocket.websocket.WebSocketServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/message")
@Api(tags = "消息接口")
@Slf4j
public class MessageController {

    @Autowired
    private OfflineMessageService offlineMessageService;

    @GetMapping("/offline")
    @ApiOperation("获取离线消息")
    public Result<List<String>> getOfflineMessages() {
        Long userId = BaseContext.getCurrentId();
        List<String> messages = offlineMessageService.getOfflineMessages(
                WebSocketServer.ROLE_USER,
                String.valueOf(userId)
        );
        return Result.success(messages);
    }

    @GetMapping("/offline/count")
    @ApiOperation("获取离线消息数量")
    public Result<Integer> getOfflineMessageCount() {
        Long userId = BaseContext.getCurrentId();
        int count = offlineMessageService.getOfflineMessageCount(
                WebSocketServer.ROLE_USER,
                String.valueOf(userId)
        );
        return Result.success(count);
    }
}
