package cn.enaium.inject;

import cn.enaium.inject.annotation.Inject;
import cn.enaium.inject.annotation.Method;
import cn.enaium.inject.annotation.At;
import cn.enaium.inject.annotation.Shadow;
import cn.enaium.inject.callback.Callback;

/**
 * @author Enaium
 */
@Inject("cn.enaium.inject.Test")
public class TestInject {

    @Shadow
    private String name;

    @Shadow
    private void test() {

    }


    @Method(name = "render", type = @At(type = At.Type.HEAD))
    private void renderInject(String var1, int var2, boolean var3) {
        System.out.println("HELLO INJECT");
    }

    @Method(name = "render", type = @At(type = At.Type.HEAD))
    public void renderInject() {
        test();
        System.out.println(name);
    }

    @Method(name = "render", type = @At(type = At.Type.HEAD))
    public void renderInject(String var1, Callback callback) {
        callback.cancel();
        callback.setReturnValue("CANCEL");
    }
}
