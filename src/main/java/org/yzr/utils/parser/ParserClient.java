package org.yzr.utils.parser;

import org.apache.commons.io.FilenameUtils;
import org.yzr.model.Package;

public class ParserClient {

    /**
     *
     * @param filePath
     * @return
     */
    public static Package parse(String filePath) throws ClassNotFoundException {
        PackageParser parser = getParser(filePath);
        if (parser != null) {
            return parser.parse(filePath);
        }
        return null;
    }

    /**
     *
     * @param filePath
     * @return
     */
    private static PackageParser getParser(String filePath) throws ClassNotFoundException {
        String extension = FilenameUtils.getExtension(filePath);
        //
        Class aClass = Class.forName("org.yzr.utils.parser." + extension.toUpperCase()+"Parser");
        try {
            PackageParser packageParser = (PackageParser)aClass.newInstance();
            return packageParser;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
