package org.yzr.controller;


import com.alibaba.fastjson.JSON;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.yzr.model.App;
import org.yzr.model.Package;
import org.yzr.model.User;
import org.yzr.service.AppService;
import org.yzr.service.PackageService;
import org.yzr.service.UserService;
import org.yzr.utils.file.FileType;
import org.yzr.utils.file.FileUtil;
import org.yzr.utils.file.PathManager;
import org.yzr.utils.image.QRCodeUtil;
import org.yzr.utils.ipa.PlistGenerator;
import org.yzr.utils.response.BaseResponse;
import org.yzr.utils.response.ResponseUtil;
import org.yzr.utils.webhook.WebHookClient;
import org.yzr.vo.AppViewModel;
import org.yzr.vo.PackageViewModel;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Controller
public class PackageController {
    @Resource
    private AppService appService;
    @Resource
    private PackageService packageService;
    @Resource
    private PathManager pathManager;
    @Resource
    private UserService userService;

    /**
     * preview
     *
     * @param code
     * @param request
     * @return
     */
    @GetMapping("/s/{code}")
    public String get(@PathVariable("code") String code, HttpServletRequest request) {
        if("V4va".equals(code)){
            code = "ZRTb";
            String id = "2c91808271873e520171876cedb20005";
            AppViewModel viewModel = this.appService.findByCode(code, id);
            request.setAttribute("app", viewModel);
            request.setAttribute("ca_path", this.pathManager.getCAPath());
            request.setAttribute("basePath", this.pathManager.getBaseURL(false));
            return "install";
        }else if("8s7N".equals(code)){
            code = "JY5f";
            String id ="2c91808271873e520171876b1f970003";
            AppViewModel viewModel = this.appService.findByCode(code, id);
            request.setAttribute("app", viewModel);
            request.setAttribute("ca_path", this.pathManager.getCAPath());
            request.setAttribute("basePath", this.pathManager.getBaseURL(false));
            return "install";
        }else{
            String id = request.getParameter("id");
            AppViewModel viewModel = this.appService.findByCode(code, id);
            request.setAttribute("app", viewModel);
            request.setAttribute("ca_path", this.pathManager.getCAPath());
            request.setAttribute("basePath", this.pathManager.getBaseURL(false));
            return "install";
        }

    }

    /**
     * devices
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/devices/{id}")
    public String devices(@PathVariable("id") String id, HttpServletRequest request) {
        PackageViewModel viewModel = this.packageService.findById(id);
        request.setAttribute("app", viewModel);
        return "devices";
    }

    /**
     *
     *
     * @param platform
     * @param request
     * @return
     */
    @GetMapping("/guide/{platform}")
    public String guide(@PathVariable("platform") String platform, HttpServletRequest request) {
        request.setAttribute("platform", platform);
        return "guide";
    }

    /**
     *
     *
     * @param file
     * @param request
     * @return
     */
    @RequestMapping("/app/upload")
    @ResponseBody
    public BaseResponse upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            String token = request.getParameter("token");
            User user = this.userService.findByToken(token);
            if (user == null) {
                Subject currentUser = SecurityUtils.getSubject();
                user = (User) currentUser.getPrincipal();
            }

            if (user == null) {
                return ResponseUtil.unauthz();
            }
            String filePath = transfer(file);
            FileType fileType = FileUtil.getType(filePath);
            if (fileType == null || fileType != FileType.ZIP) {
                FileUtils.forceDelete(new File(filePath));
                return ResponseUtil.badArgument();
            }
            Package aPackage = this.packageService.buildPackage(filePath, user);
            Map<String, String> extra = new HashMap<>();
            String jobName = request.getParameter("jobName");
            String buildNumber = request.getParameter("buildNumber");
            if (StringUtils.hasLength(jobName)) {
                extra.put("jobName", jobName);
            }
            if (StringUtils.hasLength(buildNumber)) {
                extra.put("buildNumber", buildNumber);
            }
            if (!extra.isEmpty()) {
                aPackage.setExtra(JSON.toJSONString(extra));
            }
            App app = this.appService.savePackage(aPackage, user);
            // URL
            String codeURL = this.pathManager.getBaseURL(false) + "p/code/" + app.getCurrentPackage().getId();

            WebHookClient.sendMessage(app, pathManager);
            return ResponseUtil.ok(codeURL);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.badArgument();
        }
    }

    /**
     * download
     *
     * @param id
     * @param response
     */
    @RequestMapping("/p/{id}")
    public void download(@PathVariable("id") String id, HttpServletResponse response) {
        try {
            Package aPackage = this.packageService.get(id);
            String path = PathManager.getFullPath(aPackage) + aPackage.getFileName();
            File file = new File(path);
            if (file.exists()) {
                response.setContentType("application/force-download");
                String fileName = aPackage.getName() + "_" + aPackage.getVersion();
                String ext = "." + FilenameUtils.getExtension(aPackage.getFileName());
                String appName = new String(fileName.getBytes("UTF-8"), "iso-8859-1");
                response.setHeader("Content-Disposition", "attachment;fileName=" + appName + ext);

                byte[] buffer = new byte[1024];
                OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer);
                    i = bis.read(buffer);
                }
                bis.close();
                fis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *   manifest
     *
     * @param id
     * @param response
     */
    @RequestMapping("/m/{id}")
    public void getManifest(@PathVariable("id") String id, HttpServletResponse response) {
        try {
            PackageViewModel viewModel = this.packageService.findById(id);
            if (viewModel != null && viewModel.isiOS()) {
                response.setContentType("application/force-download");
                response.setHeader("Content-Disposition", "attachment;fileName=manifest.plist");
                Writer writer = new OutputStreamWriter(response.getOutputStream());
                PlistGenerator.generate(viewModel, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  qr code
     *
     * @param id
     * @param response
     */
    @RequestMapping("/p/code/{id}")
    public void getQrCode(@PathVariable("id") String id, HttpServletResponse response) {
        try {
            PackageViewModel viewModel = this.packageService.findById(id);
            if (viewModel != null) {
                response.setContentType("image/png");
                QRCodeUtil.encode(viewModel.getPreviewURL()).withSize(250, 250).writeTo(response.getOutputStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  
     *
     * @param id
     * @return
     */
    @RequiresPermissions("/p/delete")
    @RequestMapping("/p/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteById(@PathVariable("id") String id) {
        Map<String, Object> map = new HashMap<>();
        try {
            this.packageService.deleteById(id);
            map.put("success", true);
            map.put("msg", "削除成功");

        } catch (Exception e) {
            map.put("success", false);
            map.put("msg", "削除失敗");
        }
        return map;
    }

    /**
     *
     *
     * @param srcFile
     * @return
     */
    private String transfer(MultipartFile srcFile) {
        try {

            String fileName = srcFile.getOriginalFilename();
            String ext = FilenameUtils.getExtension(fileName);
            String newFileName = UUID.randomUUID().toString() + "." + ext;
            String destPath = FileUtils.getTempDirectoryPath() + File.separator + newFileName;
            destPath = destPath.replaceAll("//", "/");
            srcFile.transferTo(new File(destPath));
            return destPath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
