import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.Interpolator;

class ScrollAnimator implements  ValueAnimator.AnimatorUpdateListener{
    View mChild;
    ValueAnimator mAnimator;
    private static final int DURATION_MILS = 500;
    ScrollAnimator(View child, Interpolator interpolator){
        mChild= child;
        mAnimator = new ValueAnimator();
        mAnimator.setDuration(DURATION_MILS);
        mAnimator.setInterpolator(interpolator);
        mAnimator.addUpdateListener(this);
    }

    void startAnimator(float endx, int delay){
        float startx = mChild.getTranslationX();
        mAnimator.setFloatValues(startx,endx);
        mAnimator.setStartDelay(delay);
        mAnimator.start();
    }

    void cancel(){
        mAnimator.cancel();
    }

    float getPosition(){
        float curValue=mChild.getTranslationX();
        return curValue;
    }

    void reStartAnimator(float endx, int delay){
        if(mAnimator.isStarted()){
            mAnimator.cancel();
        }
        float startx = mChild.getTranslationX();
        mAnimator.setFloatValues(startx,endx);
        mAnimator.setStartDelay(delay);
        mAnimator.setDuration(DURATION_MILS);
        mAnimator.start();
    }
    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float curValue=(float)animation.getAnimatedValue();
        mChild.setTranslationX(curValue);

    }
}