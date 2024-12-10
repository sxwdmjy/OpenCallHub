package com.och.api.controller;

import com.github.pagehelper.PageInfo;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.system.domain.query.callin.CallInPhoneAddQuery;
import com.och.system.domain.query.callin.CallInPhoneQuery;
import com.och.system.domain.vo.callin.CallInPhoneVo;
import com.och.system.service.ICallInPhoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author danmo
 * @date 2024-11-09 18:15
 **/
@Tag(name = "呼入号码管理")
@RestController
@RequestMapping("/callIn")
public class CallInPhoneController extends BaseController {

    @Autowired
    private ICallInPhoneService iCallInPhoneService;


    @Operation(summary = "新增呼入号码", method = "POST")
    @PostMapping("/add")
    public ResResult add(@RequestBody @Validated CallInPhoneAddQuery query) {
        iCallInPhoneService.add(query);
        return success();
    }

    @Operation(summary = "修改呼入号码", method = "PUT")
    @PutMapping("/edit/{id}")
    public ResResult edit(@PathVariable("id") Long id, @RequestBody @Validated CallInPhoneAddQuery query) {
        query.setId(id);
        iCallInPhoneService.update(query);
        return success();
    }

    @Operation(summary = "删除呼入号码", method = "POST")
    @PostMapping("/delete")
    public ResResult delete(@RequestBody CallInPhoneQuery query) {
        iCallInPhoneService.delete(query);
        return success();
    }

    @Operation(summary = "呼入号码详情", method = "POST")
    @PostMapping("/get/{id}")
    public ResResult<CallInPhoneVo> getDetail(@PathVariable("id") Long id, @RequestBody CallInPhoneQuery query) {
        query.setId(id);
        CallInPhoneVo detail = iCallInPhoneService.getDetail(query);
        return success(detail);
    }

    @Operation(summary = "呼入号码列表(分页)", method = "POST")
    @PostMapping("/page/getList")
    public ResResult<PageInfo<CallInPhoneVo>> getPageList(@RequestBody CallInPhoneQuery query) {
        List<CallInPhoneVo> list = iCallInPhoneService.getPageList(query);
        PageInfo<CallInPhoneVo> pageInfo = PageInfo.of(list);
        return success(pageInfo);
    }

    @Operation(summary = "呼入号码列表", method = "POST")
    @PostMapping("/getList")
    public ResResult<List<CallInPhoneVo>> getList(@RequestBody CallInPhoneQuery query) {
        List<CallInPhoneVo> list = iCallInPhoneService.getList(query);
        return success(list);
    }

    @Operation(summary = "通过手机号获取呼入号码信息", method = "GET")
    @GetMapping("/getDetailByPhone")
    public ResResult<CallInPhoneVo> getDetailByPhone(@RequestParam("phone") String phone) {
        CallInPhoneVo result = iCallInPhoneService.getDetailByPhone(phone);
        return success(result);
    }
}
