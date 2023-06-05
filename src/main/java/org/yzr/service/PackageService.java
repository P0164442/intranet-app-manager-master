package org.yzr.service;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.yzr.dao.PackageDao;
import org.yzr.model.App;
import org.yzr.model.Package;
import org.yzr.model.User;
import org.yzr.utils.file.PathManager;
import org.yzr.utils.image.ImageUtils;
import org.yzr.utils.parser.ParserClient;
import org.yzr.vo.PackageViewModel;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.io.File;

@Service
public class PackageService {

    @Resource
    private PackageDao packageDao;
    @Resource
    private PathManager pathManager;

    public Package buildPackage(String filePath, User user) throws ClassNotFoundException {
        Package aPackage = ParserClient.parse(filePath);
        try {
            App app = new App();
            app.setOwner(user);
            aPackage.setApp(app);

            String fileName =  aPackage.getPlatform() + "." + FilenameUtils.getExtension(filePath);
            //
            aPackage.setFileName(fileName);

            String packagePath = PathManager.getFullPath(aPackage);
            String tempIconPath = PathManager.getTempIconPath(aPackage);
            String iconPath =  packagePath + File.separator + "icon.png";
            String sourcePath = packagePath + File.separator + fileName;

            //
            ImageUtils.resize(tempIconPath, iconPath, 192, 192);
            //
            FileUtils.copyFile(new File(filePath), new File(sourcePath));

            //
            FileUtils.forceDelete(new File(tempIconPath));
            //
            FileUtils.forceDelete(new File(filePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        aPackage.setApp(null);
        return aPackage;
    }

    @Transactional
    public Package save(Package aPackage) {
        return this.packageDao.save(aPackage);
    }

    @Transactional
    public Package get(String id) {
        Package aPackage = this.packageDao.findById(id).get();
        //
        aPackage.getApp().getOwner().getId();
        return aPackage;
    }

    @Transactional
    public PackageViewModel findById(String id) {
        Package aPackage = this.packageDao.findById(id).get();
        PackageViewModel viewModel = new PackageViewModel(aPackage, this.pathManager);
        return viewModel;
    }

    @Transactional
    public void deleteById(String id) {
        Package aPackage = this.packageDao.findById(id).get();
        if (aPackage != null) {
            this.packageDao.deleteById(id);
            String path = PathManager.getFullPath(aPackage);
            PathManager.deleteDirectory(path);
        }

    }
}
