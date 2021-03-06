package dev.dworks.libs.actionbarplus.widget;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import dev.dworks.libs.actionbarplus.R;

public class FloatLabelEditText extends LinearLayout {

    private int mFocusedColor, mUnFocusedColor, mFitScreenWidth,
                mCurrentApiVersion = android.os.Build.VERSION.SDK_INT;
    private float mTextSizeInSp;
    private String mHintText, mEditText;

    private AttributeSet mAttrs;
    private Context mContext;
    private EditText mEditTextView;
    private TextView mFloatingLabel;

    // -----------------------------------------------------------------------
    // default constructors

    public FloatLabelEditText(Context context) {
        super(context);
        mContext = context;
        initializeView();
    }

    public FloatLabelEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mAttrs = attrs;
        initializeView();
    }

    public FloatLabelEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mAttrs = attrs;
        initializeView();
    }

    // -----------------------------------------------------------------------
    // public interface

    public EditText getEditText() {
        return mEditTextView;
    }

    public String getText() {
        if (getEditTextString() != null &&
                getEditTextString().toString() != null &&
                getEditTextString().toString().length() > 0) {
            return getEditTextString().toString();
        }
        return "";
    }

    // -----------------------------------------------------------------------
    // private helpers

    private void initializeView() {

        if (mContext == null) return;

        LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.floatlabel_edittext, this, true);

        mFloatingLabel = (TextView) findViewById(R.id.floating_label_hint);
        mEditTextView = (EditText) findViewById(R.id.floating_label_edit_text);

        getAttributesFromXmlAndStoreLocally();
        setupEditTextView();
        setupFloatingLabel();
    }

    private void getAttributesFromXmlAndStoreLocally() {
        TypedArray attributesFromXmlLayout = mContext.obtainStyledAttributes(mAttrs, R.styleable.FloatLabelEditText );
        if (attributesFromXmlLayout == null) return;

        mHintText = attributesFromXmlLayout.getString(R.styleable.FloatLabelEditText_hint);
        mEditText = attributesFromXmlLayout.getString(R.styleable.FloatLabelEditText_text);
        mTextSizeInSp = getScaledFontSize(attributesFromXmlLayout.getDimensionPixelSize(R.styleable.FloatLabelEditText_textSize, (int) mEditTextView.getTextSize()));
        mFocusedColor = attributesFromXmlLayout.getColor(R.styleable.FloatLabelEditText_textColorHintFocused, android.R.color.black);
        mUnFocusedColor = attributesFromXmlLayout.getColor(R.styleable.FloatLabelEditText_textColorHintUnFocused, android.R.color.darker_gray);
        mFitScreenWidth = attributesFromXmlLayout.getInt(R.styleable.FloatLabelEditText_fitScreenWidth, 0);

        attributesFromXmlLayout.recycle();
    }

    private void setupEditTextView() {
        mEditTextView.setHint(mHintText);
        mEditTextView.setHintTextColor(mUnFocusedColor);
        mEditTextView.setText(mEditText);
        mEditTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextSizeInSp);
        mEditTextView.addTextChangedListener(getTextWatcher());

        if (mFitScreenWidth > 0) {
            mEditTextView.setWidth(getSpecialWidth());
        }

        if (mCurrentApiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            mEditTextView.setOnFocusChangeListener(getFocusChangeListener());
        }
    }

    private void setupFloatingLabel() {
        mFloatingLabel.setText(mHintText);
        mFloatingLabel.setTextColor(mUnFocusedColor);
        mFloatingLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) (mTextSizeInSp / 1.3));

        mFloatingLabel.setPadding(mEditTextView.getPaddingLeft(), 0, 0, 0);

        if (getText().length() > 0) {
            showFloatingLabel();
        }
    }

    private TextWatcher getTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && mFloatingLabel.getVisibility() == INVISIBLE) {
                    showFloatingLabel();
                } else if (s.length() == 0 && mFloatingLabel.getVisibility() == VISIBLE) {
                    hideFloatingLabel();
                }
            }
        };
    }

    private void showFloatingLabel() {
    	setShowHint(true);
        //mFloatingLabel.setVisibility(VISIBLE);
        //mFloatingLabel.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_from_bottom));
    }

    private void hideFloatingLabel() {
    	setShowHint(true);
        //mFloatingLabel.setVisibility(INVISIBLE);
        //mFloatingLabel.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_to_bottom));
    }
    
    private void setShowHint(final boolean show) {
        AnimatorSet animation = null;
        if ((mFloatingLabel.getVisibility() == VISIBLE) && !show) {
            animation = new AnimatorSet();
            ObjectAnimator move = ObjectAnimator.ofFloat(mFloatingLabel, "translationY", 0, mFloatingLabel.getHeight() / 8);
            ObjectAnimator fade = ObjectAnimator.ofFloat(mFloatingLabel, "alpha", 1, 0);
            animation.playTogether(move, fade);
        } else if ((mFloatingLabel.getVisibility() != VISIBLE) && show) {
            animation = new AnimatorSet();
            ObjectAnimator move = ObjectAnimator.ofFloat(mFloatingLabel, "translationY", mFloatingLabel.getHeight() / 8, 0);
            ObjectAnimator fade = ObjectAnimator.ofFloat(mFloatingLabel, "alpha", 0, 1);
            animation.playTogether(move, fade);
        }

        if (animation != null) {
            animation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    mFloatingLabel.setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mFloatingLabel.setVisibility(show ? VISIBLE : INVISIBLE);
                }
            });
            animation.start();
        }
    }

    private OnFocusChangeListener getFocusChangeListener() {
        return new OnFocusChangeListener() {

            ValueAnimator mFocusToUnfocusAnimation, mUnfocusToFocusAnimation;

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                ValueAnimator lColorAnimation;

                if (hasFocus) {
                    lColorAnimation = getFocusToUnfocusAnimation();
                } else {
                    lColorAnimation = getUnfocusToFocusAnimation();
                }

                lColorAnimation.setDuration(700);
                lColorAnimation.start();
            }

            private ValueAnimator getFocusToUnfocusAnimation() {
                if (mFocusToUnfocusAnimation == null) {
                    mFocusToUnfocusAnimation = getFocusAnimation(mUnFocusedColor, mFocusedColor);
                }
                return mFocusToUnfocusAnimation;
            }

            private ValueAnimator getUnfocusToFocusAnimation() {
                if (mUnfocusToFocusAnimation == null) {
                    mUnfocusToFocusAnimation = getFocusAnimation(mFocusedColor, mUnFocusedColor);
                }
                return mUnfocusToFocusAnimation;
            }
        };
    }

    private ValueAnimator getFocusAnimation(int fromColor, int toColor) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mFloatingLabel.setTextColor((Integer) animator.getAnimatedValue());
            }
        });
        return colorAnimation;
    }

    private Editable getEditTextString() {
        return mEditTextView.getText();
    }

    private float getScaledFontSize(float fontSizeFromAttributes) {
        float scaledDensity = getContext().getResources().getDisplayMetrics().scaledDensity;
        return fontSizeFromAttributes/scaledDensity;
    }

    private int getSpecialWidth() {
        float screenWidth = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
        int   prevWidth   = mEditTextView.getWidth();

        switch (mFitScreenWidth) {
            case 2:
                return (int) Math.round(screenWidth * 0.5);
            default:
                return Math.round(screenWidth);
        }
    }
}
