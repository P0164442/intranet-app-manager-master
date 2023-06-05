package org.yzr.utils.ipa;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.util.ResourceUtils;
import org.yzr.vo.PackageViewModel;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class PlistGenerator {
    public static void generate(PackageViewModel aPackage, String destPath) {
        try {
            Writer out = new FileWriter(new File(destPath));
            generate(aPackage, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param aPackage
     * @param out
     */
    public static void generate(PackageViewModel aPackage, Writer out) {
        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);
            cfg.setClassLoaderForTemplateLoading(PlistGenerator.class.getClassLoader(), "/freemarker");
            cfg.setDefaultEncoding(StandardCharsets.UTF_8.name());
            Map<String, Object> root = new HashMap<>();
            root.put("aPackage", aPackage);
            Template template = cfg.getTemplate("manifest.plist");
            template.process(root, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
