package com.kaituo.comparison.back.core.controller;

import com.kaituo.comparison.back.common.bean.ResponseCode;
import com.kaituo.comparison.back.common.bean.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @Author: yedong
 * @Date: 2020/2/13 17:14
 * @Modified by:
 */
@RestController
@Slf4j
@RequestMapping(value = {"/common"})
public class CommonControl {


    @GetMapping(value = "/cors")
    public String cors() {
        log.info("{}", "今天是个好日子~");
        return "cors success";
    }

    @GetMapping(value = "/test")
    public ResponseResult test() {

        return ResponseResult.e(ResponseCode.OK);
    }
}
