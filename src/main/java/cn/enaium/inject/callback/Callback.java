package cn.enaium.inject.callback;

/**
 * @author Enaium
 */
public class Callback {
    private boolean cancel = false;
    private Object returnValue;

    public void cancel() {
        cancel = true;
    }

    public boolean getCancel() {
        return cancel;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    @SuppressWarnings("unchecked")
    public <T> T getReturnValue() {
        return (T) returnValue;
    }

    public byte getReturnValueB() {
        if (this.returnValue == null) {
            return 0;
        }
        return (Byte) this.returnValue;
    }

    public char getReturnValueC() {
        if (this.returnValue == null) {
            return 0;
        }
        return (Character) this.returnValue;
    }

    public double getReturnValueD() {
        if (this.returnValue == null) {
            return 0.0;
        }
        return (Double) this.returnValue;
    }

    public float getReturnValueF() {
        if (this.returnValue == null) {
            return 0.0F;
        }
        return (Float) this.returnValue;
    }

    public int getReturnValueI() {
        if (this.returnValue == null) {
            return 0;
        }
        return (Integer) this.returnValue;
    }

    public long getReturnValueJ() {
        if (this.returnValue == null) {
            return 0;
        }
        return (Long) this.returnValue;
    }

    public short getReturnValueS() {
        if (this.returnValue == null) {
            return 0;
        }
        return (Short) this.returnValue;
    }

    public boolean getReturnValueZ() {
        if (this.returnValue == null) {
            return false;
        }
        return (Boolean) this.returnValue;
    }
}
