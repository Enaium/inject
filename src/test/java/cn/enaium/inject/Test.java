package cn.enaium.inject;

/**
 * @author Enaium
 */
public class Test {

    private String name = "Enaium";

    public void render(String var1, int var2, boolean var3) {
        System.out.println(var1);
    }

    public String render() {
        return "RENDER";
    }

    private void test() {
        System.out.println("TEST");
    }

    public String render(String var1) {
        return var1;
    }
}
