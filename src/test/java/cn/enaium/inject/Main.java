package cn.enaium.inject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Enaium
 */
public class Main {
    public static void main(String[] args) throws IOException {
        Inject.addConfiguration("inject.config.json");
        File test = new File("build/classes/java/test/cn/enaium/inject/Test.class");
        Files.write(test.toPath(), Inject.transform(Files.readAllBytes(test.toPath())));
        new Test().render("RENDER1", 0, false);
        System.out.println(new Test().render());
        System.out.println(new Test().render("C"));
    }
}
