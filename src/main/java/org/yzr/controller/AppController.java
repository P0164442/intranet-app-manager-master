package org.yzr.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.yzr.model.Role;
import org.yzr.model.User;
import org.yzr.service.AppService;
import org.yzr.utils.file.PathManager;
import org.yzr.utils.response.BaseResponse;
import org.yzr.utils.response.ResponseUtil;
import org.yzr.vo.AppViewModel;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
public class AppController {

    @Resource
    private AppService appService;
    @Resource
    private PathManager pathManager;

    @GetMapping("/")
    public String index(HttpServletRequest request) {
//        return "redirect:apps";
        return "signin";
    }



    @RequiresAuthentication
    @GetMapping("/apps")
    public String apps(HttpServletRequest request) {
        Subject currentUser = SecurityUtils.getSubject();
        User user = (User) currentUser.getPrincipal();
        List<AppViewModel> apps = new ArrayList<>();
        Iterator<Role> it = user.getRoleList().iterator();
        while (it.hasNext()) {
            if ("ANONYMOUS_USER".equals(it.next().getName())) {
                apps = this.appService.findByUserDelDev(user);
                request.setAttribute("apps", apps);
                request.setAttribute("baseURL", this.pathManager.getBaseURL(false));
                return "index_ordinary";
            } else {
                apps = this.appService.findByUser(user);
            }
            request.setAttribute("apps", apps);
            request.setAttribute("baseURL", this.pathManager.getBaseURL(false));
        }

        return "index";
    }

    @RequiresPermissions("/apps/get")
    @GetMapping("/apps/{appID}")
    public String getAppById(@PathVariable("appID") String appID, HttpServletRequest request) {
        AppViewModel appViewModel = this.appService.getById(appID);
        request.setAttribute("package", appViewModel);
        request.setAttribute("apps", appViewModel.getPackageList());
        return "list";
    }

    @RequiresPermissions("/packageList/get")
    @RequestMapping("/packageList/{appID}")
    @ResponseBody
    public Map<String, Object> getAppPackageList(@PathVariable("appID") String appID) {
        AppViewModel appViewModel = this.appService.getById(appID);
        Map<String, Object> map = new HashMap<>();
        try {
            map.put("packages", appViewModel.getPackageList());
            map.put("success", true);
        } catch (Exception e) {
            map.put("success", false);
        }
        return map;
    }

    @RequiresPermissions("/app/delete")
    @RequestMapping("/app/delete/{id}")
    @ResponseBody
    public BaseResponse deleteById(@PathVariable("id") String id) {
        try {
            Subject currentUser = SecurityUtils.getSubject();
            User user = (User) currentUser.getPrincipal();
            if (user == null) {
                return ResponseUtil.unauthz();
            }
            AppViewModel viewModel = this.appService.getById(id);
            if (viewModel.getUserId().equals(user.getId())) {
                this.appService.deleteById(id);
                return ResponseUtil.ok("削除成功");
            }
            return ResponseUtil.unauthz();
        } catch (Exception e) {
            return ResponseUtil.fail();
        }
    }

}
