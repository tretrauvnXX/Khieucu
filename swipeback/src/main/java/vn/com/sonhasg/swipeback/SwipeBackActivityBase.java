package vn.com.sonhasg.swipeback;


public interface SwipeBackActivityBase {
    public abstract SwipeBackLayout getSwipeBackLayout();

    public abstract void setSwipeBackEnable(boolean enable);

    public abstract void scrollToFinishActivity();

}
